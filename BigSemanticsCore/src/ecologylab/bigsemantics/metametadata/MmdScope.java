package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ecologylab.generic.StringBuilderBaseUtils;
import ecologylab.serialization.annotations.simpl_classes;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

/**
 * A scope (map: String -&gt; Object) with multiple ancestors.
 * 
 * Objecto reduce creating unnecessary objects, this class will not create a new HashMap until
 * necessary (i.e. when you put something into the local scope).
 * 
 * This class ignores null keys.
 * 
 * @author quyin
 * 
 * @param <Object>
 *          Objecthe value type.
 */
public class MmdScope implements Map<String, Object>, IMappable<String>
{

  public static final String                   NO_ID;

  private static final HashMap<String, Object> EMPTY_HASH_MAP;

  public static final MmdScope                 EMPTY_SCOPE;

  static
  {
    NO_ID = "NO_ID";
    EMPTY_HASH_MAP = new HashMap<String, Object>();
    EMPTY_SCOPE = new MmdScope("EMPObjectY")
    {
      @Override
      public void addAncestor(MmdScope ancestor)
      {
        // no op
      }

      @Override
      public Object put(String key, Object value)
      {
        // no op
        return value;
      }

      @Override
      public void putAll(Map<? extends String, ? extends Object> m)
      {
        // no op
      }
    };
  }

  @simpl_scalar
  private String                               id;

  @simpl_map("element")
  @simpl_classes({ MetaMetadata.class, MmdGenericTypeVar.class })
  private HashMap<String, Object>              local;

  // FIXME we don't want to serialize this field in the service, but want to do it with tests.
  // @simpl_collection("ancestor")
  private List<MmdScope>                       ancestors;

  public MmdScope()
  {
    this(NO_ID, new MmdScope[] {});
  }

  public MmdScope(String id)
  {
    this(id, new MmdScope[] {});
  }

  public MmdScope(MmdScope... ancestors)
  {
    this(NO_ID, ancestors);
  }

  public MmdScope(String id, MmdScope... ancestors)
  {
    super();
    this.id = id;
    addAncestors(ancestors);
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String key()
  {
    return id;
  }

  protected List<MmdScope> ancestors()
  {
    if (ancestors == null)
    {
      ancestors = new ArrayList<MmdScope>();
    }
    return ancestors;
  }

  /**
   * @return All ancestors using DFS. This is used to prevent infinite loops with ancestors.
   */
  protected List<MmdScope> allAncestors()
  {
    List<MmdScope> result = new ArrayList<MmdScope>();
    allAncestorsHelper(result, this);
    return result;
  }

  private void allAncestorsHelper(List<MmdScope> result, MmdScope scope)
  {
    if (scope.ancestors != null)
    {
      for (MmdScope ancestor : scope.ancestors)
      {
        if (!result.contains(ancestor))
        {
          result.add(ancestor);
          allAncestorsHelper(result, ancestor);
        }
      }
    }
  }

  /**
   * @param scope
   * @return If scope is an (not necessarily immediate) ancestor of this scope.
   */
  public boolean isAncestor(MmdScope scope)
  {
    return ancestors == null ? false : allAncestors().contains(scope);
  }

  public void addAncestor(MmdScope ancestor)
  {
    if (ancestor != null && ancestor != this && !isAncestor(ancestor))
    {
      this.ancestors().add(ancestor);
    }
  }

  public void addAncestors(MmdScope... ancestors)
  {
    if (ancestors != null)
    {
      for (MmdScope ancestor : ancestors)
      {
        this.addAncestor(ancestor);
      }
    }
  }

  public void removeImmediateAncestor(MmdScope ancestor)
  {
    if (ancestors != null)
    {
      ancestors.remove(ancestor);
    }
  }

  @Override
  public int size()
  {
    return local == null ? 0 : local.size();
  }

  @Override
  public boolean isEmpty()
  {
    return local == null ? true : local.isEmpty();
  }

  @Override
  public Set<Entry<String, Object>> entrySet()
  {
    return local == null ? EMPTY_HASH_MAP.entrySet() : local.entrySet();
  }

  @Override
  public Set<String> keySet()
  {
    return local == null ? EMPTY_HASH_MAP.keySet() : local.keySet();
  }

  @Override
  public Collection<Object> values()
  {
    return local == null ? EMPTY_HASH_MAP.values() : local.values();
  }

  public <T> Collection<T> valuesOfType(Class<T> clazz)
  {
    List<T> result = new ArrayList<T>();
    for (Object obj : values())
    {
      if (obj.getClass() == clazz)
      {
        result.add((T) obj);
      }
    }
    return result;
  }

  /**
   * This will check BOTH the local scope AND ancestors.
   * 
   * Ancestors will be looked up in the order of being added.
   */
  @Override
  public boolean containsKey(Object key)
  {
    if (key != null)
    {
      if (local != null && local.containsKey(key))
      {
        return true;
      }
      if (ancestors != null)
      {
        List<MmdScope> allAncestors = allAncestors();
        for (MmdScope ancestor : allAncestors)
        {
          if (ancestor.containsKeyLocally(key))
          {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * This will check BOObjectH the local scope AND ancestors.
   * 
   * Ancestors will be looked up in the order of being added.
   */
  @Override
  public boolean containsValue(Object value)
  {
    if (local != null && local.containsValue(value))
    {
      return true;
    }
    if (ancestors != null)
    {
      List<MmdScope> allAncestors = allAncestors();
      for (MmdScope ancestor : allAncestors)
      {
        if (ancestor.containsValueLocally((Object) value))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * This will check BOObjectH the local scope AND ancestors.
   * 
   * Ancestors will be looked up in the order of being added.
   */
  @Override
  public Object get(Object key)
  {
    if (key != null)
    {
      if (containsKeyLocally(key))
      {
        return local.get(key);
      }
      if (ancestors != null)
      {
        List<MmdScope> allAncestors = allAncestors();
        for (MmdScope ancestor : allAncestors)
        {
          if (ancestor.containsKeyLocally(key))
          {
            return ancestor.getLocally(key);
          }
        }
      }
    }
    return null;
  }

  /**
   * Get all values for the key from BOObjectH this scope AND ancestors.
   * 
   * Ancestors will be looked up in the order of being added.
   * 
   * @param key
   * @return
   */
  public List<Object> getAll(Object key)
  {
    List<Object> result = new ArrayList<Object>();
    if (key != null)
    {
      if (local != null && local.containsKey(key))
      {
        result.add(local.get(key));
      }
      if (ancestors != null)
      {
        List<MmdScope> allAncestors = allAncestors();
        for (MmdScope ancestor : allAncestors)
        {
          if (ancestor.containsKeyLocally(key))
          {
            Object value = ancestor.getLocally(key);
            result.add(value);
          }
        }
      }
    }
    return result;
  }

  @Override
  public Object put(String key, Object value)
  {
    if (key != null)
    {
      if (local == null)
      {
        local = new HashMap<String, Object>();
      }
      return local.put(key, value);
    }
    return null;
  }

  /**
   * Only put value into the scope when it is not null.
   * 
   * @param key
   * @param value
   */
  public Object putIfValueNotNull(String key, Object value)
  {
    if (key != null && value != null)
    {
      return put(key, value);
    }
    return null;
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m)
  {
    if (local == null)
    {
      local = new HashMap<String, Object>();
    }
    local.putAll(m);
  }

  @Override
  public Object remove(Object key)
  {
    return local == null ? null : key == null ? null : local.remove(key);
  }

  @Override
  public void clear()
  {
    if (local != null)
    {
      local.clear();
    }
  }

  public boolean containsKeyLocally(Object key)
  {
    return local == null ? false : key == null ? false : local.containsKey(key);
  }

  public boolean containsValueLocally(Object value)
  {
    return local == null ? false : local.containsValue(value);
  }

  /**
   * Get the value of the given key only from this scope.
   * 
   * @param key
   * @return
   */
  public Object getLocally(Object key)
  {
    return local == null ? null : key == null ? null : local.get(key);
  }

  @Override
  public String toString()
  {
    StringBuilder sb = StringBuilderBaseUtils.acquire();
    Set<Object> visited = new HashSet<Object>();
    toStringHelper(sb, "", visited);
    String result = sb.toString();
    StringBuilderBaseUtils.release(sb);
    return result;
  }

  private void toStringHelper(StringBuilder buf, String indent, Set<Object> visited)
  {
    buf.append(getClass().getSimpleName());
    buf.append(".").append(id == null ? NO_ID : id);
    buf.append(": [").append(size()).append("]");
    buf.append(local == null ? "{}" : local);
    if (ancestors != null && ancestors.size() > 0)
    {
      for (MmdScope ancestor : ancestors)
      {
        StringBuilder ancestorStr = StringBuilderBaseUtils.acquire();
        ancestorStr.append("\n").append(indent).append("    -> ");
        if (visited.contains(ancestor))
        {
          ancestorStr.append("(Ref: ");
          ancestorStr.append(ancestor.getClass().getSimpleName());
          ancestorStr.append(".").append(ancestor.getId()).append(")");
        }
        else
        {
          visited.add(ancestor);
          ancestor.toStringHelper(ancestorStr, indent + "    ", visited);
        }
        buf.append(ancestorStr);
        StringBuilderBaseUtils.release(ancestorStr);
      }
    }
  }

  public void reset()
  {
    id = null;
    local = null;
    ancestors = null;
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    // inheritance relation:
    // s4 -> s2 -----------> s1
    // \---> s3(nolocal) -/
    MmdScope s1 = new MmdScope("s1");
    s1.put("one", 1);
    s1.put("two", 2);

    MmdScope s2 = new MmdScope("s2", s1);
    s2.put("three", 3);

    MmdScope s3 = new MmdScope("s3", s1);

    MmdScope s4 = new MmdScope("s4", s2, s3);
    s4.put("five", 5);

    System.out.println(s4);
    System.out.println(s4.get("five"));
    System.out.println(s4.get("two"));
  }

}
