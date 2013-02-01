package ecologylab.semantics.collecting;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import ecologylab.generic.Continuation;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;

/**
 * 
 * 
 * @author quyin
 * 
 */
public class DocumentDownloadedEventHandler implements Continuation<DocumentClosure>
{

	protected Object		hostObject;

	protected ParsedURL	location;

	protected Field			field;

	public DocumentDownloadedEventHandler()
	{
		super();
	}

	@Override
	public void callback(DocumentClosure closure)
	{
		Document newDoc = closure.getDocument();

		beforeDocumentChange(newDoc);

		handle(newDoc);

		afterDocumentChange(newDoc);
	}

	protected void handle(Document newDoc)
	{
		if (hostObject == null || field == null)
			return;
		
		if (!Collection.class.isAssignableFrom(field.getType()))
		{
			ReflectionTools.setFieldValue(hostObject, field, newDoc);
		}
		else
		{
			Object listObj = ReflectionTools.getFieldValue(hostObject, field);
			if (listObj != null && listObj instanceof List)
			{
				List list = (List) listObj;
				int i = 0;
				while (i < list.size())
				{
					Object element = list.get(i);
					if (element instanceof Document)
					{
						Document currDoc = (Document) list.get(i);
						if (currDoc.hasLocation(location))
						{
							list.set(i, newDoc);
						}
					}
					i++;
				}
			}
		}
		
		if (hostObject instanceof Metadata)
			((Metadata) hostObject).setMetadataChanged(true);
	}

	/**
	 * invoked before assigning downloaded document to the host object.
	 * 
	 * @param newDoc
	 */
	protected void beforeDocumentChange(Document newDoc)
	{

	}

	/**
	 * invoked after assigning downloaded document to the host object.
	 * 
	 * @param newDoc
	 */
	protected void afterDocumentChange(Document newDoc)
	{

	}

}
