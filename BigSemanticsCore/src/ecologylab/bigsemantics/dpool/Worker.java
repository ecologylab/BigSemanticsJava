package ecologylab.bigsemantics.dpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.dpool.Task.State;

/**
 * Represents a worker. This implementation uses a fixed size thread pool, but subclasses can
 * extend.
 * 
 * @author quyin
 */
public class Worker<T extends Task>
{

  public static interface AvailableEventHandler<T extends Task>
  {
    void onAvailable(Worker<T> worker);
  }

  public static enum Result
  {
    ACCEPTED, ACCEPTED_AND_FULL, REJECTED
  }

  static Logger                    logger = LoggerFactory.getLogger(Worker.class);

  private String                   id;

  private int                      numThreads;

  private int                      priority;

  private ExecutorService          executors;

  private int                      numOngoingTasks;

  private AvailableEventHandler<T> availableEventHandler;

  public Worker(String id, int numThreads)
  {
    this(id, numThreads, 0);
  }

  public Worker(String id, int numThreads, int priority)
  {
    this.id = id;
    this.numThreads = numThreads;
    this.priority = priority;
    executors = Executors.newFixedThreadPool(numThreads);
  }

  public String getId()
  {
    return this.id;
  }

  public int getNumThreads()
  {
    return numThreads;
  }

  public int getPriority()
  {
    return priority;
  }

  public void setAvailableEventHandler(AvailableEventHandler<T> availableEventHandler)
  {
    this.availableEventHandler = availableEventHandler;
  }

  protected synchronized void triggerAvailableEventIfOk()
  {
    if (numOngoingTasks < numThreads && availableEventHandler != null)
    {
      availableEventHandler.onAvailable(this);
    }
  }

  protected synchronized void incOngoing()
  {
    numOngoingTasks++;
  }

  protected synchronized void decOngoing()
  {
    numOngoingTasks--;
  }

  public boolean canHandle(Task task)
  {
    return true;
  }

  private class WorkerHandler implements TaskEventHandler<T>
  {

    private TaskEventHandler<T> handler;

    private WorkerHandler(TaskEventHandler<T> handler)
    {
      this.handler = handler;
    }

    @Override
    public void onComplete(T task)
    {
      notifyWorker(task);
      if (handler != null)
      {
        handler.onComplete(task);
      }
    }

    @Override
    public void onFail(T task)
    {
      notifyWorker(task);
      if (handler != null)
      {
        handler.onFail(task);
      }
    }

    @Override
    public void onTerminate(T task)
    {
      notifyWorker(task);
      if (handler != null)
      {
        handler.onTerminate(task);
      }
    }

    void notifyWorker(T task)
    {
      logger.debug("Task {} performed in worker {}", task, Worker.this);
      decOngoing();
      triggerAvailableEventIfOk();
    }

  }

  /**
   * Submit a task to this worker.
   * 
   * @param task
   * @param handler
   *          The callback.
   * @return
   */
  public synchronized Result submit(T task, final TaskEventHandler<T> handler)
  {
    if (numOngoingTasks >= numThreads)
    {
      return Result.REJECTED;
    }

    WorkerHandler workerHandler = new WorkerHandler(handler);

    doSubmit(task, workerHandler);
    incOngoing();

    if (numOngoingTasks < numThreads)
    {
      return Result.ACCEPTED;
    }
    else
    {
      return Result.ACCEPTED_AND_FULL;
    }
  }

  protected void doSubmit(final T task, final TaskEventHandler<T> handler)
  {
    executors.submit(new Runnable()
    {
      @Override
      public void run()
      {
        performTask(task, handler);
      }
    });
  }

  protected void performTask(T task, TaskEventHandler<T> handler)
  {
    logger.debug("Performing task {}", task);
    synchronized (task)
    {
      State state = task.getState();
      switch (state)
      {
      case SUCCEEDED:
      case TERMINATED:
        return;
      default:
        task.setState(State.ONGOING);
        try
        {
          Object taskArg = getPerformArg();
          if (task.perform(taskArg))
          {
            task.setState(State.SUCCEEDED);
            logger.debug("Task {} completed", task);
            if (handler != null)
            {
              handler.onComplete(task);
            }
            task.notifyAll();
          }
          else
          {
            task.setState(State.WAITING);
            task.incFailCount();
            logger.debug("Task {} failed", task);
            if (handler != null)
            {
              handler.onFail(task);
            }
          }
        }
        catch (Exception e)
        {
          task.setState(State.TERMINATED);
          task.setError(e);
          logger.error("Exception when performing task " + task, e);
          if (handler != null)
          {
            handler.onTerminate(task);
          }
          task.notifyAll();
        }
      }
    }
  }

  /**
   * Subclasses should override this to provide arguments for the task to perform.
   * 
   * @return
   */
  protected Object getPerformArg()
  {
    return this;
  }

  @Override
  public String toString()
  {
    return Worker.class.getSimpleName() + "[" + getId() + ", P" + getPriority() + "]";
  }

}
