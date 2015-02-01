package ecologylab.bigsemantics.dpool;

/**
 * Helper class.
 * 
 * @author quyin
 */
public abstract class Runner
{

  Thread  thread;

  boolean stop = false;

  public Runner()
  {
    thread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        while (!stop)
        {
          try
          {
            body();
          }
          catch (InterruptedException e)
          {
            break;
          }
          catch (Exception e)
          {
            e.printStackTrace();
            break;
          }
        }
      }
    });
  }

  abstract protected void body() throws Exception;

  public void start()
  {
    stop = false;
    thread.start();
  }

  public void stop()
  {
    stop = true;
    thread.interrupt();
  }

}
