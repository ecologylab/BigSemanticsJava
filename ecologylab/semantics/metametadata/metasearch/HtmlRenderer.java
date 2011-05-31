package ecologylab.semantics.metametadata.metasearch;

import java.io.IOException;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.serialization.SIMPLTranslationException;

public class HtmlRenderer
{

	private String	title;

	private String	header;

	private String	styleSheet;

	private String	jQuery	= "<script language=\"JavaScript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js\"></script>";

	private String	javascript;

	public HtmlRenderer(String title, String header, String styleSheet, String javascript)
	{
		this.title = title;
		this.header = header;
		this.styleSheet = styleSheet;
		this.javascript = javascript;
	}

	public void appendHeader(Appendable a) throws IOException
	{
		a.append("<html>\n");
		a.append("  <head>\n");
		a.append("    <title>").append(title).append("</title>\n");
		a.append("    <link href=\"").append(styleSheet)
				.append("\" rel=\"stylesheet\" type=\"text/css\" />\n");
		a.append("		").append(jQuery).append('\n');
		a.append("    <script language=\"JavaScript\" src=\"").append(javascript)
				.append("\"> </script>");
		a.append("  </head>\n");
		a.append("  <body>\n");
		a.append("    <h1 class=\"header\">").append(header).append("</h1>\n");
		a.append("    <div id=\"results\">\n");
	}

	public void appendItem(Metadata item, Appendable a) throws IOException
	{
		try
		{
			a.append("      ");
			item.serializeToHtml(a, item.getMetaMetadata().createGraphContext());
			a.append("\n");
			for (String linkedMetadataKey : item.getLinkedMetadataKeys())
			{
				Metadata linkedMetadata = item.getLinkedMetadata(linkedMetadataKey);
				if (linkedMetadata != null)
				{
					a.append("      <div class=\"linked_metadata ").append(linkedMetadataKey).append("\">");
					linkedMetadata.serializeToHtml(a, linkedMetadata.getMetaMetadata().createGraphContext());
					a.append("</div>\n");
				}
			}
			a.append("\n");
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
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void appendFooter(Appendable a) throws IOException
	{
		a.append("    </div>\n");
		a.append("    <div class=\"footer\">Powered by Meta-Metadata, Interface Ecology Lab</div>\n");
		a.append("  ").append("</body>\n");
		a.append("</html>\n");
	}

}
