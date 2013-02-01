/**
 * 
 */
package ecologylab.bigsemantics.seeding;

import ecologylab.appframework.types.prefs.PrefElementState;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_other_tags;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * @author andruid
 *
 */
@simpl_inherit
@simpl_other_tags({"pref_element"})
public class PrefSeedSet extends PrefElementState<SeedSet>
{
	@simpl_composite
	@simpl_tag("seed_set")
	SeedSet				value;
	/**
	 * 
	 */
	public PrefSeedSet()
	{
		super();
	}
	public PrefSeedSet(String name)
	{
		super();
		this.name	= name;
	}
	@Override
	protected SeedSet getValue()
	{
		return value;
	}
	@Override
	public void setValue(SeedSet newValue)
	{
		value	= newValue;
		prefChanged();
	}

	/**
	 * XXX NOTE: THIS IS AN UNSAFE CLONE. IF THE VALUE OF THIS PREFERENCE IS TO BE MODIFIED, THIS
	 * METHOD MUST BE RECONSIDERED. A very cool and proper way to do this would be to translate value
	 * to and from XML, but this is impossible without the correct translation scope.
	 * 
	 * @see ecologylab.appframework.types.prefs.Pref#clone()
	 */
	@Override
	public PrefElementState<SeedSet> clone()
	{
		PrefSeedSet result	= new PrefSeedSet(this.name);
		result.value				= this.value;
		return result;
	}
}
