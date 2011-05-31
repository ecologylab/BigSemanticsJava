package ecologylab.semantics.metametadata.test;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Debug;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;

public class MmToHtmlTest extends NewMmTest
{
	PrintStream	print;

	public MmToHtmlTest(OutputStream outputStream)
	{
		super(outputStream);
		outputOneAtATime = false;
		try
		{
			print = new PrintStream("MmToHtml.html");
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args)
	{
		TranslationScope.graphSwitch = GRAPH_SWITCH.ON;
		MmToHtmlTest test = new MmToHtmlTest(System.out);
		test.collect(args);
	}

	public void generateHtml() throws IllegalArgumentException, IllegalAccessException, IOException,
			SIMPLTranslationException, URISyntaxException
	{
		appendHeader(print);
		Desktop desktop = Desktop.getDesktop();
		URI uri = new URI("MmtoHtml.html");
		for (DocumentClosure documentClosure : documentCollection)
		{
			Document document = documentClosure.getDocument();

			document.serializeToHtml(print, document.createGraphContext());
		}
		appendFooter(print);
		desktop.browse(uri);

	}

	public void appendHeader(Appendable a) throws IOException
	{
		String jQuery = "<script language=\"JavaScript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js\"></script>";

		a.append("<html>\n");
		a.append("  <head>\n");
		a.append("    <title>").append("HTML Rendering of Metadata").append("</title>\n");
		a.append("    <link href=\"").append("MmToHtml.css")
				.append("\" rel=\"stylesheet\" type=\"text/css\" />\n");
		a.append("		").append(jQuery).append('\n');
		a.append("    <script language=\"JavaScript\" src=\"").append("MmToHtml.js")
				.append("\"> </script>");
		a.append("  </head>\n");
		a.append("  <body>\n");
		a.append("    <h1 class=\"header\">").append("Rendered HTML").append("</h1>\n");
		a.append("    <div id=\"data\">\n");
	}

	public void appendFooter(Appendable a) throws IOException
	{
		a.append("    </div>\n");
		a.append("    <div class=\"footer\">Powered by Meta-Metadata, Interface Ecology Lab</div>\n");
		a.append("  ").append("</body>\n");
		a.append("</html>\n");
	}

	@Override
	public void delivery(DocumentClosure incomingClosure)
	{
		if (outputOneAtATime)
			incomingClosure.serialize(outputStream);
		else if (++currentResult == documentCollection.size())
		{
			System.out.println("\n\n");
			try
			{
				generateHtml();
			}
			catch (IllegalArgumentException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (SIMPLTranslationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (URISyntaxException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			downloadMonitor.stop();
		}
	}

}
