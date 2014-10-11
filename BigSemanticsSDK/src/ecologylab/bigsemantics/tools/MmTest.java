/**
 * 
 */
package ecologylab.bigsemantics.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.bigsemantics.collecting.MetaMetadataRepositoryInit;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTypesScope;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metadata.builtins.RichArtifact;
import ecologylab.bigsemantics.metadata.builtins.creativeWork.Curation;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.Format;

/**
 * Basic program for testing meta-metadata. Takes a set of locations as arguments as input. (Input
 * parsing is terminated by a // comment symbol).
 * 
 * Uses semantics to download and parse each input. Sends output to the console as XML.
 * 
 * Also construct an information_composition_declaration object. Add each metadata that gets constructed / extracted into it.
 * Write this file to the temp directory (Java's idea of where you can write).
 * Report this file's path on the console, so you can paste into Chrome and browse it.
 * 
 * @author andruid
 */
public class MmTest extends SingletonApplicationEnvironment implements
		Continuation<DocumentClosure>
{
	ArrayList<DocumentClosure>			documentCollection	= new ArrayList<DocumentClosure>();

	int															currentResult;

	protected boolean								outputOneAtATime;

	OutputStream										outputStream;

	protected SemanticsSessionScope	semanticsSessionScope;
	
	protected Curation curation = new Curation();

	public MmTest(String appName) throws SIMPLTranslationException
	{
		this(appName, System.out);
	}

	public MmTest(String appName, OutputStream outputStream) throws SIMPLTranslationException
	{
		this(appName, outputStream, RepositoryMetadataTypesScope.get());
	}

	public MmTest(String appName, OutputStream outputStream,
			SimplTypesScope metadataTranslationScope) throws SIMPLTranslationException
	{
		this(appName, outputStream, metadataTranslationScope, null,
				MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FORMAT);
	}

	public MmTest(String appName, File repositoryLocation, Format repositoryFormat)
			throws SIMPLTranslationException
	{
		this(appName, System.out, repositoryLocation, repositoryFormat);
	}

	public MmTest(String appName, OutputStream outputStream, File repositoryLocation,
			Format repositoryFormat) throws SIMPLTranslationException
	{
		this(appName, outputStream, RepositoryMetadataTypesScope.get(), repositoryLocation,
				repositoryFormat);
	}

	public MmTest(String appName, OutputStream outputStream,
			SimplTypesScope metadataTranslationScope, File repositoryLocation, Format repositoryFormat)
			throws SIMPLTranslationException
	{
		super(appName);
		SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
		this.outputStream = outputStream;
		semanticsSessionScope = new SemanticsSessionScope(repositoryLocation, repositoryFormat,
				metadataTranslationScope, CybernekoWrapper.class);
	}

	public void collect(String[] urlStrings)
	{
		// seed start urls
		for (int i = 0; i < urlStrings.length; i++)
		{
			if ("//".equals(urlStrings[i]))
			{
				System.err.println("Terminate due to //");
				break;
			}
			if (urlStrings[i].startsWith("//"))
				continue; // commented out urls

			ParsedURL thatPurl = ParsedURL.getAbsolute(urlStrings[i]);
			Document document = semanticsSessionScope.getOrConstructDocument(thatPurl);
			DocumentClosure documentClosure = document.getOrConstructClosure();
			if (documentClosure != null) // super defensive -- make sure its not malformed or null or
																		// otherwise a mess
				documentCollection.add(documentClosure);
		}
		postParse(documentCollection.size());

		// process documents after parsing command line so we now how many are really coming
		for (DocumentClosure documentClosure : documentCollection)
		{
			documentClosure.addContinuation(this);
			documentClosure.queueDownload();
		}
		semanticsSessionScope.getDownloadMonitors().requestStops();
	}

	protected void postParse(int size)
	{

	}

	public static void main(String[] args)
	{
	  String urlListFile = null;
	  
	  int i = 0;
	  while (i < args.length)
	  {
	    String a = args[i];
	    if ("-h".equals(a))
	    {
	      helpAndExit();
	    }
	    else if ("-f".equals(a))
	    {
	      i += 1;
	      if (i < args.length)
	      {
	        urlListFile = args[i];
	      }
	    }
	    
	    i += 1;
	  }
	  
	  String[] urlList = null;
	  if (urlListFile == null)
	  {
	    urlList = args;
	  }
	  else
	  {
	    urlList = readLines(new File(urlListFile));
	  }
	  
	  
		MmTest mmTest;
		try
		{
			mmTest = new MmTest("NewMmTest");
			mmTest.collect(urlList);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}

	private static void helpAndExit()
  {
	  System.err.println("Args: <URL-list> | -f <URL-list-file>");
	  System.err.println("Note: a single argument or line of double slash (//) terminates the input URL list.");
  }

  private static String[] readLines(File file)
  {
	  if (file == null)
      return null;
	  
	  List<String> lines = new ArrayList<String>();
    BufferedReader br = null;
	  try
    {
	    br = new BufferedReader(new FileReader(file));
	    while (true)
	    {
  	    String line = br.readLine();
  	    if (line == null)
  	      break;
  	    line = line.trim();
  	    if (line.length() > 0)
  	      lines.add(line);
	    }
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
	  finally
	  {
	    if (br != null)
	    {
        try
        {
          br.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
	    }
	  }
	  
	  System.err.println(lines.size() + " line(s) are read.");
    return lines.toArray(new String[] {});
  }

  @Override
	public synchronized void callback(DocumentClosure incomingClosure)
	{
		if (outputOneAtATime)
			output(incomingClosure);
		if (++currentResult == documentCollection.size())
		{
			if (!outputOneAtATime)
			{
				System.out.println("\n\n");
				for (DocumentClosure documentClosure : documentCollection)
					output(documentClosure);
				writeCompositionFile();
			}
			semanticsSessionScope.getDownloadMonitors().stop(false);
		}
	}

	private static final String OUT_PREFIX		= "mmTest";
	private static final String OUT_SUFFIX		= ".xml";
	private static final String OUT_NAME			= OUT_PREFIX + OUT_SUFFIX;
	
	private void writeCompositionFile()
	{
		try
		{
			File tempFile	= File.createTempFile(OUT_PREFIX, OUT_SUFFIX);
			String dirName= tempFile.getParent();
			File outFile	= new File(dirName, OUT_NAME);
			tempFile.renameTo(outFile);
			
			SimplTypesScope.serialize(curation, outFile, Format.XML);
			System.out.println("Wrote to: " + outFile.getAbsolutePath());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void output(DocumentClosure incomingClosure)
	{
		incomingClosure.serialize(outputStream);
		Document document	= incomingClosure.getDocument();
		if (document != null)
		{
		  RichArtifact<Metadata> artifact = new RichArtifact<Metadata>();
		  artifact.outlinks().add(document);
		  curation.metadataCollection().add(artifact);
		}
	}
}
