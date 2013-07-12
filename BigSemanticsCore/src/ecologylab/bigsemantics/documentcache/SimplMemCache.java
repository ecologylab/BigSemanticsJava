package ecologylab.bigsemantics.documentcache;

import java.util.concurrent.ConcurrentHashMap;
import java.lang.UnsupportedOperationException;

public class SimplMemCache implements ISimplCache
{
	private ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
	
	public SimplMemCache()
	{	
	}

	@Override
	public boolean containsKey(String key)
	{
		return map.containsKey(key);
	}

	@Override
	public Object get(String key)
	{
		return map.containsKey(key) ? map.get(key) : null;
	}
	
	@Override
	public Object get(String key, String revision)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void put(String key, Object obj)
	{
		map.put(key, obj);
	}

	@Override
	public Object putIfAbsent(String key, Object obj)
	{
		map.putIfAbsent(key, obj);
		
		return map.get(key);
	}

	@Override
	public boolean replace(String key, Object oldObj, Object newObj)
	{
		return map.replace(key, oldObj, newObj);
	}

	@Override
	public Object replace(String key, Object newObj)
	{
		return map.replace(key, newObj) == null ? null : map.get(key);
	}

	@Override
	public void remove(String key)
	{
		map.remove(key);
	}

	@Override
	public boolean remove(String key, Object obj)
	{
		return map.remove(key, obj);
	}
}
