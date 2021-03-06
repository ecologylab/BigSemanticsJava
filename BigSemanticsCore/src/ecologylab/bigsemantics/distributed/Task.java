package ecologylab.bigsemantics.distributed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Representing a redistributable task. Can inherit this to include more information.
 * 
 * @author quyin
 * 
 * @param <A>
 *          Type of the argument for performing.
 */
public abstract class Task implements Cloneable
{

  public static enum State
  {
    WAITING, ONGOING, SUCCEEDED, TERMINATED
  }

  public static enum Result
  {
    OK, ERROR, FATAL
  }

  static Logger  logger = LoggerFactory.getLogger(Task.class);

  @simpl_scalar
  private String id;

  @simpl_scalar
  private int    priority;

  @simpl_scalar
  private State  state  = State.WAITING;

  @simpl_scalar
  private int    failCount;

  public Task(String id)
  {
    this(id, 0);
  }

  public Task(String id, int priority)
  {
    this.id = id;
    this.priority = priority;
  }

  public String getId()
  {
    return id;
  }

  public int getPriority()
  {
    return priority;
  }

  public synchronized State getState()
  {
    return state;
  }

  protected synchronized void setState(State state)
  {
    this.state = state;
  }

  public int getFailCount()
  {
    return failCount;
  }

  protected void incFailCount()
  {
    failCount++;
  }

  /**
   * Perform the task.
   * 
   * @return The result status. OK means task succeeded, ERROR means there is an error and the
   *         system should retry, and FATAL means there is a non-recoverable error happened and the
   *         task should be abandoned.
   */
  abstract public Result perform();

  public synchronized void waitForDone() throws InterruptedException
  {
    if (state != State.SUCCEEDED && state != State.TERMINATED)
    {
      this.wait();
    }
  }

  public synchronized void waitForDone(long ms) throws InterruptedException
  {
    if (state != State.SUCCEEDED && state != State.TERMINATED)
    {
      this.wait(ms);
    }
  }

  @Override
  public String toString()
  {
    return Task.class.getSimpleName() + "[" + getId() + "]";
  }

}
