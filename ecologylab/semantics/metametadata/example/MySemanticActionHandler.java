/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.documenttypes.DocumentParser;
import ecologylab.documenttypes.MetaMetadataSearchParser;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.Argument;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.xml.XMLTools;
import ecologylab.semantics.actions.SemanticActionNamedArguments;
import ecologylab.semantics.actions.exceptions.SemanticActionExecutionException;
import ecologylab.semantics.connectors.ContentElement;
import ecologylab.semantics.generated.library.WeatherReport;

/**
 * @author quyin
 * 
 */
public class MySemanticActionHandler extends SemanticActionHandler<MyContainer, MyInfoCollector>
{
	@Override
	public void backOffFromSite(SemanticAction action,
			DocumentParser<MyContainer, ?, ?> documentType, MyInfoCollector infoCollector)
	{
		Argument domainArgA = getNamedArgument(action, DOMAIN);
		String domain = domainArgA.getValue();
		infoCollector.reject(domain);
		// // this should be done in infoCollector.reject()
		// infoCollector.removeAllCandidateContainersFromSite(site);
	}

	@Override
	public void createAndVisualizeImgSurrogate(SemanticAction action,
			DocumentParser<MyContainer, ?, ?> docType, MyInfoCollector infoCollector)
	{
		/*
		try
		{
			// get the container
			MyContainer container = docType.getContainer();

			// get arguments
			Argument imagePURLArgument = getNamedArgument(action, IMAGE_PURL);
			Argument captionArgument = getNamedArgument(action, CAPTION);
			Argument hrefPURLArgument = getNamedArgument(action, HREF);
			Argument widthArgument = getNamedArgument(action, WIDTH);
			Argument heightArgument = getNamedArgument(action, HEIGHT);

			// get image url
			ParsedURL imagePURL = (ParsedURL) semanticActionReturnValueMap.get(imagePURLArgument
					.getValue());

			// get caption
			String caption = null;
			// if we have passed caption also as an argument, we need to find it.
			if (captionArgument != null)
				caption = (String) semanticActionReturnValueMap.get(captionArgument.getValue());

			// get href
			ParsedURL hrefPURL = null;
			if (hrefPURLArgument != null)
				hrefPURL = (ParsedURL) semanticActionReturnValueMap.get(hrefPURLArgument.getValue());

			// get width
			int width = 0;
			if (widthArgument != null)
				width = (Integer) semanticActionReturnValueMap.get(widthArgument.getValue());

			// get height
			int height = 0;
			if (heightArgument != null)
				height = (Integer) semanticActionReturnValueMap.get(heightArgument.getValue());

			MetaMetadata metaMetadata = infoCollector.metaMetaDataRepository().getImageMM(imagePURL);
			if (imagePURL != null)
			{
				// look out for error condition
				MetaMetadataSearchParser mmSearchType = docType instanceof MetaMetadataSearchParser ? (MetaMetadataSearchParser) docType
						: null;
				ImageElement imageElement = GlobalCollections.getNewImgElement(container, imagePURL,
						hrefPURL, width, height, caption, false, metaMetadata);
				if (imageElement != null)
				{
					// set the flags if any
					setFlagIfAny(action, imageElement);
					
					// put it in the return value map
					semanticActionReturnValueMap.put(action.getReturnValue(), imageElement);

					SeedDistributor<CfContainer> resultsDistributor = null;
					SearchResult searchResult = null;
					if (mmSearchType != null)
					{
						Seed seed = mmSearchType.getSeed();
						resultsDistributor = seed.seedDistributer(infoCollector);
						if (resultsDistributor != null) // Case 1 - Image Search
						{
							int resultNum = mmSearchType.getResultNum();
							imageElement.setSearchResult(infoCollector, resultsDistributor,
									((MetaMetadataSearchParser) documentType).getResultSoFar());
							resultsDistributor.queueResult(imageElement);
						}
					}
					if (resultsDistributor == null) // Case 2
						container.addToCandidateLocalImages(imageElement);
					if (mmSearchType != null)
					{ // FIXME -- is this enough to do if a search fails????!
						mmSearchType.incrementResultSoFar();
					}
				}
			}
			else
				error("Can't createAndVisualizeImgSurrogate because null PURL: " + metaMetadata.getName()
						+ " - " + container.purl());

		}
		catch (Exception e)
		{
			// e.printStackTrace();
			throw new SemanticActionExecutionException(e, action, semanticActionReturnValueMap);
		}
		*/
	}

	@Override
	public void createAndVisualizeTextSurrogateSemanticAction(SemanticAction action,
			DocumentParser documentType, MyInfoCollector infoCollector)
	{
		// TODO Auto-generated method stub

	}

	// seems that this is no longer needed
	@Override
	public MyContainer createContainer(SemanticAction action,
			DocumentParser<MyContainer, ?, ?> docType, MyInfoCollector infoCollector)
	{
		return null;
	}

	@Override
	public void createSemanticAnchor(SemanticAction action,
			DocumentParser<MyContainer, ?, ?> documentType, MyInfoCollector infoCollector)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void getFieldAction(SemanticAction action, DocumentParser docType,
			MyInfoCollector infoCollector)
	{
		try
		{
			// get the method name
			String returnValue = action.getReturnValue();
			String actionName = "get" + XMLTools.javaNameFromElementName(returnValue, true);

			// get the object name
			String object = action.getObject();
			if (object == null) // If no name is specified, the metadata is acted upon.
			{
				object = SemanticActionsKeyWords.METADATA;
			}

			// invoke it via reflection
			handleGeneralAction(action, object, actionName);
		}
		catch (Exception e)
		{
			System.err.println("oops! get_field action failed.");
			e.printStackTrace();
		}
	}

	protected void handleGeneralAction(SemanticAction action, String objectName, String actionName)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// get the object on which the action has to be invoked
		Object object = semanticActionReturnValueMap.get(objectName);
		// System.out.println("DEBUG::object=\t" + object);

		// when the action takes no arguments [SIMPLEST CASE :-)]
		if (!action.hasArguments())
		{
			// check if all the pre-conditions are satisfied for this action
			// if (checkPreConditionFlagsIfAny(action))
			{
				// get the method to be invoked on the object
				Method method = ReflectionTools.getMethod(object.getClass(), actionName, null);
				// System.out.println("DEBUG::methodToBeInvoked=\t" + method);

				// invoke the specified method
				Object returnValue = method.invoke(object, null);
				// System.out.println("DEBUG::Return Value=\t" + returnValue);

				// set the flags if any
				setFlagIfAny(action, returnValue);

				// put it into the semantic action return value map
				if (action.getReturnValue() != null)
				{
					// check if the method is not of type void.
					semanticActionReturnValueMap.put(action.getReturnValue(), returnValue);
				}
			}
		}
		else
		{
			// when action has some arguments
			Collection<Argument> args = action.getArgs();
			// System.out.println("DEBUG::arguments=\t" + arguments);

			// array to store the data\class type of arguments
			int numArgs = args.size();
			Class[] argumentTypeArray = new Class[numArgs];

			// array to hold the actual arguments
			Object[] argumentsArray = new Object[numArgs];

			// for finding the Method object we need to create an array of
			// classes of the arguments.
			// also we need to store the actual arguments.
			int i = 0;
			for (Argument argument : args)
			{
				// get the actual object
				argumentsArray[i] = semanticActionReturnValueMap.get(argument.getValue());
				// System.out.println("DEBUG::argumentsArray[" + i + "]=\t" + argumentsArray[i]);

				// get the object type/class
				argumentTypeArray[i] = argumentsArray[i].getClass();
				// System.out.println("DEBUG::argumentTypeArray[" + i + "]=\t" + argumentTypeArray[i]);
				i++;
			}

			// check if all the pre-conditions are satisfied for this action
			// if (checkPreConditionFlagsIfAny(action))
			{
				// get the method to be invoked on the object
				Method method = ReflectionTools.getMethod(object.getClass(), actionName, argumentTypeArray);
				// System.out.println("DEBUG::methodToBeInvoked=\t" + method + "\t object class=\t"
				// + object.getClass());

				// invoke the specified method
				Object returnValue = method.invoke(object, argumentsArray);
				// System.out.println("DEBUG::Return Value=\t" + returnValue);

				// set the flags if any
				setFlagIfAny(action, returnValue);

				// put it into the semantic action return value map
				if (action.getReturnValue() != null)
				{
					// check if method is not of type void
					semanticActionReturnValueMap.put(action.getReturnValue(), returnValue);
				}
			}
		}
	}

	@Override
	public void handleGeneralAction(SemanticAction action) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		// get the object on which the action has to be taken
		String objectName = action.getObject();

		// get the action Name;
		String actionName = action.getActionName();

		// call handleGeneralActionMethod
		handleGeneralAction(action, objectName, actionName);
	}

	@Override
	public void setFieldAction(SemanticAction action, DocumentParser docType,
			MyInfoCollector infoCollector) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException
	{
		try
		{
				String actionName = "set" + XMLTools.javaNameFromElementName(action.getReturnValue(), true);
				String object = action.getObject();
				
				// invoke it via reflection
				handleGeneralAction(action, object, actionName);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			throw new SemanticActionExecutionException(e, action, semanticActionReturnValueMap);
		}
	}

	@Override
	public void setMetadata(SemanticAction action, DocumentParser docType,
			MyInfoCollector infoCollector)
	{
		try
		{
			// get the argument
			Argument argument = getNamedArgument(action, FIELD_VALUE);
			Metadata metadata = (Metadata) semanticActionReturnValueMap.get(argument.getValue());

			// get the object
			ContentElement container = (ContentElement) semanticActionReturnValueMap.get(action.getObject());

			// invoke the actual method
			if(container!=null)
				container.setMetadata(metadata);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			throw new SemanticActionExecutionException(e, action, semanticActionReturnValueMap);
		}
	}

	@Override
	public void syncNestedMetadataSemanticAction(SemanticAction action, DocumentParser documentType,
			MyInfoCollector infoCollector)
	{
		// TODO Auto-generated method stub

	}

	public void parseLater(SemanticAction action, DocumentParser documentType,
			MyInfoCollector infoCollector)
	{
		/*
		try
		{
			MyContainer candidateContainer = getOrCreateContainer(action, documentType, infoCollector);
			
			if (candidateContainer != null)
			{
				// // TODO: implement this method in MyInfoCollector 
				// infoCollector.addCandidateContainer(candidateContainer);
			}
		}
		catch (Exception e)
		{
			throw new SemanticActionExecutionException(e, action, semanticActionReturnValueMap);
		}*/
	}

	public void parseNow(SemanticAction action, DocumentParser docType, MyInfoCollector infoCollector)
	{
		Argument purlA = action.getArgument(SemanticActionNamedArguments.CONTAINER_LINK);
		if (purlA != null)
		{
			ParsedURL purl = (ParsedURL) semanticActionReturnValueMap.get(purlA.getValue());
			MyContainer container = new MyContainer(null, infoCollector, purl);
			try
			{
				container.performDownload();
				Document metadata = container.metadata();
				if (metadata != null && metadata instanceof WeatherReport
						&& ((WeatherReport) metadata).city().getValue() != null)
				{
					ResultCollector.get().collect(container.metadata());
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
