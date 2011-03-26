/**
 * 
 */
package ecologylab.semantics.seeding;

import java.util.ArrayList;

import ecologylab.collections.Scope;
import ecologylab.semantics.connectors.old.OldContainerI;

/**
 * Encapsulates application-specific funtionality that must be associated with each Seed.
 * 
 * @author andruid
 *
 */
abstract public class SeedPeer<SP extends SeedPeer, C extends OldContainerI, I, T, S>
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
	
	abstract public void addToIndex(C container);
	
	abstract public void addToIndexI(I imgElement);
	
	abstract public void addToIndexT(T textElement);
	
	abstract public void addToIndexS(S surrogate);

}
