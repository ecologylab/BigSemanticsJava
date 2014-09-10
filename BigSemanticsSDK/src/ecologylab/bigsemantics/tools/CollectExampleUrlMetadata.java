package ecologylab.bigsemantics.tools;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.bigsemantics.collecting.MetaMetadataRepositoryInit;
import ecologylab.bigsemantics.collecting.MetaMetadataRepositoryLocator;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTypesScope;
import ecologylab.bigsemantics.generated.library.curation.*;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metadata.builtins.RichArtifact;
import ecologylab.bigsemantics.metametadata.ExampleUrl;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.Format;


/**
 * Similar to NewMmTest, only that instead of taking a set of locations as arguments as input,
 * the locations are taken from the <example_url> tags from the metadata in the repository.  
 * 
 * Construct an information_composition_declaration object. Add each metadata that gets constructed / extracted into it.
 * Deserialize this object and write it to xml file: (DEFAULT_REPOSITORY_LOCATION + "/../testData/" + "collectedExampleUrlMetadata.xml")
 * 
 */
public class CollectExampleUrlMetadata extends SingletonApplicationEnvironment 
implements	Continuation<DocumentClosure>
{
	ArrayList<DocumentClosure> documentCollection = new ArrayList<DocumentClosure>();
	
	int currentResult;
	
	protected boolean outputOneAtATime;
	
	OutputStream outputStream;
	
	protected SemanticsSessionScope	semanticsSessionScope;
	
	protected Curation curation = new Curation();
	
	public CollectExampleUrlMetadata(String appName) throws SIMPLTranslationException
	{
		this(appName, System.out);
	}
	
	public CollectExampleUrlMetadata(String appName, OutputStream outputStream) throws SIMPLTranslationException
	{
		this(appName, outputStream, RepositoryMetadataTypesScope.get());
	}
	
	public CollectExampleUrlMetadata(String appName, OutputStream outputStream,
	SimplTypesScope metadataTranslationScope) throws SIMPLTranslationException
	{
		this(appName, outputStream, metadataTranslationScope, null,
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FORMAT);
	}
	
	public CollectExampleUrlMetadata(String appName, File repositoryLocation, Format repositoryFormat)
	throws SIMPLTranslationException
	{
		this(appName, System.out, repositoryLocation, repositoryFormat);
	}
	
	public CollectExampleUrlMetadata(String appName, OutputStream outputStream, File repositoryLocation,
	Format repositoryFormat) throws SIMPLTranslationException
	{
		this(appName, outputStream, RepositoryMetadataTypesScope.get(), repositoryLocation,
		repositoryFormat);
	}
	
	public CollectExampleUrlMetadata(String appName, OutputStream outputStream,
	SimplTypesScope metadataTranslationScope, File repositoryLocation, Format repositoryFormat)
	throws SIMPLTranslationException
	{
		super(appName);
		SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
		this.outputStream = outputStream;
		semanticsSessionScope = new SemanticsSessionScope(repositoryLocation, repositoryFormat,
		metadataTranslationScope, CybernekoWrapper.class);
	}
	
	public void collect()
	{
		
		List<String> urlStrings = new ArrayList<String>();
		//collect all the example_urls from the metametada from the repository
		//for(Metametadata metametadata : semanticsSessionScope.getMetaMetadataRepository().g)
		
		
		Collection<MetaMetadata> metametadataByName = semanticsSessionScope.getMetaMetadataRepository().values();
		SimplTypesScope metadataTScope = semanticsSessionScope.getMetaMetadataRepository().metadataTranslationScope();
		for (MetaMetadata metametadata : metametadataByName)
		{	
			List<ExampleUrl> exampleUrls = metametadata.getExampleUrls();
			if (exampleUrls != null && exampleUrls.size() > 0)
				for(ExampleUrl exampleUrl : metametadata.getExampleUrls())
				{					
					
					ParsedURL thatPurl = exampleUrl.getUrl();
										
					//case 1:
					Document document = (Document) metametadata.constructMetadata(metadataTScope);
					document.setLocation(thatPurl);
					document.setSemanticsSessionScope(semanticsSessionScope);

					//or defensive - case 2:
					//Document document = semanticsSessionScope.getOrConstructDocument(thatPurl);					
					
					DocumentClosure documentClosure = document.getOrConstructClosure();
					if (documentClosure != null) // super defensive -- make sure its not malformed or null or
																				// otherwise a mess
						documentCollection.add(documentClosure);
				}
		}
		postParse(documentCollection.size());
		
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
		CollectExampleUrlMetadata collectExampleUrlMetadata;
		try
		{
			collectExampleUrlMetadata = new CollectExampleUrlMetadata("GenerateExampleUrlMm");
			collectExampleUrlMetadata.collect();
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
	
	private static final String OUT_PREFIX		= "collectedExampleUrlMetadata";
	private static final String OUT_SUFFIX		= ".xml";
	private static final String OUT_NAME			= OUT_PREFIX + OUT_SUFFIX;
	
	private void writeCompositionFile()
	{
		try
		{
			File outFile = new File(MetaMetadataRepositoryLocator.locateRepositoryByDefaultLocations()
			                        + "/../testData/"
			                        + OUT_NAME);
			
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
