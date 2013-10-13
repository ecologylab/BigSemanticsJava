package ecologylab.bigsemantics.documentcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.metadata.builtins.Document;

/** 
 * JUnit test for the SimplMemCache class
 * 
 * @author colton
 *
 */
public class TestHashMapDocumentCache
{

	private HashMapDocumentCache cache;
	
	@Before
	public void setUp() throws Exception
	{
		cache = new HashMapDocumentCache();
	}

	@Test
	public void testContainsKey_empty()
	{
		assertFalse("Empty map does not contain key", cache.containsKey("someKey"));
	}
	
	@Test
	public void testContainsKey_shouldContain()
	{
		cache.put("someKey", new Document());
		
		assertTrue("Map contains previously added key and value", cache.containsKey("someKey"));
	}

	@Test
	public void testGet_empty()
	{
		assertNull(cache.get("someKey"));
	}
	
	@Test
	public void testPutAndGet()
	{
		Document someDoc = new Document();
    String title = "test title";
		someDoc.setTitle(title);
		cache.put("someKey", someDoc);
		
		Document doc = cache.get("someKey");
		assertNotNull(doc);
		assertEquals("Correct previuosly added item is returned by the map", title, doc.getTitle());
	}

	@Test
	public void testPutIfAbsent_notPresent()
	{
		Document someDoc = new Document();
    String title = "test title";
		someDoc.setTitle(title);
		cache.putIfAbsent("someKey", someDoc);
		
		Document doc = cache.get("someKey");
		assertNotNull(doc);
		assertEquals("The item was correctly added to the map", title, doc.getTitle());
	}
	
	@Test
	public void testPutIfAbsent_alreadyPresent()
	{
		String title1 = "test title 1";
		Document someDoc1 = new Document();
		someDoc1.setTitle(title1);
		cache.put("someKey", someDoc1);
		
		String title2 = "test title 2";
		Document someDoc2 = new Document();
		someDoc2.setTitle(title2);
		Document doc = cache.putIfAbsent("someKey", someDoc2);
		assertNotNull(doc);
		assertEquals("putIfAbsent() returns the previous value", title1, doc.getTitle());
		doc = cache.get("someKey");
		assertNotNull(doc);
		assertEquals("The original item was not replaced", title1, doc.getTitle());
	}

	@Test
	public void testReplace_exists()
	{
		String title1 = "test title 1";
		Document someDoc1 = new Document();
		someDoc1.setTitle(title1);
		cache.put("someKey", someDoc1);
		
		String title2 = "test title 2";
		Document someDoc2 = new Document();
		someDoc2.setTitle(title2);
		Document doc = cache.replace("someKey", someDoc2);
		
		assertNotNull(doc);
		assertEquals("replace(key, newDoc) returns the previous doc", title1, doc.getTitle());
		doc = cache.get("someKey");
		assertNotNull(doc);
		assertEquals("The original item was replaced", title2, doc.getTitle());
	}
	
	@Test
	public void testReplace_doesNotExist()
	{
		assertNull(cache.replace("someKey", new Document()));
	}

	@Test
	public void testRemove()
	{
		String title1 = "test title 1";
		Document someDoc1 = new Document();
		someDoc1.setTitle(title1);
		cache.put("someKey", someDoc1);
		
		cache.remove("someKey");
	
		assertFalse("The item was removed from the map", cache.containsKey("someKey"));
	}

}
