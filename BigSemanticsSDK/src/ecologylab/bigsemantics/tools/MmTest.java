/**
 * 
 */
package ecologylab.bigsemantics.tools;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.bigsemantics.collecting.MetaMetadataRepositoryInit;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metadata.builtins.declarations.InformationCompositionDeclaration;
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
	
	protected InformationCompositionDeclaration informationComposition = new InformationCompositionDeclaration();

	public MmTest(String appName) throws SIMPLTranslationException
	{
		this(appName, System.out);
	}

	public MmTest(String appName, OutputStream outputStream) throws SIMPLTranslationException
	{
		this(appName, outputStream, RepositoryMetadataTranslationScope.get());
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
		this(appName, outputStream, RepositoryMetadataTranslationScope.get(), repositoryLocation,
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
		MmTest mmTest;
		try
		{
			mmTest = new MmTest("NewMmTest");
			mmTest.collect(args);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
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
			
			SimplTypesScope.serialize(informationComposition, outFile, Format.XML);
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
			List<Metadata>	allMetadata	= informationComposition.getMetadata();
			if (allMetadata == null)
			{
				allMetadata	= new ArrayList<Metadata>();
				informationComposition.setMetadata(allMetadata);
			}
			allMetadata.add(document);
		}
	}
}
