package ecologylab.bigsemantics.distributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.distributed.Task.State;
import ecologylab.bigsemantics.distributed.Worker.AvailableEventHandler;

/**
 * The dispatcher. Needs a set of workers. Can queue tasks to this.
 * 
 * @author quyin
 *
 * @param <T>
 * @param <W>
 */
public class Dispatcher<T extends Task, W extends Worker<T>>
{

  /**
   * Convenient class for handling the task queue.
   * 
   * @author quyin
   *
   * @param <T>
   */
  private static class TaskEntry<T extends Task> extends PQEntry<T>
  {

    private TaskEventHandler<T> handler;

    public TaskEntry(T task, TaskEventHandler<T> handler)
    {
      super(task, task.getPriority());
      this.handler = handler;
    }

    public T getTask()
    {
      return getEntry();
    }

    public TaskEventHandler<T> getHandler()
    {
      return handler;
    }

  }

  /**
   * Convenient class for handling the worker queue.
   * 
   * @author quyin
   *
   * @param <T>
   * @param <W>
   */
  private static class WorkerEntry<T extends Task, W extends Worker<T>> extends PQEntry<W>
  {

    public WorkerEntry(W worker)
    {
      super(worker, worker.getPriority());
    }

    public W getWorker()
    {
      return getEntry();
    }

  }

  static Logger                                    logger;

  static
  {
    logger = LoggerFactory.getLogger(Dispatcher.class);
  }

  private Map<String, W>                           workers;

  private PriorityBlockingQueue<TaskEntry<T>>      taskQueue;

  private PriorityBlockingQueue<WorkerEntry<T, W>> workerQueue;

  public Dispatcher()
  {
    workers = new HashMap<String, W>();
    taskQueue = new PriorityBlockingQueue<TaskEntry<T>>();
    workerQueue = new PriorityBlockingQueue<WorkerEntry<T, W>>();
  }

  Map<String, W> getWorkers()
  {
    return workers;
  }

  public void addWorker(W worker)
  {
    worker.setAvailableEventHandler(new AvailableEventHandler<T>()
    {
      @SuppressWarnings("unchecked")
      @Override
      public void onAvailable(Worker<T> worker)
      {
        queueWorker((W) worker);
      }
    });
    onAddWorker(worker);
    String id = worker.getId();
    workers.put(id, worker);
    queueWorker(worker);
  }

  /**
   * Subclasses can use this to operate on a worker when it is added to this dispatcher.
   * 
   * @param worker
   */
  protected void onAddWorker(W worker)
  {
    // no op
  }

  /**
   * Queue a task to this dispatcher.
   * 
   * @param task
   * @param handler
   *          The callback.
   */
  public void queueTask(T task, TaskEventHandler<T> handler)
  {
    taskQueue.put(new TaskEntry<T>(task, handler));
    onQueued(task);
    logger.debug("Task queued: {}", task);
  }

  /**
   * Subclasses can use this to notify the task that it is being dispatched.
   * 
   * @param task
   */
  protected void onQueued(T task)
  {
    // no op
  }

  protected void queueWorker(W worker)
  {
    workerQueue.put(new WorkerEntry<T, W>(worker));
  }

  /**
   * Key method for dispatching one task (or moving it to the end if no workers can handle it right
   * now).
   * @throws Exception 
   */
  public void dispatchTask() throws Exception
  {
    TaskEntry<T> taskEntry = taskQueue.take(); // this will block if taskQueue is empty
    T task = taskEntry.getTask(); // the task to dispatch in this invocation
    onDispatch(task);
    TaskEventHandler<T> handler = taskEntry.getHandler();
    logger.debug("Task taken: {}", task);

    // try to dispatch the task
    boolean dispatched = false;
    List<WorkerEntry<T, W>> triedEntries = new ArrayList<WorkerEntry<T, W>>();
    while (true)
    {
      WorkerEntry<T, W> workerEntry = null;
      if (triedEntries.isEmpty())
      {
        // initially, if no workers available, wait for one
        workerEntry = workerQueue.take();
      }
      else
      {
        // if we are not in the situation that no workers are available, keep trying without wait
        workerEntry = workerQueue.poll();
        if (workerEntry == null)
        {
          // tried all the workers but no one can handle it
          break;
        }
      }
      W worker = workerEntry.getWorker();
      logger.debug("Worker polled: {}", worker);

      if (worker.canHandle(task))
      {
        DispatcherHandler dispatcherHandler = getDispatcherHandler(handler);
        logger.debug("Trying submitting task {} to worker {}", task, worker);
        Worker.Result result = worker.submit(task, dispatcherHandler);
        logger.debug("Submitted task {} to worker {}, result: {}", task, worker, result);
        switch (result)
        {
        case ACCEPTED:
          dispatched = true;
          queueWorker(worker);
          break;
        case ACCEPTED_AND_FULL:
          dispatched = true;
          break;
        case REJECTED:
          dispatched = false;
          break;
        }
        break; // to break the while loop, not the switch
      }
      else
      {
        logger.debug("Worker {} not able to handle task {}", worker, task);
        triedEntries.add(workerEntry);
      }
    }
    workerQueue.addAll(triedEntries);

    // if not dispatched, put the task back
    if (!dispatched)
    {
      logger.debug("Task {} not handled, re-enqueue", task);
      queueTask(task, handler);
    }
  }

  /**
   * Subclasses can use this to notify the task that it is being dispatched.
   * 
   * @param task
   */
  protected void onDispatch(T task)
  {
    // no op
  }

  private class DispatcherHandler implements TaskEventHandler<T>
  {

    private TaskEventHandler<T> handler;

    private DispatcherHandler(TaskEventHandler<T> handler)
    {
      this.handler = handler;
    }

    @Override
    public void onComplete(T task)
    {
      if (handler != null)
      {
        handler.onComplete(task);
      }
    }

    @Override
    public void onFail(T task)
    {
      if (isTooManyFail(task))
      {
        logger.debug("Terminating task {} after too many failures", task);
        task.setState(State.TERMINATED);
        if (handler != null)
        {
          handler.onTerminate(task);
        }
        task.notifyAll();
      }
      else
      {
        logger.debug("Retry task {} after failure", task);
        queueTask(task, handler);
      }
    }

    @Override
    public void onTerminate(T task)
    {
      if (handler != null)
      {
        handler.onTerminate(task);
      }
    }

  }

  protected DispatcherHandler getDispatcherHandler(TaskEventHandler<T> handler)
  {
    DispatcherHandler dispatcherHandler = new DispatcherHandler(handler);
    return dispatcherHandler;
  }

  /**
   * Subclasses can override this to smartly determine if a task has too many failures, based on
   * task information (such as failCount).
   * 
   * @param task
   * @return true if there are too many failures for the input task; otherwise false.
   */
  protected boolean isTooManyFail(T task)
  {
    return true;
  }

}
