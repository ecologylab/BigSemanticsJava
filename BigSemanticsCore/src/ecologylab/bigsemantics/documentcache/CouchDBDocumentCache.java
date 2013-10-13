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
import ecologylab.bigsemantics.collecting.DocumentCache;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.MetadataDeserializationHookStrategy;

/** 
 * A Document cache using CouchDB as the backend.
 * 
 * @author colton
 *
 */
public class CouchDBDocumentCache implements DocumentCache<String, Document>
{

	private DefaultHttpClient client;

	private SemanticsSessionScope sss = null;
	private SimplTypesScope metadataTypesScope;

	private static String host = "localhost";
	private static String port = "5984";
	private static String database = "exampledb";

	public static CouchDBDocumentCache simplCouchDBCacheFactory(SemanticsSessionScope sss)
	{
		CouchDBDocumentCache cache = new CouchDBDocumentCache();
		
		cache.setSemanticsSessionScope(sss);
		
		return cache;
	}
	
	private CouchDBDocumentCache()
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

  @Override
	public Document get(String key)
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

	@Override
	public Document get(String key, String revision)
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

	@Override
	public void put(String key, Document obj)
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

	@Override
	public Document putIfAbsent(String key, Document obj)
	{
		Document result = null;
		
		if (!containsKey(key))
		{
			put(key, obj);
			
			result = obj;
		}

		return result;
	}

  /**
   * TODO This does not work - need to be able to compare oldObj to object in database Replaces the
   * object which a key is mapped to, only if the key is mapped to the object also indicated in the
   * call
   */
	@Override
	public boolean replace(String key, Document oldObj, Document newObj)
	{
		boolean wasReplaced = false;

		//if (oldObj = object in database @ key)
		//{
		//	replace(key, newObj);
		//	wasReplaced = true;
		//}

		return wasReplaced;
	}

	@Override
	public Document replace(String key, Document newObj)
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

	@Override
	public boolean remove(String key, Document oldObj)
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
   * Helper method to support instances where CouchDB requires the most recent revision number to
   * manipulate documents
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