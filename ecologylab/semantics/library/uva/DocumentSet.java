/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.util.ArrayList;

import ecologylab.semantics.seeding.DocumentState;
import ecologylab.xml.ElementState;
import ecologylab.xml.xml_inherit;

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
