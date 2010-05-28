/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.util.ArrayList;

import ecologylab.services.messages.cf.DocumentState;
import ecologylab.xml.ElementState;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_collection;
import ecologylab.xml.ElementState.xml_nowrap;

/**
 * 
 *
 * @author andruid 
 */
@xml_inherit
public class DocumentSet extends ElementState
{
	@xml_nowrap
	@xml_collection("document")
	ArrayList<DocumentState>	documents;
	
	/**
	 * 
	 */
	public DocumentSet()
	{
		// TODO Auto-generated constructor stub
	}
	
	public ArrayList<DocumentState> getDocuments()
	{
		if (documents != null)
			return documents;
		return documents = new ArrayList<DocumentState>();
	}


}
