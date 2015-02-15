package ecologylab.bigsemantics.distributed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import ecologylab.bigsemantics.distributed.Task.State;

/**
 * 
 * @author quyin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestDispatcherMultiThreads
{

  static class FakeTask extends Task
  {

    int                maxFailCount;

    int                waitTime;

    long               beginTime = -1;

    long               endTime;

    List<Worker<Task>> workers   = new ArrayList<Worker<Task>>();

    public FakeTask(String id, int priority, int maxFailCount, int waitTime)
    {
      super(id, priority);
      this.maxFailCount = maxFailCount;
      this.waitTime = waitTime;
    }

    @Override
    public Result perform()
    {
      try
      {
        Thread.sleep(waitTime);
      }
      catch (InterruptedException e)
      {
        return Result.FATAL;
      }
      return getFailCount() >= maxFailCount ? Result.OK : Result.ERROR;
    }

  }

  static class FakeWorker extends Worker<Task>
  {

    List<String> forbiddenDomains;

    public FakeWorker(String id, int numThreads, String... forbiddenDomains)
    {
      super(id, numThreads);
      this.forbiddenDomains = Arrays.asList(forbiddenDomains);
    }

    @Override
    public boolean canHandle(Task task)
    {
      String id = task.getId();
      for (String forbiddenDomain : forbiddenDomains)
      {
        if (id.contains(forbiddenDomain))
        {
          return false;
        }
      }
      return true;
    }

    @Override
    protected void performTask(Task task, TaskEventHandler<Task> handler)
    {
      if (task instanceof FakeTask)
      {
        FakeTask fakeTask = (FakeTask) task;
        fakeTask.workers.add(this);
      }
      super.performTask(task, handler);
    }

  }

  @Test
  public void testMultiThreads() throws InterruptedException
  {
    final Dispatcher dispatcher = new Dispatcher()
    {
      @Override
      protected void onDispatch(Task task)
      {
        if (task instanceof FakeTask)
        {
          FakeTask fakeTask = (FakeTask) task;
          if (fakeTask.beginTime < 0)
          {
            fakeTask.beginTime = System.currentTimeMillis();
          }
        }
      }

      @Override
      protected boolean isTooManyFail(Task task)
      {
        return task.getFailCount() >= 4;
      }
    };

    // add 4 workers, each with 4 threads and 2 domains that it cannot handle
    for (int i = 1; i <= 4; ++i)
    {
      FakeWorker worker = new FakeWorker("w" + i, 4, "" + i + ".com", "" + (11 - i) + ".com");
      dispatcher.addWorker(worker);
    }

    // create 500 tasks: 10 domains x 5 fail counts x 10 wait times
    Random rand = new Random(System.currentTimeMillis()); // generating random priority
    List<FakeTask> tasks = new ArrayList<FakeTask>();
    Map<Integer, Map<Integer, List<FakeTask>>> taskMap =
        new HashMap<Integer, Map<Integer, List<FakeTask>>>();
    for (int i = 1; i <= 10; ++i)
    {
      for (int failCount = 0; failCount < 5; ++failCount)
      {
        taskMap.put(failCount, new HashMap<Integer, List<FakeTask>>());
        for (int waitTime = 0; waitTime < 100; waitTime += 10)
        {
          taskMap.get(failCount).put(waitTime, new ArrayList<FakeTask>());
          String id = String.format("http://%d.com/index.html?f=%d&t=%d", i, failCount, waitTime);
          FakeTask task = new FakeTask(id, rand.nextInt(), failCount, waitTime);
          TaskEventHandler<FakeTask> handler = new TaskEventHandler<FakeTask>()
          {
            @Override
            public void onComplete(FakeTask task)
            {
              task.endTime = System.currentTimeMillis();
              System.out.format("Task %s completed\n", task);
            }

            @Override
            public void onFail(FakeTask task)
            {
              task.endTime = System.currentTimeMillis();
              System.out.format("Task %s failed\n", task);
            }

            @Override
            public void onTerminate(FakeTask task)
            {
              task.endTime = System.currentTimeMillis();
              System.out.format("Task %s terminated\n", task);
            }
          };
          tasks.add(task);
          taskMap.get(failCount).get(waitTime).add(task);
          dispatcher.queueTask(task, handler);
        }
      }
    }

    // do it!
    Runner runner = new Runner()
    {
      @Override
      protected void body() throws Exception
      {
        dispatcher.dispatchTask();
      }
    };
    long allBeginTime = System.currentTimeMillis();
    runner.start();
    for (FakeTask task : tasks)
    {
      task.waitForDone();
    }
    long allTotalTime = System.currentTimeMillis() - allBeginTime;

    // verify:

    // no worker does domains it cannot handle
    for (FakeTask task : tasks)
    {
      for (Worker worker : task.workers)
      {
        assertTrue(worker instanceof FakeWorker);
        for (String domain : ((FakeWorker) worker).forbiddenDomains)
        {
          assertFalse(task.getId().contains(domain));
        }
      }
    }

    // some of them should have completed, while some terminated.
    for (FakeTask task : tasks)
    {
      if (task.maxFailCount == 4)
      {
        assertEquals(State.TERMINATED, task.getState());
      }
      else
      {
        assertEquals(State.SUCCEEDED, task.getState());
      }
    }

    // stats:

    // for each fail count x wait time: average of: total time, # of retries, # of different workers
    double ttime = 0;
    for (Integer failCount : taskMap.keySet())
    {
      Map<Integer, List<FakeTask>> these = taskMap.get(failCount);
      for (Integer waitTime : these.keySet())
      {
        List<FakeTask> those = these.get(waitTime);
        int n = those.size();
        double time = 0;
        double retries = 0;
        double uniqw = 0;
        for (FakeTask task : those)
        {
          time += task.endTime - task.beginTime;
          retries += task.getFailCount() + 1;
          uniqw = (new HashSet<Worker>(task.workers)).size();
        }
        ttime += time;

        System.out.format("FC=%d, WT=%d: Tmin = %d, Tmean = %.0f, Rmean = %.0f, Umean = %.0f\n",
                          failCount, waitTime, failCount * waitTime,
                          time / n, retries / n, uniqw / n);
      }
    }

    System.out.format("Total mean latency: %.2f\n", ttime / tasks.size());
    System.out.format("Total throughput per sec: %.2f\n", tasks.size() / (allTotalTime / 1000.0));
  }

}
