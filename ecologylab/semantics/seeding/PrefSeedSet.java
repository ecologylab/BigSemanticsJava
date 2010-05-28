/**
 * 
 */
package ecologylab.semantics.seeding;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_other_tags;

/**
 * @author andruid
 *
 */
@xml_inherit
@xml_other_tags({"pref_element"})
public class PrefSeedSet extends Pref<SeedSet>
{
	@xml_nested
	@xml_tag("seed_set")
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
	public Pref<SeedSet> clone()
	{
		PrefSeedSet result	= new PrefSeedSet(this.name);
		result.value				= this.value;
		return result;
	}
}
