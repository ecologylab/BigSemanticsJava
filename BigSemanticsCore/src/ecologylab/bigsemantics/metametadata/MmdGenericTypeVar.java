package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.Collection;

import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_map_key_field;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * the generic variable in meta-metadata.
 * 
 * @author quyin
 * 
 */
@simpl_tag("generic_type_var")
@simpl_inherit
public class MmdGenericTypeVar extends ElementState
{

	/**
	 * the name of the generic type variable. should be all capitalized.
	 */
	@simpl_scalar
	private String									name;

	/**
	 * the (covariant) bound of the generic type. it could be either a concrete meta-metadata type
	 * name, or an already defined generic type variable name.
	 */
	@simpl_scalar
	@simpl_tag("extends")
	private String									extendsAttribute;

	// TODO @simpl_scalar @simpl_tag("super") private String superAttribute;

	/**
	 * the type used to instantiate this generic type variable. it could be either a concrete
	 * meta-metadata type name, or an already defined generic type variable name.
	 */
	@simpl_scalar
	private String									arg;

	/**
	 * a scope of nested generic type variables. e.g. A, B in &lt;M extends Media&lt;A, B&gt;&gt;.
	 */
	@simpl_map("generic_type_var")
	@simpl_map_key_field("name")
	@simpl_nowrap
	private MmdGenericTypeVarScope	nestedGenericTypeVars;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getExtendsAttribute()
	{
		return extendsAttribute;
	}

	public void setExtendsAttribute(String extendsAttribute)
	{
		this.extendsAttribute = extendsAttribute;
	}

	public String getArg()
	{
		return arg;
	}

	public void setArg(String arg)
	{
		this.arg = arg;
	}

	public MmdGenericTypeVarScope getNestedGenericTypeVarScope()
	{
		return nestedGenericTypeVars;
	}
	
	static Collection<MmdGenericTypeVar> EMPTY_COLLECTION = new ArrayList<MmdGenericTypeVar>();
	
	public Collection<MmdGenericTypeVar> getNestedGenericTypeVars()
	{
		return nestedGenericTypeVars == null ? EMPTY_COLLECTION : nestedGenericTypeVars.values();
	}

	public void setNestedGenericTypeVars(MmdGenericTypeVarScope nestedGenericTypeVars)
	{
		this.nestedGenericTypeVars = nestedGenericTypeVars;
	}

	public static String getMdClassNameFromMmdOrNoChange(String mmdName,
			MetaMetadataRepository repository, MmdCompilerService compilerService)
	{
		MetaMetadata mmd = repository.getMMByName(mmdName);
		if (mmd == null)
		{
			return mmdName;
		}
		else
		{
			MetadataClassDescriptor metadataClassDescriptor = mmd.getMetadataClassDescriptor();
			if (compilerService != null)
				compilerService.addCurrentClassDependency(metadataClassDescriptor);
			return metadataClassDescriptor.getDescribedClassSimpleName();
		}
	}

	public boolean isAssignment()
	{
		return arg != null;
	}

	public boolean isBound()
	{
		return extendsAttribute != null /* || superAttribute != null */;
	}

	public void resolveArgAndBounds(MmdGenericTypeVarScope genericTypeVarScope)
	{
		if (isAssignment())
		{
			MmdGenericTypeVar gtv = genericTypeVarScope.get(arg);
			if (gtv != null)
			{
				gtv.resolveArgAndBounds(genericTypeVarScope);

				if (gtv.isAssignment())
				{
					arg = gtv.arg;
				}
				else if (gtv.isBound())
				{
					arg = null;
					extendsAttribute = gtv.extendsAttribute;
					// superAttribute = gtv.superAttribute;
				}
			}
		}
		else if (isBound())
		{
			MmdGenericTypeVar extendsGtv = genericTypeVarScope.get(extendsAttribute);
			if (extendsGtv != null)
			{
				extendsGtv.resolveArgAndBounds(genericTypeVarScope);
				extendsAttribute = extendsGtv.arg != null ? extendsGtv.arg : extendsGtv.extendsAttribute;
			}
			
			// TODO superAttribute
		}
		else
			throw new MetaMetadataException(
					"wrong meta-metadata generic type var type! must either be an assignment or a bound.");
	}

}
