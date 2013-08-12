package ecologylab.bigsemantics.documentcache;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.ClientProtocolException;

import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.MetadataDeserializationHookStrategy;

/** 
 * Implements the ISimplCache interface using CouchDB
 * 
 * @author colton
 *
 */
public class SimplCouchDBCache implements ISimplCache
{
	private DefaultHttpClient client;

	private SemanticsSessionScope sss = null;
	private SimplTypesScope metadataTypesScope;

	private static String host = "localhost";
	private static String port = "5984";
	private static String database = "exampledb";

	public static SimplCouchDBCache simplCouchDBCacheFactory(SemanticsSessionScope sss)
	{
		SimplCouchDBCache cache = new SimplCouchDBCache();
		
		cache.setSemanticsSessionScope(sss);
		
		return cache;
	}
	
	private SimplCouchDBCache()
	{
		client = new DefaultHttpClient();
	}

	/**
	 * Sets the SemanticsSessionScope
	 * 
	 * @param sss
	 */
	public void setSemanticsSessionScope(SemanticsSessionScope sss)
	{
		this.sss = sss;
		
		metadataTypesScope = sss.getMetadataTypesScope();
	}
	
	/**
	 * Returns the SemanticsSessionScope
	 * 
	 * @return the SemanticsSessionScope
	 */
	public SemanticsSessionScope getSemanticsSessionScope()
	{
		return sss;
	}
	
	/**
	 * Gets the object mapped to the specified key. Returns <code>null</code> if
	 * the key is not mapped to an object
	 * 
	 * @param key
	 *            the key to search by
	 * @return the object mapped to the specified key, or <code>null</code> if
	 *         no mapping exists
	 */
	@Override
	public boolean containsKey(String key)
	{
		boolean isContained = false;
		HttpResponse response = null;
		HttpHead headRequest = new HttpHead("http://" + host + ":" + port + "/"
				+ database + "/" + key);

		try
		{
			response = client.execute(headRequest);
			
			isContained = (response.getStatusLine().getStatusCode() == 200);		
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			EntityUtils.consumeQuietly(response.getEntity());	
		}
		
		return isContained;
	}

	/**
	 * Gets the object mapped to the specified key. Returns <code>null</code> if
	 * the key is not mapped to an object
	 * 
	 * @param key
	 *            the key to search by
	 * @return the object mapped to the specified key, or <code>null</code> if
	 *         no mapping exists
	 */
	public Object get(String key)
	{
		String temp;
		String readerOutput = null;
		Object obj;
		Document doc = null;
		HttpResponse response = null;
		HttpGet getRequest = new HttpGet("http://" + host + ":" + port + "/"
				+ database + "/" + key);

		try
		{
			response = client.execute(getRequest);
			
			if (response.getStatusLine().getStatusCode() != 200)
			{
				throw new RuntimeException("ERROR: HTTP status code "
						+ response.getStatusLine().getStatusCode());
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));

			while ((temp = reader.readLine()) != null)
				readerOutput += temp;

			// Stripping off CouchDB ID and revision number
			readerOutput = "{\"" + readerOutput.split("\"", 10)[9];

			MetadataDeserializationHookStrategy deserializationStrategy 
				= new MetadataDeserializationHookStrategy(sss);
			obj = metadataTypesScope.deserialize(readerOutput,
					deserializationStrategy, StringFormat.JSON);
			
			if (obj instanceof Document)
			{
				doc = (Document) obj;
			}
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			EntityUtils.consumeQuietly(response.getEntity());	
		}

		return doc;
	}

	/**
	 * Gets the object mapped to the specified key. Returns <code>null</code> if
	 * the key is not mapped to an object
	 * 
	 * @param key
	 *            the key to search by
	 * @param revision 
	 * 			  the revision number to retrieve
	 * @return the object mapped to the specified key, or <code>null</code> if
	 *         no mapping exists
	 */
	public Object get(String key, String revision)
	{
		String temp;
		String readerOutput = null;
		Object obj;
		Document doc = null;
		HttpResponse response = null;
		HttpGet getRequest = new HttpGet("http://" + host + ":" + port + "/"
				+ database + "/" + key + "?rev=" + revision);

		try
		{
			response = client.execute(getRequest);
			
			if (response.getStatusLine().getStatusCode() != 200)
			{
				throw new RuntimeException("ERROR: HTTP status code "
						+ response.getStatusLine().getStatusCode());
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));

			while ((temp = reader.readLine()) != null)
				readerOutput += temp;

			// Stripping off CouchDB ID and revision number
			readerOutput = "{\"" + readerOutput.split("\"", 10)[9];

			MetadataDeserializationHookStrategy deserializationStrategy 
				= new MetadataDeserializationHookStrategy(sss);
			obj = metadataTypesScope.deserialize(readerOutput,
					deserializationStrategy, StringFormat.JSON);
			
			if (obj instanceof Document)
			{
				doc = (Document) obj;
			}
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			EntityUtils.consumeQuietly(response.getEntity());	
		}

		return doc;
	}

	/**
	 * Maps the specified key to the object within the cache
	 * 
	 * @param key
	 *            the key to map the object to
	 * @param obj
	 *            the object to be added to the cache
	 */
	@Override
	public void put(String key, Object obj)
	{
		HttpResponse response = null;
		HttpPut putRequest = new HttpPut("http://" + host + ":" + port + "/"
				+ database + "/" + key);

		try
		{
			putRequest.setEntity(new StringEntity(SimplTypesScope.serialize(
					obj, StringFormat.JSON).toString()));

			response = client.execute(putRequest);
			
			if (response.getStatusLine().getStatusCode() != 201)
			{
				throw new RuntimeException("ERROR: HTTP status code "
						+ response.getStatusLine().getStatusCode());
			}
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}

	/**
	 * Checks first if the given key is already associated with an object. If no
	 * mapping exists, the object is added to the cache and a mapping is created
	 * 
	 * @param key
	 *            the key to search by
	 * @param obj
	 *            the Simpl-serializable object to potentially be added to the cache
	 * @return the object mapped to the key
	 */
	@Override
	public Object putIfAbsent(String key, Object obj)
	{
		Object result = null;
		
		if (!containsKey(key))
		{
			put(key, obj);
			
			result = obj;
		}

		return result;
	}

	/**
	 * TODO This does not work - need to be able to compare oldObj to object in database
	 * Replaces the object which a key is mapped to, only if the key is mapped
	 * to the object also indicated in the call
	 * 
	 * @param key
	 *            the key to search by
	 * @param oldObj
	 *            the Simpl-serializable object which is thought to be associated with the
	 *            key
	 * @param newObj
	 *            the Simpl-serializable object with which to replace the original object with if
	 *            conditions are satisfied
	 * @return the object mapped to the key, or
	 *         <code>null</code> if no previous mapping existed
	 */
	@Override
	public boolean replace(String key, Object oldObj, Object newObj)
	{
		boolean wasReplaced = false;

		//if (oldObj = object in database @ key)
		//{
		//	replace(key, newObj);
		//	wasReplaced = true;
		//}

		return wasReplaced;
	}

	/**
	 * Replaces the entry for a key if it is already mapped to some other entry
	 * 
	 * @param key
	 *            the key to search by
	 * @param newObj
	 *            the object to be associated with the key
	 * @return the previous object that was mapped to the key, or
	 *         <code>null</code> if no previous mapping existed
	 */
	@Override
	public Object replace(String key, Object newObj)
	{	
		boolean wasReplaced = false;
		HttpResponse response = null;
		HttpPut putRequest = new HttpPut("http://" + host + ":" + port + "/"
				+ database + "/" + key);
		
		if (containsKey(key))
		{
			try
			{
				putRequest.setEntity(new StringEntity("{\"_id\":\"" + key
						+ "\",\"_rev\":\"" + getRevisionNumber(key) + "\","
						+ SimplTypesScope.serialize(newObj, StringFormat.JSON)
								.toString().substring(1)));

				response = client.execute(putRequest);
				
				if (response.getStatusLine().getStatusCode() != 201)
				{
					throw new RuntimeException("ERROR: HTTP status code "
							+ response.getStatusLine().getStatusCode());
				}
			}
			catch (SIMPLTranslationException e)
			{
				e.printStackTrace();
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				EntityUtils.consumeQuietly(response.getEntity());
			}
			
			wasReplaced = true;
		}
		
		return wasReplaced ? get(key) : null;
	}

	/**
	 * Removes the key and entry pair from the cache, if the key exists
	 * 
	 * @param key
	 *            the key to search by
	 */
	@Override
	public void remove(String key)
	{
		if (containsKey(key))
		{
			HttpResponse response = null;
			HttpDelete deleteRequest = new HttpDelete("http://" + host + ":"
					+ port + "/" + database + "/" + key + "?rev="
					+ getRevisionNumber(key));

			try
			{
				response = client.execute(deleteRequest);
				
				if (response.getStatusLine().getStatusCode() != 200)
				{
					throw new RuntimeException("ERROR: HTTP status code "
							+ response.getStatusLine().getStatusCode());
				}
			}
			catch (ClientProtocolException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}
	}

	/**
	 * TODO This does not work - need to be able to compare oldObj to object in database
	 * Should remove the key and entry from the cache, only if the key maps to the
	 * object also given in the call
	 * 
	 * @param key
	 *            the key to search by
	 * @param oldObj
	 *            the object which is expected to be associated with the key
	 * @return a boolean indicating if the conditions were met, and a deletion
	 *         occurred
	 */
	@Override
	public boolean remove(String key, Object oldObj)
	{
		boolean wasRemoved = false;

		if (containsKey(key) && get(key) == oldObj)
		{
			remove(key);

			wasRemoved = true;
		}

		return wasRemoved;
	}

	/**
	 * Helper method to support instances where CouchDB requires the most recent revision number to manipulate documents
	 * 
	 * @param key
	 * @return the most recent revision number associated with the document linked to the key
	 */
	private String getRevisionNumber(String key)
	{
		String temp;
		String readerOutput = null;
		HttpResponse response = null;
		HttpGet getRequest = new HttpGet("http://" + host + ":" + port + "/"
				+ database + "/" + key);

		try
		{
			response = client.execute(getRequest);
			
			if (response.getStatusLine().getStatusCode() != 200)
			{
				throw new RuntimeException("ERROR: HTTP status code "
						+ response.getStatusLine().getStatusCode());
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));

			while ((temp = reader.readLine()) != null)
				readerOutput += temp;
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			EntityUtils.consumeQuietly(response.getEntity());
		}

		// Split output on '"', select revision number
		return readerOutput.split("\"", 9)[7];
	}
}