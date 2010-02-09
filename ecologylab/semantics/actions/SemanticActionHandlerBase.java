/**
 * 
 */
package ecologylab.semantics.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import ecologylab.documenttypes.DocumentParser;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionNamedArguments;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.actions.exceptions.SemanticActionExecutionException;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.ContentElement;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.Argument;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.XMLTools;

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
			Container container = infoCollector.getContainer((C) docType.getContainer(), purl, false,
					false, mmd);
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
	public void getFieldAction(SemanticAction action, DocumentParser docType, IC infoCollector)
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

		// get the action Name;
		String actionName = action.getActionName();

		// call handleGeneralActionMethod
		handleGeneralAction(action, objectName, actionName);
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
	public void setFieldAction(SemanticAction action, DocumentParser docType, IC infoCollector)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
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
			// e.printStackTrace();
			throw new SemanticActionExecutionException(e, action, semanticActionReturnValueMap);
		}
	}

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
