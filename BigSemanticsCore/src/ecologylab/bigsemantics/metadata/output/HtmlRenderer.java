package ecologylab.bigsemantics.metadata.output;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.generic.Debug;
import ecologylab.serialization.SIMPLTranslationException;

public class HtmlRenderer extends Debug
{

	private String	title;

	private String	header;

	private String	styleSheet;

	private String	jQuery	= "<script language=\"JavaScript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js\"></script>";

	private String	javascript;

	private boolean	bad;

	File						file;

	PrintWriter			fileWriter;

	public HtmlRenderer(File file, String title, String header)
	{
		this(file, title, header, "mixed_search.css", "linking_metadata.js");
	}

	public HtmlRenderer(File file, String title, String header, String styleSheet, String javascript)
	{
		this.file = file;
		debug("Opening " + file.getAbsolutePath());
		try
		{
			fileWriter = new PrintWriter(file);
			setup(title, header, styleSheet, javascript);
			appendHeader(fileWriter);
		}

		catch (Exception e)
		{
			e.printStackTrace();
			bad = true;
		}
	}

	public HtmlRenderer(String title, String header, String styleSheet, String javascript)
	{
		setup(title, header, styleSheet, javascript);
	}

	/**
	 * @param title
	 * @param header
	 * @param styleSheet
	 * @param javascript
	 */
	public void setup(String title, String header, String styleSheet, String javascript)
	{
		this.title = title;
		this.header = header;
		this.styleSheet = styleSheet;
		this.javascript = javascript;
	}

	public void appendHeader(Appendable appendable) throws IOException
	{
		appendable.append("<html>\n");
		appendable.append("  <head>\n");
		appendable.append("    <title>").append(title).append("</title>\n");
		appendable.append("    <link href=\"").append(styleSheet) .append("\" rel=\"stylesheet\" type=\"text/css\" />\n");
		appendable.append("		").append(jQuery).append('\n');
		appendable.append("    <script language=\"JavaScript\" src=\"").append(javascript) .append("\"> </script>");
		appendable.append("  </head>\n");
		appendable.append("  <body>\n");
		appendable.append("    <h1 class=\"header\">").append(header).append("</h1>\n");
		appendable.append("    <div id=\"results\">\n");
	}

	public boolean appendMetadata(Metadata metadata)
	{
		try
		{
			appendMetadata(metadata, fileWriter);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			bad = true;
		}
		return bad;
	}

	public void appendMetadata(Metadata metadata, Appendable a) throws IOException,
			IllegalArgumentException, IllegalAccessException, SIMPLTranslationException
	{
		a.append("      ");
		metadata.serializeToHtml(a, metadata.getMetaMetadata().createGraphContext());
		a.append("\n");
		for (String linkedMetadataKey : metadata.getLinkedMetadataKeys())
		{
			Metadata linkedMetadata = metadata.getLinkedMetadata(linkedMetadataKey);
			if (linkedMetadata != null)
			{
				a.append("      <div class=\"linked_metadata ").append(linkedMetadataKey).append("\">");
				linkedMetadata.serializeToHtml(a, linkedMetadata.getMetaMetadata().createGraphContext());
				a.append("</div>\n");
			}
		}
		a.append("\n");
	}

	public void appendFooter(Appendable a) throws IOException
	{
		a.append("    </div>\n");
		a.append("    <div class=\"footer\">Powered by Meta-Metadata, Interface Ecology Lab</div>\n");
		a.append("  ").append("</body>\n");
		a.append("</html>\n");
	}

	public boolean close()
	{
		try
		{
			appendFooter(fileWriter);
			fileWriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			bad = true;
		}
		debug("Closed " + file.getAbsolutePath());
		return bad;
	}

	/**
	 * @return the bad
	 */
	public boolean isBad()
	{
		return bad;
	}

}
