package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.generic.StringBuilderBaseUtils;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.types.element.IMappable;

/**
 * the generic variable in meta-metadata.
 * 
 * @author quyin
 * 
 */
@SuppressWarnings("rawtypes")
@simpl_tag("generic_type_var")
@simpl_inherit
public class MmdGenericTypeVar extends ElementState
    implements IMappable<String>, ScopeProvider
{

  /**
   * the name of the generic type variable. should be all capitalized.
   */
  @simpl_scalar
  private String                         name;

  /**
   * the (covariant) bound of the generic type. it could be either a concrete meta-metadata type
   * name, or an already defined generic type variable name.
   */
  @simpl_scalar
  @simpl_tag("extends")
  private String                         extendsAttribute;

  // TODO @simpl_scalar @simpl_tag("super") private String superAttribute;

  /**
   * the type used to instantiate this generic type variable. it could be either a concrete
   * meta-metadata type name, or an already defined generic type variable name.
   */
  @simpl_scalar
  private String                         arg;

  /**
   * a scope of nested generic type variables. e.g. A, B in &lt;M extends Media&lt;A, B&gt;&gt;.
   */
  @simpl_map("generic_type_var")
  @simpl_nowrap
  private Map<String, MmdGenericTypeVar> nestedGenericTypeVars;

  private boolean                        rebound;

  private MmdScope                       scope;
  
  private MetaMetadata                   resolvedArgMmd;
  
  private MetaMetadata                   resolvedExtendsMmd;

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

  public Map<String, MmdGenericTypeVar> getNestedGenericTypeVarScope()
  {
    return nestedGenericTypeVars;
  }

  static Collection<MmdGenericTypeVar> EMPTY_COLLECTION = new ArrayList<MmdGenericTypeVar>();

  public Collection<MmdGenericTypeVar> getNestedGenericTypeVars()
  {
    return nestedGenericTypeVars == null ? EMPTY_COLLECTION : nestedGenericTypeVars.values();
  }

  public void setNestedGenericTypeVars(Map<String, MmdGenericTypeVar> nestedGenericTypeVars)
  {
    this.nestedGenericTypeVars = nestedGenericTypeVars;
  }

  public static String getMdClassNameFromMmdOrNoChange(String mmdName,
                                                       MetaMetadataRepository repository,
                                                       MmdCompilerService compilerService)
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

  public boolean isRebound()
  {
    return rebound;
  }

  public void setRebound(boolean rebound)
  {
    this.rebound = rebound;
  }

  public void resolveArgAndBounds(Map<String, Object> scope)
  {
    if (isAssignment())
    {
      Object obj = scope.get(arg);
      MmdGenericTypeVar gtv = (MmdGenericTypeVar) obj;
      if (gtv != null)
      {
        gtv.resolveArgAndBounds(scope);

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
      MmdGenericTypeVar extendsGtv = (MmdGenericTypeVar) scope.get(extendsAttribute);
      if (extendsGtv != null)
      {
        extendsGtv.resolveArgAndBounds(scope);
        extendsAttribute = extendsGtv.arg != null ? extendsGtv.arg : extendsGtv.extendsAttribute;
      }

      // TODO superAttribute

      if (scope.get(name) != null)
      {
        rebound = true;
      }
    }
    else
      throw new MetaMetadataException(
                                      "wrong meta-metadata generic type var type! must either be an assignment or a bound.");
  }

  public boolean nothingSpecified()
  {
    return arg == null && extendsAttribute == null;
  }

  public String toString()
  {
    StringBuilder sb = StringBuilderBaseUtils.acquire();
    sb.append(getClass().getSimpleName()).append("[").append(name);
    if (arg != null)
    {
      sb.append(",arg=").append(arg);
    }
    if (extendsAttribute != null)
    {
      sb.append(",extends=").append(extendsAttribute);
    }
    sb.append("]");
    String result = sb.toString();
    StringBuilderBaseUtils.release(sb);
    return result;
  }

  @Override
  public MmdScope getScope()
  {
    return scope;
  }

  @Override
  public MmdScope scope()
  {
    if (scope == null)
    {
      scope = new MmdScope();
      if (nestedGenericTypeVars != null)
      {
        for (MmdGenericTypeVar nestedGtv : nestedGenericTypeVars.values())
        {
          nestedGtv.scope().addAncestor(scope);
          scope.put(nestedGtv.getName(), nestedGtv);
        }
      }
    }
    return scope;
  }

  @Override
  public void setScope(MmdScope scope)
  {
    this.scope = scope;
  }

  public MetaMetadata getResolvedArgMmd()
	{
		return resolvedArgMmd;
	}

	public void setResolvedArgMmd(MetaMetadata resolvedArgMmd)
	{
		this.resolvedArgMmd = resolvedArgMmd;
	}

	public MetaMetadata getResolvedExtendsMmd()
	{
		return resolvedExtendsMmd;
	}

	public void setResolvedExtendsMmd(MetaMetadata resolvedExtendsMmd)
	{
		this.resolvedExtendsMmd = resolvedExtendsMmd;
	}

	@Override
  public String key()
  {
    return name;
  }

}
