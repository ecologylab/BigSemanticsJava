package ecologylab.bigsemantics.dpool;

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
public abstract class Task<A>
{

  public static enum State
  {
    WAITING, ONGOING, SUCCEEDED, TERMINATED
  }

  static Logger     logger = LoggerFactory.getLogger(Task.class);

  @simpl_scalar
  private String    id;

  @simpl_scalar
  private int       priority;

  @simpl_scalar
  private State     state  = State.WAITING;

  @simpl_scalar
  private int       failCount;

  private Exception error;

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

  public Exception getError()
  {
    return error;
  }

  protected void setError(Exception error)
  {
    this.error = error;
  }

  /**
   * Perform the task.
   * 
   * If an Exception is thrown, the system will NOT retry the task but terminate it. If no Exception
   * is thrown, but this method returns false (which means the task failed), the system will retry
   * the task.
   * 
   * @param arg
   *          The argument provided by the caller. For example, can be environmental information.
   * @return true if the task succeeded; otherwise false.
   * @throws Exception
   */
  abstract public boolean perform(A arg) throws Exception;

  public synchronized void waitForDone() throws InterruptedException
  {
    if (state != State.SUCCEEDED && state != State.TERMINATED)
    {
      this.wait();
    }
  }

  @Override
  public String toString()
  {
    return Task.class.getSimpleName() + "[" + getId() + "]";
  }

}
