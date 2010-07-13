/**
 * 
 */
package ecologylab.semantics.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.exceptions.SemanticActionExecutionException;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.ContentElement;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.Argument;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.serialization.XMLTools;

/**
 * This is the base class for SemanticActionHandler interface. It implements the most common
 * semantic handlers as the defaults. You start from here to write your own semantic handler. Note
 * that you can specify the generic type C and IC to use your own content Container and
 * InfoCollector.
 * 
 * @author quyin
 */
public class SemanticActionHandlerBase<C extends Container, IC extends InfoCollector<C>> extends
		SemanticActionHandler<C, IC>
{
	public static final String	handlerMethodName	= "handle";

	public SemanticActionHandlerBase()
	{
		super();
	}

	/**
	 * By default, this method prevent the InfoCollector from collecting information from the
	 * specified domain, by calling the reject() method of the InfoCollector. You specify the unwanted
	 * domain by providing the "domain" semantic argument in the meta-metadata xml codes.
	 */
	@Override
	public void backOffFromSite(SemanticAction action, DocumentParser documentType, IC infoCollector)
	{
		Argument domainArgA = getNamedArgument(action, DOMAIN);
		String domain = domainArgA.getValue();
		infoCollector.reject(domain);
		// infoCollector.removeAllCandidateContainersFromSite(site);
	}

	/**
	 * This is a user defined (or application specified) semantic action. It creates and visualizes an
	 * image surrogate as its name reveals. Currently, to add your own semantic action, you need to
	 * modify the handleSemanticAction() method and process the arguments by yourself.
	 */
	@Override
	public void createAndVisualizeImgSurrogate(SemanticAction action, DocumentParser docType,
			IC infoCollector)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Another user defined semantic action.
	 */
	@Override
	public void createAndVisualizeTextSurrogateSemanticAction(SemanticAction action,
			DocumentParser documentType, IC infoCollector)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Create a container for a specific link, so that you can process it (e.g. parse it). Now you
	 * don't have to manually use this action in the meta-metadata xml codes before downloading or
	 * parsing it; however, if you write your own semantic action, you may need to call this method
	 * when you handle it. Parameter: container_link, the link you want to create a container for.
	 * 
	 * @return The appropriate container for the URL, or null if there is an error.
	 */
	@Override
	public Container createContainer(SemanticAction action, DocumentParser docType, IC infoCollector)
	{
		Argument purlA = action.getArgument(SemanticActionNamedArguments.CONTAINER_LINK);
		if (purlA != null)
		{
			Container ancestor = docType.getContainer();
			ParsedURL purl = (ParsedURL) semanticActionReturnValueMap.get(purlA.getValue());
			MetaMetadata mmd = infoCollector.metaMetaDataRepository().getDocumentMM(purl);
			Container container = infoCollector.getContainer((C) ancestor, purl, false, true, mmd);
			return container;
		}
		return null;
	}

	/**
	 * This is also a user defined semantic action.
	 */
	@Override
	public void createSemanticAnchor(SemanticAction action, DocumentParser documentType,
			IC infoCollector)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * This semantic action will retrieve a certain field from a certain object, so that you can
	 * operate on it later, e.g. retrieve an image URL and create an image surrogate based on this
	 * URL. Parameters: the target object and the target field.
	 */
	@Override
	@Deprecated
	public void getFieldAction(SemanticAction action, DocumentParser docType, IC infoCollector)
	{
	}

	/**
	 * The entry method of handling semantic actions. Basically, this method gets names, assemble the
	 * action and call a helper function to actually handle the action.
	 */
	@Override
	public void handleGeneralAction(SemanticAction action) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		// get the object on which the action has to be taken
		String objectName = action.getObject();
		if (objectName == null)
			objectName = SemanticActionsKeyWords.METADATA;

		// get the action Name;
		String actionName = action.getActionName();
		String properActionName = XMLTools.javaNameFromElementName(actionName, false);

		// call handleGeneralActionMethod
		handleGeneralAction(action, objectName, properActionName);
	}

	/**
	 * This is the actual function which handles semantic actions. It retrieves the object, apply the
	 * action, and make the returned value (if any) available to following actions.
	 * 
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected void handleGeneralAction(SemanticAction action, String objectName, String actionName)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// in new implementation, semantic actions will always have arguments, including the target
		// object name and the return value name

		// get the object on which the action has to be invoked
		Object object = semanticActionReturnValueMap.get(objectName);
		// System.out.println("DEBUG::object=\t" + object);

		// when action has some arguments
		Map<String, Argument> args = action.getArgs();
		// System.out.println("DEBUG::arguments=\t" + arguments);

		Map<String, Object> argValues = new HashMap<String, Object>();

		for (String argumentName : args.keySet())
		{
			// get the actual object
			Object value = semanticActionReturnValueMap.get(action.getArgument(argumentName).getValue());
			argValues.put(argumentName, value);
		}

		// check if all the pre-conditions are satisfied for this action
		// if (checkPreConditionFlagsIfAny(action))
		{
			// get the method to be invoked on the object
			// Method method = ReflectionTools.getMethod(object.getClass(), actionName,
			// argumentTypeArray);
			
			Object returnValue = action.handle(object, argValues);
			
//			Method method = ReflectionTools.getMethod(action.getClass(), handlerMethodName, new Class[]
//			{ Object.class, Map.class });
			// System.out.println("DEBUG::methodToBeInvoked=\t" + method + "\t object class=\t"
			// + object.getClass());

			// invoke the specified method
			// Object returnValue = method.invoke(object, argumentsArray);
//			Object returnValue = method.invoke(action, object, argValues);
			// System.out.println("DEBUG::Return Value=\t" + returnValue);

			// put it into the semantic action return value map
			if (action.getReturnValue() != null)
			{
				// check if method is not of type void
				semanticActionReturnValueMap.put(action.getReturnValue(), returnValue);
			}
		}
	}

	/**
	 * This action adds a URL to the to-be-downloaded list. The URL will be downloaded and parsed
	 * later. When it will be downloaded and parsed is determined by the InfoCollector. This action is
	 * similiar to ParseDocumentNow action.
	 */
	@Override
	public void parseDocumentLater(SemanticAction action, DocumentParser documentType,
			IC infoCollector)
	{
		Container container = createContainer(action, documentType, infoCollector);
		if (container != null)
		{
			infoCollector.getContainerDownloadIfNeeded((C) documentType.getContainer(), container.purl(),
					null, false, false, false);
		}
	}

	/**
	 * This action downloads the document from a given URL, parses it with specified parsing method,
	 * and put the results in a Container which can be used later.
	 */
	@Override
	public void parseDocumentNow(SemanticAction action, DocumentParser docType, IC infoCollector)
	{
		Container container = createContainer(action, docType, infoCollector);
		if (container != null)
		{
			try
			{
				container.performDownload();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * This action is the setter version of getFieldAction. It allows you to set the value of a
	 * certain field of a certain object manually.
	 */
	@Override
	@Deprecated
	public void setFieldAction(SemanticAction action, DocumentParser docType, IC infoCollector)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
	}

	/*
	 * private void setField(Object object, String actionName) throws IllegalArgumentException,
	 * IllegalAccessException, InvocationTargetException { Method method =
	 * ReflectionTools.getMethod(object.getClass(), actionName, null); return method.invoke(object,
	 * null); }
	 */

	/**
	 * This action allows you to specify metadata for a certain object manually.
	 */
	@Override
	public void setMetadata(SemanticAction action, DocumentParser docType, IC infoCollector)
	{
		try
		{
			// get the argument
			Argument argument = getNamedArgument(action, FIELD_VALUE);
			Metadata metadata = (Metadata) semanticActionReturnValueMap.get(argument.getValue());

			// get the object
			ContentElement container = (ContentElement) semanticActionReturnValueMap.get(action
					.getObject());

			// invoke the actual method
			if (container != null)
				container.setMetadata(metadata);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			throw new SemanticActionExecutionException(e, action, semanticActionReturnValueMap);
		}
	}
}
