/**
 * 
 */
package ecologylab.semantics.seeding;

import java.util.ArrayList;

import ecologylab.collections.Scope;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.TextClipping;

/**
 * Encapsulates application-specific funtionality that must be associated with each Seed.
 * 
 * @author andruid
 *
 */
abstract public class SeedPeer<SP extends SeedPeer>
{
	Seed						seed;
	
	SP							ancestor;

  protected ArrayList<SP>                kids                         = new ArrayList<SP>(0);


	/**
	 * 
	 */
	public SeedPeer(Seed seed)
	{
		this.seed	= seed;
	}
	
	public void addKid(SP kid)
	{
		kids.add(kid);
	}

	public void addThisToParent()
	{
		if (ancestor != null)
			ancestor.addKid((SP) this);
	}
	

	public void refreshScreenSurrogatesVisibility()
	{
		
	}
	
	abstract public void notifyInterface(Scope scope, String key);
	
	abstract public void addToIndex(DocumentClosure documentClosure);
	
	abstract public void addToIndexI(DocumentClosure<Image> imageClosure);
	
	abstract public void addToIndexT(TextClipping textClipping);
	
//	abstract public void addToIndexS(S surrogate);

}
