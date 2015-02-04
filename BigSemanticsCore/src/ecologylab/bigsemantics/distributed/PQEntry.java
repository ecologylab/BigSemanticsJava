package ecologylab.bigsemantics.distributed;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author quyin
 *
 * @param <T>
 */
class PQEntry<T> implements Comparable<PQEntry<T>>
{

  private static AtomicLong SEQ = new AtomicLong(1);

  private int               priority;

  private long              seq;

  private T                 entry;

  public PQEntry(T entry, int priority)
  {
    this.entry = entry;
    this.priority = priority;
  }

  public int getPriority()
  {
    return priority;
  }

  public void setPriority(int priority)
  {
    this.priority = priority;
  }

  public long getSeq()
  {
    return seq;
  }

  public T getEntry()
  {
    return entry;
  }

  public void nextSeq()
  {
    seq = SEQ.getAndIncrement();
  }

  @Override
  public int compareTo(PQEntry<T> other)
  {
    int result = Integer.compare(priority, other.priority);
    if (result == 0)
    {
      return Long.compare(seq, other.seq);
    }
    return result;
  }

}
