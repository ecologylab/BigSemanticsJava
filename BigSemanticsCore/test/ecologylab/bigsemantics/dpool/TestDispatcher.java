package ecologylab.bigsemantics.dpool;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

/**
 * 
 * @author quyin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestDispatcher
{

  @Test
  public void testComplete() throws Exception
  {
    testSimple(true, 1, 0, 0);
  }

  @Test
  public void testTerminate() throws Exception
  {
    testSimple(false, 0, 0, 1);
  }

  void testSimple(final boolean taskResult, int cTimes, int fTimes, int tTimes)
      throws Exception
  {
    final Dispatcher dispatcher = new Dispatcher();

    Worker worker = spy(new Worker("test-worker", 1));
    dispatcher.addWorker(worker);

    Task task = spy(new Task("test-task")
    {
      @Override
      public boolean perform(Object arg) throws Exception
      {
        return taskResult;
      }
    });

    TaskEventHandler<Task> handler = mock(TaskEventHandler.class);
    dispatcher.queueTask(task, handler);
    Runner runner = new Runner()
    {
      @Override
      protected void body() throws InterruptedException
      {
        dispatcher.dispatchTask();
      }
    };
    runner.start();
    task.waitForDone();

    verify(worker, times(1)).submit(eq(task), any(TaskEventHandler.class));
    verify(task, times(1)).perform(anyObject());
    verify(handler, times(cTimes)).onComplete(task);
    verify(handler, times(fTimes)).onFail(task);
    verify(handler, times(tTimes)).onTerminate(task);

    runner.stop();
  }

  @Test
  public void testFail() throws Exception
  {
    final Dispatcher dispatcher = spy(new Dispatcher());
    when(dispatcher.isTooManyFail(any(Task.class))).thenReturn(false);

    Worker worker = spy(new Worker("test-worker", 1));
    dispatcher.addWorker(worker);

    Task task = spy(new Task("test-task")
    {
      int n = 0;

      @Override
      public boolean perform(Object arg) throws Exception
      {
        return ++n >= 3;
      }
    });

    TaskEventHandler<Task> handler = mock(TaskEventHandler.class);
    dispatcher.queueTask(task, handler);
    Runner runner = new Runner()
    {
      @Override
      protected void body() throws InterruptedException
      {
        dispatcher.dispatchTask();
      }
    };
    runner.start();
    task.waitForDone();

    verify(worker, times(3)).submit(eq(task), any(TaskEventHandler.class));
    verify(task, times(3)).perform(anyObject());
    verify(handler, times(1)).onComplete(task);
    verify(handler, times(0)).onFail(task);
    verify(handler, times(0)).onTerminate(task);

    runner.stop();
  }

}
