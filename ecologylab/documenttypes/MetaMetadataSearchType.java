/**
 * 
 */
package ecologylab.documenttypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.CFPrefNames;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.SearchEngineNames;
import ecologylab.semantics.library.TypeTagNames;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.seeding.ResultDistributer;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.xml.ElementState;
import ecologylab.xml.XMLTranslationException;

/**
 * @author amathur
 * 
 */
public class MetaMetadataSearchType<M extends MetadataBase,C extends Container,SA extends SemanticAction<SA>> extends MetaMetadataDocumentTypeBase<M,SA,C>
		implements CFPrefNames, DispatchTarget
{

	/**
	 * The Serach query URL
	 */
	private ParsedURL							searchURL;

	/**
	 * Name of the Search engine;
	 */
	private String								engine;

	/**
	 * 
	 */
	private SearchState						searchSeed;

	private int										resultsSoFar	= 0;


	/**
	 * 
	 * @param infoProcessor
	 * @param semanticActionHandler
	 */
	public MetaMetadataSearchType(InfoCollector infoProcessor,
			SemanticActionHandler<SA,C,?> semanticActionHandler)
	{
		super(infoProcessor,semanticActionHandler);
	

	}

	public MetaMetadataSearchType(InfoCollector infoCollector, String query, float bias,
			int numResults, String engine)
	{
		this(new SearchState(query, engine, (short) 0, numResults, true), infoCollector, engine);
	}

	public MetaMetadataSearchType(SearchState searchSeed, InfoCollector infoCollector, String engine)
	{
		this(infoCollector, null, engine, searchSeed);
	}

	/**
	 * @param infoProcessor
	 * @param engine
	 * @param semanticAction
	 * @param searchURL
	 */
	public MetaMetadataSearchType(InfoCollector infoProcessor,
			SemanticActionHandler<SA,C,?> semanticActionHandler, String engine, SearchState searchSeed)
	{
		super(infoProcessor,semanticActionHandler);
		this.engine = engine;
		this.searchSeed = searchSeed;
		formSearchUrlBasedOnEngine(searchSeed.currentFirstResultIndex(), infoProcessor);

		// if search PURL is not null for a container.
		if (searchURL != null)
		{
			MetaMetadata metaMetadata = infoProcessor.metaMetaDataRepository().getByTagName(
					TypeTagNames.SEARCH_TAG);
			Container container = infoProcessor.getContainer(null, searchURL, false, false, metaMetadata);
			setContainer(container);
			searchSeed.eliminatePlusesFromQuery();
			setQuery(searchSeed);
			container.presetDocumentType(this);
			container.setDispatchTarget(this);
			container.setAsTrueSeed(searchSeed);
			queueSearchrequest();
			// System.out.println("DEBUG::queued search request for \t"+searchSeed.getQuery());
			// infoProcessor.pauseDownloadMonitor();
		}
	}

	public void queueSearchrequest()
	{
		if (this.searchSeed != null)
		{
			ResultDistributer resultDistributer = this.searchSeed
					.resultDistributer(abstractInfoCollector);
			if (resultDistributer != null)
			{
				resultDistributer.queueSearchRequest(container);
				// System.out.println("DEBUG::queued search request for\t"+container+"\tusing rd=\t"+resultDistributer);
				return;
			}
		}
		container.queueDownload();
		// System.out.println("DEBUG::queued container\t"+container+"\t for download");
	}

	/**
	 * TODO implement this. This method set the query using reflecrtion on search class.
	 */
	private void setQuery(SearchState searchSeed)
	{
		container.setQuery(searchSeed.getQuery());
	}

	/**
	 * Customize the type based on the search engine
	 */
	private void customizeBasedOnEngine()
	{
		if (engine.equals(SearchEngineNames.GOOGLE))
		{
			container.setJustCrawl(true);
			String query = searchSeed.valueString();
			InterestModel.expressInterest(query, (short) 2);
			abstractInfoCollector.displayStatus("Processing google search page: " + query);
		}
	}

	/**
	 * 
	 */
	private void formSearchUrlBasedOnEngine(int firstResultIndex,InfoCollector infoProcessor)
	{
/*		if (engine.equals(SearchEngineNames.GOOGLE))
		{
			searchURL = ParsedURL
					.getAbsolute(
							((searchSeed.searchType() == SearchEngineNames.REGULAR) ? SearchEngineNames.regularGoogleSearchURLString
									:

									(searchSeed.searchType() == SearchEngineNames.IMAGE) ? SearchEngineNames.imageGoogleSearchURLString
											: (searchSeed.searchType() == SearchEngineNames.SITE) ? SearchEngineUtilities
													.siteGoogleLimitSearchURLString(searchSeed.siteString())
													: SearchEngineNames.relatedGoogleSearchUrlString)
									+ (searchSeed.valueString().replace(' ', '+')).replace("&quot;", "%22")

									+ (IGNORE_PDF.value() ? SearchEngineNames.GOOGLE_NO_PDF_ARG : "")
									+ "&num="
									+ searchSeed.numResults() + "&start=" + firstResultIndex, "broken Google URL");
		}
		else if (engine.equals(SearchEngineNames.FLICKR))
		{
			searchURL = ParsedURL.getAbsolute("http://www.flickr.com/search/?q=" + searchSeed.getQuery());
		}
		else if(engine.equals(SearchEngineNames.YAHOO))
		{
			searchURL= ParsedURL.getAbsolute("http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=yahoosearchwebrss&query="+searchSeed.getQuery());
		}
		else if(engine.equals(SearchEngineNames.FLICK_AUTHOR))
		{
			searchURL = ParsedURL.getAbsolute("http://www.flickr.com/photos/"+searchSeed.getQuery()+"/");
		}*/

		MetaMetadataRepository mmdRepo= infoProcessor.metaMetaDataRepository();
		String urlPrefix= mmdRepo.getSearchURL(engine);
		String query = searchSeed.getQuery();
		// we replace all white spaces by +
		query = query.replace(' ', '+'); 
		String urlSuffix= mmdRepo.getSearchURLSufix(engine);
		String numResultString=mmdRepo.getNumResultString(engine);
		String startString = mmdRepo.getStartString(engine);
		
		searchURL = ParsedURL.getAbsolute(urlPrefix+query+numResultString+searchSeed.numResults()+startString+firstResultIndex+urlSuffix);
		//searchURL = ParsedURL.getAbsolute(infoProcessor.metaMetaDataRepository().getSearchURL(engine)+searchSeed.getQuery());
	}

	public void delivery(Object o)
	{
		ResultDistributer aggregator = this.searchSeed.resultDistributer(abstractInfoCollector);
		if (aggregator != null)
			aggregator.doneQueueing(searchSeed.searchNum(), resultsSoFar);
		if (this.searchSeed != null)
			this.searchSeed.incrementNumResultsBy(searchSeed.numResults());

	}

	/**
	 * @return the searchSeed
	 */
	public SearchState getSearchSeed()
	{
		return searchSeed;
	}

	public void incrementResultSoFar()
	{
		this.resultsSoFar++;
	}

	public int getResultSoFar()
	{
		return this.resultsSoFar;
	}

	public int getResultNum()
	{
		return resultsSoFar + searchSeed.currentFirstResultIndex();
	}

	public ParsedURL purl()
	{
		return searchURL;
	}

	@Override
	public M buildMetadataObject()
	{
		M populatedMetadata = null;
		initailizeMetadataObjectBuilding();
		if (metaMetadata.isSupported(container.purl()))
		{
			if (metaMetadata.directBindingType())
			{
				try
				{
					populatedMetadata =(M) ElementState.translateFromXML(inputStream(),
							getMetadataTranslationScope());
				}
				catch (XMLTranslationException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				Tidy tidy;
				tidy = new Tidy();
				tidy.setQuiet(true);
				tidy.setShowWarnings(false);
				XPath xpath = XPathFactory.newInstance().newXPath();
				/*final InputStream in= inputStream();
				System.out.println(convertStreamToString(in));*/
				Document tidyDOM = tidy.parseDOM(inputStream(), null);
				populatedMetadata = (M) recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
						(M) getMetadata(), tidyDOM, xpath);
			}
		}
		try
		{
			populatedMetadata.translateToXML(System.out);
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return populatedMetadata;
	}

	public String convertStreamToString(InputStream is) {
	        /*
		29.         * To convert the InputStream to String we use the BufferedReader.readLine()
		30.         * method. We iterate until the BufferedReader return null which means
		31.         * there's no more data to read. Each line will appended to a StringBuilder
		32.         * and returned as String.
		33.         */
		        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		        StringBuilder sb = new StringBuilder();
		 
		       String line = null;
		       try {
		           while ((line = reader.readLine()) != null) {
		               sb.append(line + "\n");
		            }
		        } catch (IOException e) {
		           e.printStackTrace();
		        } finally {
		        }
		 
		        return sb.toString();
		    }
}