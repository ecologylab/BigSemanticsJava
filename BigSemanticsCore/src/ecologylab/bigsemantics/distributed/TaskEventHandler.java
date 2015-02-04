package ecologylab.bigsemantics.distributed;

/**
 * An event handler interface for handling tasks.
 * 
 * @author quyin
 *
 * @param <T>
 */
public interface TaskEventHandler<T extends Task>
{

  /**
   * Called when completed successfully.
   * 
   * @param task
   */
  void onComplete(T task);

  /**
   * Called when there is a failure. Can retry.
   * 
   * For this event and onTerminate(), there may be error message available in teh task object.
   * 
   * @param task
   */
  void onFail(T task);

  /**
   * Called when there is an irrecoverable failure or there has been too many failures. No more
   * retries.
   * 
   * For this event and onFail(), there may be error message available in teh task object.
   * 
   * @param task
   */
  void onTerminate(T task);

}
