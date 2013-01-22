package ecologylab.semantics.metametadata;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import ecologylab.collections.MultiAncestorScope;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

public class MmdScope implements Map<String, MetaMetadata>, IMappable<String>
{

  @simpl_scalar
  String                           name;

  @simpl_map("mmd")
  MultiAncestorScope<MetaMetadata> mmds;

  public MmdScope()
  {
    mmds = new MultiAncestorScope<MetaMetadata>();
  }

  public MmdScope(Map<String, MetaMetadata>... ancestors)
  {
    mmds = new MultiAncestorScope<MetaMetadata>(ancestors);
  }

  @Override
  public int size()
  {
    return mmds.size();
  }

  @Override
  public boolean isEmpty()
  {
    return mmds.isEmpty();
  }

  @Override
  public boolean containsKey(Object key)
  {
    return mmds.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value)
  {
    return mmds.containsValue(value);
  }

  @Override
  public MetaMetadata get(Object key)
  {
    return mmds.get(key);
  }

  @Override
  public MetaMetadata put(String key, MetaMetadata value)
  {
    return mmds.put(key, value);
  }

  @Override
  public MetaMetadata remove(Object key)
  {
    return mmds.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends MetaMetadata> m)
  {
    mmds.putAll(m);
  }

  @Override
  public void clear()
  {
    mmds.clear();
  }

  @Override
  public Set<String> keySet()
  {
    return mmds.keySet();
  }

  @Override
  public Collection<MetaMetadata> values()
  {
    return mmds.values();
  }

  @Override
  public Set<java.util.Map.Entry<String, MetaMetadata>> entrySet()
  {
    return mmds.entrySet();
  }

  @Override
  public String key()
  {
    return name;
  }

}
