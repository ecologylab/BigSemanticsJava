package ecologylab.semantics.filestorage;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import ecologylab.net.ParsedURL;

public class SHA256FileNameGenerator
{
	public static String getName(ParsedURL originalPURL)
	{
		String OUT_PREFIX		= "nwDownloaded";
		//String OUT_SUFFIX = ".html";

		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance("SHA-256");
			md.update(originalPURL.toString().getBytes("UTF-8")); // Change this to "UTF-16" if needed
			byte[] digest = md.digest();
			String hashStr = Arrays.toString(digest);
			
			OUT_PREFIX = hashStr.substring(1, (hashStr.length()-1));
			OUT_PREFIX= OUT_PREFIX.replace(", ", "");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		//suffix based on contentType
		//if (mimeType != null && mimeType.equals("text/xml"))
			//OUT_SUFFIX = ".xml";
		
		String OUT_NAME	= OUT_PREFIX; //+ OUT_SUFFIX;
		return OUT_NAME;
	}
}
