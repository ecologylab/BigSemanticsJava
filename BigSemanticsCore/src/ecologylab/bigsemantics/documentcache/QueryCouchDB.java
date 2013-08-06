package ecologylab.bigsemantics.documentcache;
 
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

public class QueryCouchDB
{
	private static String host = "ecoarray0";
	private static String port = "9084";
	private static String database = "exdb";
	
	@GET
	@Produces("application/JSON")
	@Path("/query_field_values.json")
	public Response queryFieldValues(
			@QueryParam("callback") String callback,
			@QueryParam("type") String type,
			@QueryParam("field") String field)
	{
		type = type.replaceAll("[^a-zA-Z0-9_]", "");
		field = field.replaceAll("[^a-zA-Z0-9_]", "");
		
		int statusCode;
		Response response = null;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		HttpGet getRequest = new HttpGet("http://" + host + ":" + port + "/"
				+ database + "/_design/query_field_values/_view/by_" + field + "?key=\"" + type + "\"");
 
		try
		{
			httpResponse = client.execute(getRequest);
			
			statusCode = httpResponse.getStatusLine().getStatusCode();
			
			if (statusCode == 200)
				response = Response.status(Status.OK).entity(callback + ", " + EntityUtils.toString(httpResponse.getEntity()) + ");").build();
			else
				response = Response.status(Status.SERVICE_UNAVAILABLE).entity(null).type(MediaType.TEXT_PLAIN).build();
				
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(null).type(MediaType.TEXT_PLAIN).build();		
		}
		
		return response;
	}
 
	@GET
	@Produces("application/JSON")
	@Path("/query_metadata_value.json")
	public Response queryMetadata(
			@QueryParam("callback") String callback,
			@QueryParam("type") String type,
			@QueryParam("field") String field, 
			@QueryParam("value") String value)
	{
		type = type.replaceAll("[^a-zA-Z0-9_]", "");
		field = field.replaceAll("[^a-zA-Z0-9_]", "");
		value = value.replaceAll("[^a-zA-Z0-9_]", "");
		
		int statusCode;
		Response response = null;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		HttpGet getRequest = new HttpGet("http://" + host + ":" + port + "/"
				+ database + "/_design/query_metadata_value/_view/general?key=[\"" + type + "\",\"" + field + "\",\"" + value + "\"]");
  
		try
		{
			httpResponse = client.execute(getRequest);
			
			statusCode = httpResponse.getStatusLine().getStatusCode();
			
			if (statusCode == 200)
				response = Response.status(Status.OK).entity(callback + ", " + EntityUtils.toString(httpResponse.getEntity()) + ");").build();
			else
				response = Response.status(Status.SERVICE_UNAVAILABLE).entity(null).type(MediaType.TEXT_PLAIN).build();
				
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(null).type(MediaType.TEXT_PLAIN).build();		
		}
		
		return response;
	}
 
	@GET
	@Produces("application/JSON")
	@Path("/query_metadata_range.json")
	public Response queryFieldValues(
			@QueryParam("callback") String callback,
			@QueryParam("type") String type,
			@QueryParam("field") String field, 
			@QueryParam("lower") double min,
			@QueryParam("upper") double max)
	{
		type = type.replaceAll("[^a-zA-Z0-9_]", "");
		field = field.replaceAll("[^a-zA-Z0-9_]", "");
		
		int statusCode;
		Response response = null;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		HttpGet getRequest = new HttpGet("http://" + host + ":" + port + "/"
				+ database + "/_design/query_metadata_value/_view/general?startkey=[\"" + type + "\",\"" + field + "\",\"" + min + "\"]&endkey=[\"" + type + "\",\"" + field + "\",\"" + max + "\"]");
  
		try
		{
			httpResponse = client.execute(getRequest);
			
			statusCode = httpResponse.getStatusLine().getStatusCode();
			
			if (statusCode == 200)
				response = Response.status(Status.OK).entity(callback + ", " + EntityUtils.toString(httpResponse.getEntity()) + ");").build();
			else
				response = Response.status(Status.SERVICE_UNAVAILABLE).entity(null).type(MediaType.TEXT_PLAIN).build();				
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(null).type(MediaType.TEXT_PLAIN).build();		
		}
		
		return response;
	}
 
/*	@GET
	@Produces("application/JSON")
	@Path("/sort_linked_metadata.json")
	public Response sortLinkedMetadata(
			@QueryParam("source_metadata") String url,
			@QueryParam("linkage") String collectionFieldName,
			@QueryParam("field") String field)
	{
		//type = type.replaceAll("[^a-zA-Z0-9_]", "");
		//field = field.replaceAll("[^a-zA-Z0-9_]", "");
		//value = value.replaceAll("[^a-zA-Z0-9_]", "");
		
		int statusCode;
		Response response = null;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		HttpGet getRequest = new HttpGet("http://" + host + ":" + port + "/"
				+ database + "/_design/docs/" + field + "/by_type?key=" + type);
 
		try
		{
			httpResponse = client.execute(getRequest);
			
			if (httpResponse.getStatusLine().getStatusCode() != 200)
				throw new RuntimeException("ERROR: HTTP status code "
						+ httpResponse.getStatusLine().getStatusCode());
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return response;
	}
*/ 
	@GET
	@Produces("application/JSON")
	@Path("/query_metadata_keyword.json")
	public Response queryMetadataKeyword(
			@QueryParam("callback") String callback,
			@QueryParam("keyword") String keyword)
	{

		keyword = keyword.replaceAll("[^a-zA-Z0-9_]", "");
		
		int statusCode;
		Response response = null;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		HttpGet getRequest = new HttpGet("http://" + host + ":" + port + "/"
				+ database + "/_design/query_metadata_keyword/_view/by_keyword?key=\"" + keyword + "\"");
  
		try
		{
			httpResponse = client.execute(getRequest);
			
			statusCode = httpResponse.getStatusLine().getStatusCode();
			
			if (statusCode == 200)
				response = Response.status(Status.OK).entity(callback + ", " + EntityUtils.toString(httpResponse.getEntity()) + ");").build();
			else
				response = Response.status(Status.SERVICE_UNAVAILABLE).entity(null).type(MediaType.TEXT_PLAIN).build();				
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(null).type(MediaType.TEXT_PLAIN).build();		
		}
		
		return response;
	}
	
/*	@GET
	@Produces("application/JSON")
	@Path("/query_metadata_keyword_advanced.json")
	public Response queryMetadataKeyword(
			@QueryParam("type") String type,
			@QueryParam("field") String field,
			@QueryParam("keyword") String keyword)
	{
		type = type.replaceAll("[^a-zA-Z0-9_]", "");
		field = field.replaceAll("[^a-zA-Z0-9_]", "");
		keyword = keyword.replaceAll("[^a-zA-Z0-9_]", "");
		
		int statusCode;
		Response response = null;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		HttpGet getRequest = new HttpGet("http://" + host + ":" + port + "/"
				+ database + "/_design/query_metadata_value_keyword/_view/general?key=[\"" + type + "\",\"" + field + "\",\"" + keyword + "\"]");
  
		try
		{
			httpResponse = client.execute(getRequest);
			
			if (httpResponse.getStatusLine().getStatusCode() != 200)
				throw new RuntimeException("ERROR: HTTP status code "
						+ httpResponse.getStatusLine().getStatusCode());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return response;
	}
*/
}