package ecologylab.bigsemantics.documentcache;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/** 
 * JUnit test for the SimplMemCache class
 * 
 * @author colton
 *
 */
public class SimplMemCacheTest
{
	private SimplMemCache cache;
	
	@Before
	public void setUp() throws Exception
	{
		cache = new SimplMemCache();
	}

	@Test
	public void testContainsKey_empty()
	{
		assertFalse("Empty map does not contain key", cache.containsKey("someKey"));
	}
	
	@Test
	public void testContainsKey_shouldContain()
	{
		cache.put("someKey", new Integer(35));
		
		assertTrue("Map contains previously added key and value", cache.containsKey("someKey"));
	}

	@Test (expected = NullPointerException.class)
	public void testGet_empty()
	{
		int testInt = (int) cache.get("someKey");
		
		fail("Should have returned null, resulting in an exception");
	}
	
	@Test
	public void testGet_shouldContain()
	{
		String someString = "test String";
		cache.put("someKey", someString);
		
		assertEquals("Correct previuosly added item is returned by the map", someString, cache.get("someKey"));
	}

	@Test
	public void testPut()
	{
		String someString = "test String for put test";
		cache.put("someKey", someString);
		
		assertEquals("Correct previuosly added item is returned by the map", cache.get("someKey"), someString);
	}

	@Test
	public void testPutIfAbsent_notPresent()
	{
		String someString = "test String";
		cache.putIfAbsent("someKey", someString);
		
		assertEquals("The item was correctly added to the map", cache.get("someKey"), someString);
	}
	
	@Test
	public void testPutIfAbsent_alreadyPresent()
	{
		String someString0 = "test String";
		String someString1 = "test String number two";
		cache.put("someKey", someString0);
		
		assertEquals("The original item was added to the map", cache.get("someKey"), someString0);
		
		cache.putIfAbsent("someKey", someString1);
		
		assertEquals("The original item was not replaced", cache.get("someKey"), someString0);
	}

	@Test
	public void testReplaceStringObjectObject_exists()
	{
		String someString0 = "test String";
		String someString1 = "test String number two";
		cache.put("someKey", someString0);
		
		assertEquals("The original item was added to the map", cache.get("someKey"), someString0);
		
		cache.replace("someKey", someString0, someString1);
		
		assertEquals("The original item was replaced", cache.get("someKey"), someString1);
		
	}
	
	@Test
	public void testReplaceStringObjectObject_doesNotExist()
	{
		String someString = "test String";
		
		cache.replace("someKey", someString);
		
		assertFalse("The item is not in the map", cache.containsKey("someKey"));
	}

	@Test
	public void testReplaceStringObject()
	{
		String someString0 = "test String";
		String someString1 = "test String number two";
		cache.put("someKey", someString0);
		
		assertEquals("The original item was added to the map", cache.get("someKey"), someString0);
		
		cache.replace("someKey", someString1);
		
		assertEquals("The original item was replaced", cache.get("someKey"), someString1);
	}

	@Test
	public void testRemoveString()
	{
		String someString = "test String";
		cache.put("someKey", someString);
		
		assertEquals("The original item was added to the map", cache.get("someKey"), someString);
		
		cache.remove("someKey");
	
		assertFalse("The item was removed from the map", cache.containsKey("someKey"));
	}

	@Test
	public void testRemoveStringObject()
	{
		String someString = "test String";
		cache.put("someKey", someString);
		
		assertEquals("The original item was added to the map", cache.get("someKey"), someString);
		
		cache.remove("someKey", someString);
	
		assertFalse("The item was removed from the map", cache.containsKey("someKey"));
	}

}
