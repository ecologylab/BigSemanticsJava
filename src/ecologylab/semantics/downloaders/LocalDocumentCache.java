/**
 * 
 */
package ecologylab.semantics.downloaders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ecologylab.generic.Debug;
import ecologylab.io.Files;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.metadata.builtins.Document;

/**
 * url connection to local file store
 * 
 * @author andruid
 * @author ajit
 *
 */

public class LocalDocumentCache extends Debug implements SemanticActionsKeyWords
{

	private Document							document;

	private SemanticsGlobalScope	semanticsScope;

	private PURLConnection				purlConnection;

	private DocumentParser				documentParser;

	public LocalDocumentCache(Document document)
	{
		this.document = document;
		this.semanticsScope = document.getSemanticsScope();
	}

	public void connect() throws IOException
	{
		ParsedURL originalPURL = document.getDownloadLocation();
		purlConnection = new PURLConnection(originalPURL);

		if (originalPURL.isFile())
		{
			File file = originalPURL.file();

			// Handle localhost issues on mac?
			if ("localhost".equals(originalPURL.url().getAuthority()))
			{
				String s = originalPURL.url().toString();
				s = s.replaceFirst("localhost", "");
				ParsedURL newPURL = new ParsedURL(new URL(s));
				purlConnection = new PURLConnection(newPURL);
				document.setLocation(newPURL);
				file = newPURL.file();
			}

			if (!file.exists())
			{
				// this might be pointing to an entry in a ZIP file, e.g. the packed composition file.
				File ancestor = Files.findFirstExistingAncestor(file);
				if (ancestor != null && !ancestor.isDirectory() && Files.isZipFile(ancestor))
				{
					// read from a ZIP file, which should be the packed composition file
					String entryName = ancestor.toURI().relativize(file.toURI()).toString();
					entryName = URLDecoder.decode(entryName, "utf-8");
					ZipFile zipFile = new ZipFile(ancestor);
					ZipEntry entry = zipFile.getEntry(entryName);
					if (entry == null)
					{
						warning("No zip entry found: " + entryName);
					}
					else
					{
						InputStream in = zipFile.getInputStream(entry);
						purlConnection.streamConnect(in);
					}
				}
			}
			else if (file.isDirectory())
			{
				// FileDirectoryParser
				documentParser = DocumentParser.getParserInstanceFromBindingMap(FILE_DIRECTORY_PARSER,
						semanticsScope);
			}
			else
			{
				purlConnection.fileConnect();
				// we already have the correct meta-metadata, having used suffix to construct, or having
				// gotten it from a restore.
			}
		}
	}

	public PURLConnection getPurlConnection()
	{
		return purlConnection;
	}

	public DocumentParser getDocumentParser()
	{
		return documentParser;
	}
}
