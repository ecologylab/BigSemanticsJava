package ecologylab.bigsemantics.distributed;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.distributed.Task.Result;
import ecologylab.bigsemantics.distributed.Task.State;
import ecologylab.serialization.annotations.simpl_scalar;

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

  public static enum SubmissionResult
  {
    ACCEPTED, ACCEPTED_AND_FULL, REJECTED
  }

  static Logger                    logger = LoggerFactory.getLogger(Worker.class);

  @simpl_scalar
  private String                   id;

  @simpl_scalar
  private int                      numThreads;

  private int                      priority;

  private ExecutorService          executors;

  @simpl_scalar
  private int                      numOngoingTasks;

  @simpl_scalar
  private int                      consecutiveFailures;

  private AvailableEventHandler<T> availableEventHandler;

  /**
   * For deserialization only.
   */
  public Worker()
  {
    this("UNINITIALIZED_WORKER", 1);
  }

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

  protected void setId(String id)
  {
    this.id = id;
  }

  public int getNumThreads()
  {
    return numThreads;
  }

  protected void setNumThreads(int numThreads)
  {
    this.numThreads = numThreads;
  }

  public int getPriority()
  {
    return priority;
  }

  public int getConsecutiveFailures()
  {
    return consecutiveFailures;
  }

  /**
   * Subclasses should use this when the failure is caused by the worker itself, not the task.
   */
  protected void incConsecutiveFailures()
  {
    consecutiveFailures++;
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
    triggerAvailableEventIfOk();
  }

  public boolean canHandle(T task) throws Exception
  {
    return true;
  }

  /**
   * Submit a task to this worker.
   * 
   * @param task
   * @param handler
   *          The callback.
   * @return
   */
  public synchronized SubmissionResult submit(T task, final TaskEventHandler<T> handler)
  {
    if (numOngoingTasks >= numThreads)
    {
      return SubmissionResult.REJECTED;
    }

    doSubmit(task, handler);
    incOngoing();

    return (numOngoingTasks < numThreads)
        ? SubmissionResult.ACCEPTED
        : SubmissionResult.ACCEPTED_AND_FULL;
  }

  protected void doSubmit(final T task, final TaskEventHandler<T> handler)
  {
    onSubmitAccept(task, handler);
    executors.submit(new Runnable()
    {
      @Override
      public void run()
      {
        performTask(task, handler);

        logger.debug("Task {} performed in worker {}", task, Worker.this);
        onTaskPerformed(task, handler);
        decOngoing();
      }
    });
  }

  /**
   * Subclasses can override this to take actions when task is accepted by this worker, e.g. to
   * provide worker-specific information for performing.
   */
  protected void onSubmitAccept(T task, TaskEventHandler<T> handler)
  {
    // no op
  }

  /**
   * Subclasses can override this to take actions after task is performed by this worker.
   */
  protected void onTaskPerformed(T task, TaskEventHandler<T> handler)
  {
    // no op
  }

  /**
   * Subclasses can override this to take actions when this worker is removed from the dispatcher.
   */
  protected void onRemoval()
  {
    // no op
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
        Result result = Result.FATAL;
        try
        {
          result = task.perform();
        }
        catch (Exception e)
        {
          result = Result.FATAL;
          logger.error("Exception when performing task " + task, e);
        }

        if (result == Result.OK)
        {
          task.setState(State.SUCCEEDED);
          logger.debug("Task {} completed", task);
          if (handler != null)
          {
            handler.onComplete(task);
          }
          task.notifyAll();
        }
        else if (result == Result.ERROR)
        {
          task.setState(State.WAITING);
          task.incFailCount();
          logger.debug("Task {} failed", task);
          if (handler != null)
          {
            handler.onFail(task);
          }
        }
        else
        {
          // result == Result.FATAL
          task.setState(State.TERMINATED);
          logger.debug("Task {} terminated with fatal problem", task);
          if (handler != null)
          {
            handler.onTerminate(task);
          }
          task.notifyAll();
        }
        consecutiveFailures = 0;
      }
    }
  }

  @Override
  public String toString()
  {
    return Worker.class.getSimpleName() + "[" + getId() + ", P" + getPriority() + "]";
  }

}
