package ecologylab.bigsemantics.metametadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class TestUsingRepositoryAsResources {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		URL url = TestUsingRepositoryAsResources.class.getResource("/mmdrepository/primitives.xml");
		if (url != null)
		{
			InputStream in = url.openStream();
			String s = streamToString(in);
			System.out.println(s);
		}
		
		processResourceDir("/mmdrepository");
		
	}
	
	public static String streamToString(InputStream stream) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(stream);
		char[] buf = new char[4096]; int n = 0;
		while (true)
		{
			n = reader.read(buf);
			if (n < 0)
				break;
			sb.append(buf, 0, n);
		}
		reader.close();
		return sb.toString();
	}
	
	public static void processResourceDir(String resourceDirName) throws IOException
	{
		System.out.println("processing resource dir: " + resourceDirName);
		
		URL url = TestUsingRepositoryAsResources.class.getResource(resourceDirName);
		System.out.println("URL: " + url);
		InputStream in = url.openStream();
		if (in == null)
		{
			System.err.println("NULL STREAM!");
			URLConnection conn = url.openConnection();
			if (conn == null)
			{
				System.err.println("NULL CONNECTION!");
				conn.connect();
				in = conn.getInputStream();
			}
		}
		String list = streamToString(in);
//		System.out.println(list);
		String[] rootDirReses = list.split("\n");
		for (String rootDirRes : rootDirReses)
		{
			String kidResName = resourceDirName + "/" + rootDirRes;
			if (rootDirRes.endsWith(".xml"))
			{
				URL kidResUrl = TestUsingRepositoryAsResources.class.getResource(kidResName);
				if (kidResUrl != null)
					System.out.println("resource found: " + kidResName);
				else
					System.out.println("!!! resource not found: " + kidResName);
			}
			else if (!rootDirRes.contains("."))
			{
				processResourceDir(kidResName);
			}
		}
	}
	
}
