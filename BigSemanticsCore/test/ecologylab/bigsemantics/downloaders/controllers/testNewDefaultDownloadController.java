package ecologylab.bigsemantics.downloaders.controllers;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import ecologylab.net.ParsedURL;

public class testNewDefaultDownloadController
{
	public static void main(String[] args)
	{
		URL url0;
		NewDefaultDownloadController controller0 = new NewDefaultDownloadController();
		Boolean success0;
		URL url1;
		NewDefaultDownloadController controller1 = new NewDefaultDownloadController();
		Boolean success1;
		URL url2;
		NewDefaultDownloadController controller2 = new NewDefaultDownloadController();
		Boolean success2;
		URL url3;
		NewDefaultDownloadController controller3 = new NewDefaultDownloadController();
		Boolean success3;
		URL url4;
		NewDefaultDownloadController controller4 = new NewDefaultDownloadController();
		Boolean success4;

		
		System.out.println("This class tests the functionality of the NewDefaultDownloadController class\n");
		
		System.out.println("Creating a URL for \"http://docs.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html\"...");
		
		try
		{
			url0 = new URL("http://docs.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html");
			
			System.out.println("Creating a ParsedURL object with the URL...");
			ParsedURL purl0 = new ParsedURL(url0);
			
			System.out.println("Creating a connection using the NewDefaultDownloadController...");
			success0 = controller0.connect(purl0);
			
			if (success0)
				System.out.println("\tConnection successfully initiated");
			else
				System.out.println("\tInitiating Connection failed!");
			
			System.out.println("Checking if the connection is good...");
			success0 = controller0.isGood();
			
			if (success0)
				System.out.println("\tConnection is good");
			else
				System.out.println("\tConnection is NOT good");
			
			System.out.println("HTTP response code:\n\t" + controller0.getStatus());
			
			System.out.println("HTTP response message:\n\t" + controller0.getStatusMessage());
			
			System.out.println("Location:\n\t" + controller0.getLocation());
			
			System.out.println("Redirected Location:\n\t" + controller0.getRedirectedLocation());
			
			System.out.println("Mime type:\n\t" + controller0.getMimeType());
			
			System.out.println("Character Set:\n\t" + controller0.getCharset());
			
			System.out.println("Content of \"Date\" header field (a test of getHeader()):\n\t" + controller0.getHeader("Date"));
			
			System.out.println("Checking if we have an InputStream...");
			if (controller0.getInputStream() != null)
				System.out.println("\tPretty sure it's working");
			else
				System.out.println("\tNope");
			
			System.out.println("\tNext Steam content:\n\t\t" + controller0.getInputStream().read());
			
		}
		catch (MalformedURLException e)
		{
			System.err.println("Creating URL failed!");
		}
		catch (IOException e)
		{
			System.err.println("Creating connection failed!");
		}		
		
		System.out.println("\n\nCreating a URL for \"http://fb.com\"...");
		
		try
		{
			url1 = new URL("http://fb.com");
			
			System.out.println("Creating a ParsedURL object with the URL...");
			ParsedURL purl1 = new ParsedURL(url1);
			
			System.out.println("Creating a connection using the NewDefaultDownloadController...");
			success1 = controller1.connect(purl1);
			
			if (success1)
				System.out.println("\tConnection successfully initiated");
			else
				System.out.println("\tInitiating Connection failed!");
			
			System.out.println("Checking if the connection is good...");
			success1 = controller1.isGood();
			
			if (success1)
				System.out.println("\tConnection is good");
			else
				System.out.println("\tConnection is NOT good");
			
			System.out.println("HTTP response code:\n\t" + controller1.getStatus());
			
			System.out.println("HTTP response message:\n\t" + controller1.getStatusMessage());
			
			System.out.println("Location:\n\t" + controller1.getLocation());
			
			System.out.println("Redirected Location:\n\t" + controller1.getRedirectedLocation());
			
			System.out.println("Mime type:\n\t" + controller1.getMimeType());
			
			System.out.println("Character Set:\n\t" + controller1.getCharset());
			
			System.out.println("Content of \"Date\" header field (a test of getHeader()):\n\t" + controller1.getHeader("Date"));
			
			System.out.println("Checking if we have an InputStream...");
			if (controller1.getInputStream() != null)
				System.out.println("\tPretty sure it's working");
			else
				System.out.println("\tNope");
			
			System.out.println("\tNext Steam content:\n\t\t" + controller1.getInputStream().read());
						
		}
		catch (MalformedURLException e)
		{
			System.err.println("Creating URL failed!");
		}
		catch (IOException e)
		{
			System.err.println("Creating connection failed!");
		}	
		
		System.out.println("\n\nCreating a URL for \"http://www.wired.com/wiredenterprise/wp-content/uploads//2012/10/ff_googleinfrastructure_large.jpg\"...");
		
		try
		{
			url2 = new URL("http://www.wired.com/wiredenterprise/wp-content/uploads//2012/10/ff_googleinfrastructure_large.jpg");
			
			System.out.println("Creating a ParsedURL object with the URL...");
			ParsedURL purl2 = new ParsedURL(url2);
			
			System.out.println("Creating a connection using the NewDefaultDownloadController...");
			success2 = controller2.connect(purl2);
			
			if (success2)
				System.out.println("\tConnection successfully initiated");
			else
				System.out.println("\tInitiating Connection failed!");
			
			System.out.println("Checking if the connection is good...");
			success2 = controller2.isGood();
			
			if (success2)
				System.out.println("\tConnection is good");
			else
				System.out.println("\tConnection is NOT good");
			
			System.out.println("HTTP response code:\n\t" + controller2.getStatus());
			
			System.out.println("HTTP response message:\n\t" + controller2.getStatusMessage());
			
			System.out.println("Location:\n\t" + controller2.getLocation());
			
			System.out.println("Redirected Location:\n\t" + controller2.getRedirectedLocation());
			
			System.out.println("Mime type:\n\t" + controller2.getMimeType());
			
			System.out.println("Character Set:\n\t" + controller2.getCharset());
			
			System.out.println("Content of \"Date\" header field (a test of getHeader()):\n\t" + controller2.getHeader("Date"));
			
			System.out.println("Checking if we have an InputStream...");
			if (controller2.getInputStream() != null)
				System.out.println("\tPretty sure it's working");
			else
				System.out.println("\tNope");
			
			System.out.println("\tNext Steam content:\n\t\t" + controller2.getInputStream().read());
			
		}
		catch (MalformedURLException e)
		{
			System.err.println("Creating URL failed!");
		}
		catch (IOException e)
		{
			System.err.println("Creating connection failed!");
		}	
		
		System.out.println("\n\nCreating a URL for \"https://code.google.com/p/git-core/source/browse/Documentation/technical/index-format.txt\"...");
		
		try
		{
			url3 = new URL("https://code.google.com/p/git-core/source/browse/Documentation/technical/index-format.txt");
			
			System.out.println("Creating a ParsedURL object with the URL...");
			ParsedURL purl3 = new ParsedURL(url3);
			
			System.out.println("Creating a connection using the NewDefaultDownloadController...");
			success3 = controller3.connect(purl3);
			
			if (success3)
				System.out.println("\tConnection successfully initiated");
			else
				System.out.println("\tInitiating Connection failed!");
			
			System.out.println("Checking if the connection is good...");
			success3 = controller3.isGood();
			
			if (success3)
				System.out.println("\tConnection is good");
			else
				System.out.println("\tConnection is NOT good");
			
			System.out.println("HTTP response code:\n\t" + controller3.getStatus());
			
			System.out.println("HTTP response message:\n\t" + controller3.getStatusMessage());
			
			System.out.println("Location:\n\t" + controller3.getLocation());
			
			System.out.println("Redirected Location:\n\t" + controller3.getRedirectedLocation());
			
			System.out.println("Mime type:\n\t" + controller3.getMimeType());
			
			System.out.println("Character Set:\n\t" + controller3.getCharset());
			
			System.out.println("Content of \"Date\" header field (a test of getHeader()):\n\t" + controller3.getHeader("Date"));
			
			System.out.println("Checking if we have an InputStream...");
			if (controller3.getInputStream() != null)
				System.out.println("\tPretty sure it's working");
			else
				System.out.println("\tNope");
			
			System.out.println("\tNext Steam content:\n\t\t" + controller3.getInputStream().read());
			
		}
		catch (MalformedURLException e)
		{
			System.err.println("Creating URL failed!");
		}
		catch (IOException e)
		{
			System.err.println("Creating connection failed!");
		}	
		
		System.out.println("\n\nCreating a URL for \"http://docs.orFAILacle.com/javase/6/docs/api/java/net/HttpURLConnection.html\"...");
		
		try
		{
			url4 = new URL("http://docs.orFAILacle.com/javase/6/docs/api/java/net/HttpURLConnection.html");
			
			System.out.println("Creating a ParsedURL object with the URL...");
			ParsedURL purl4 = new ParsedURL(url4);
			
			System.out.println("Creating a connection using the NewDefaultDownloadController...");
			success4 = controller4.connect(purl4);
			
			if (success4)
				System.out.println("\tConnection successfully initiated");
			else
				System.out.println("\tInitiating Connection failed!");
			
			System.out.println("Checking if the connection is good...");
			success4 = controller4.isGood();
			
			if (success4)
				System.out.println("\tConnection is good");
			else
				System.out.println("\tConnection is NOT good");
			
			System.out.println("HTTP response code:\n\t" + controller4.getStatus());
			
			System.out.println("HTTP response message:\n\t" + controller4.getStatusMessage());
			
			System.out.println("Location:\n\t" + controller4.getLocation());
			
			System.out.println("Redirected Location:\n\t" + controller4.getRedirectedLocation());
			
			System.out.println("Mime type:\n\t" + controller4.getMimeType());
			
			System.out.println("Character Set:\n\t" + controller4.getCharset());
			
			System.out.println("Content of \"Date\" header field (a test of getHeader()):\n\t" + controller4.getHeader("Date"));
			
			System.out.println("Checking if we have an InputStream...");
			if (controller4.getInputStream() != null)
				System.out.println("\tPretty sure it's working");
			else
				System.out.println("\tNope");
			
			System.out.println("\tNext Steam content:\n\t\t" + controller4.getInputStream().read());
			
		}
		catch (MalformedURLException e)
		{
			System.err.println("Creating URL failed!");
		}
		catch (IOException e)
		{
			System.err.println("Creating connection failed!");
		}	
	}
}