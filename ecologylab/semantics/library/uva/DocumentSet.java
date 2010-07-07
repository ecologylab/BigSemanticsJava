/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.util.ArrayList;

import ecologylab.semantics.seeding.DocumentState;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.simpl_inherit;

/**
 * 
 *
 * @author andruid 
 */
@simpl_inherit
public class DocumentSet extends ElementState
{
	@simpl_nowrap
	@simpl_collection("document")
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
