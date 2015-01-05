package ecologylab.bigsemantics.compiler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MmdCompilerService;
import ecologylab.bigsemantics.metametadata.MmdGenericTypeVar;
import ecologylab.bigsemantics.metametadata.MmdScope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.XMLTools;

/**
 * 
 * @author quyin
 */
public class DotNetGenericsRenderer
{

  static private Logger logger = LoggerFactory.getLogger(DotNetGenericsRenderer.class);
  
  private MmdCompilerService compilerService;
  
  public void setMmdCompilerService(MmdCompilerService compilerService)
  {
    this.compilerService = compilerService;
  }

  /**
   * 
   * @param mmd
   * @param scope
   * @throws IOException
   */
  public void render(Appendable appendable, MetadataClassDescriptor clazz, MmdScope repoScope)
      throws IOException
  {
    MetaMetadata typeMmd = clazz.getDefiningMmd();
    if (typeMmd == null && !findTypeMmd(clazz, repoScope))
    {
      return;
    }

    typeMmd = clazz.getDefiningMmd();
    MmdScope currentScope = typeMmd.getScope();

    MetadataClassDescriptor superClazz = (MetadataClassDescriptor) clazz.getSuperClass();
    if (superClazz != null
        && superClazz.getDefiningMmd() == null
        && !findTypeMmd(superClazz, currentScope))
    {
      return;
    }

    String clazzSimpleName = clazz.getDescribedClassSimpleName();
    appendable.append(clazzSimpleName);
    renderDefinitions(appendable, clazz);

    if (superClazz != null)
    {
      appendable.append(" : ").append(superClazz.getDescribedClassSimpleName());
      renderBindings(appendable, superClazz, currentScope);
    }

    renderWhereClause(appendable, clazz, currentScope);
  }

  private boolean findTypeMmd(MetadataClassDescriptor clazz, MmdScope scope)
  {
    String superClazzName = clazz.getDescribedClassSimpleName();
    String mmdName = XMLTools.getXmlTagName(superClazzName, "Declaration");
    Object typeObj = scope.get(mmdName);
    if (typeObj != null && typeObj instanceof MetaMetadata)
    {
      clazz.setDefiningMmd((MetaMetadata) typeObj);
      return true;
    }
    else
    {
      logger.warn("Cannot find type mmd for {}", clazz);
      return false;
    }
  }

  public void renderDefinitions(Appendable appendable, MetadataClassDescriptor clazz)
      throws IOException
  {
    MetaMetadata mmd = clazz.getDefiningMmd();
    HashMapArrayList<String, MmdGenericTypeVar> gtvs = mmd.getGenericTypeVarsFromScope();

    boolean first = true;
    for (MmdGenericTypeVar gtv : gtvs)
    {
      if (!gtv.isAssignment())
      {
        first = startOrMore(appendable, first);
        appendable.append(gtv.getName());
      }
    }
    if (!first)
    {
      // there has been at least one generic type vars
      appendable.append(">");
    }
  }

  public void renderBindings(Appendable appendable, MetadataClassDescriptor clazz, MmdScope scope)
      throws IOException
  {
    MetaMetadata mmd = clazz.getDefiningMmd();
    HashMapArrayList<String, MmdGenericTypeVar> gtvs = mmd.getGenericTypeVarsFromScope();

    boolean first = true;
    for (MmdGenericTypeVar gtv0 : gtvs)
    {
      String gtvName = gtv0.getName();
      MmdGenericTypeVar gtv = (MmdGenericTypeVar) scope.get(gtvName);
      if (gtv.isAssignment() || gtv.isRebound())
      {
        first = startOrMore(appendable, first);
        if (gtv.isAssignment())
        {
          String argName = gtv.getArg();
          renderTypeNameRecursively(appendable, gtv, scope, argName);
        }
        else
        {
          // gtv is rebounding
          appendable.append(gtvName);
        }
      }
    }
    if (!first)
    {
      // there has been at least one generic type vars
      appendable.append(">");
    }
  }

  public void renderWhereClause(Appendable appendable, MetadataClassDescriptor clazz, MmdScope scope)
      throws IOException
  {
    MetaMetadata mmd = clazz.getDefiningMmd();
    HashMapArrayList<String, MmdGenericTypeVar> gtvs = mmd.getGenericTypeVarsFromScope();

    boolean first = true;
    for (MmdGenericTypeVar gtv : gtvs)
    {
      if (!gtv.isAssignment())
      {
        first = startOrMore(appendable, first, " where ", ", ");
        appendable.append(gtv.getName()).append(" : ");
        String extendsName = gtv.getExtendsAttribute();
        renderTypeNameRecursively(appendable, gtv, scope, extendsName);
      }
    }
  }

  public void renderTypeNameRecursively(Appendable appendable,
                                        MmdGenericTypeVar gtv,
                                        MmdScope scope,
                                        String typeName) throws IOException
  {
    Object typeObj = scope.get(typeName);
    if (typeObj instanceof MmdGenericTypeVar)
    {
      appendable.append(typeName);
    }
    else
    {
      // obj is a mmd
      MetaMetadata typeMmd = (MetaMetadata) typeObj;
      MetadataClassDescriptor typeClazz = typeMmd.getMetadataClassDescriptor();
      addDependency(typeClazz);
      appendable.append(typeClazz.getDescribedClassSimpleName());
      renderBindings(appendable, typeClazz, scope);
    }
  }

  private boolean startOrMore(Appendable appendable, boolean first) throws IOException
  {
    return startOrMore(appendable, first, "<", ", ");
  }

  private boolean startOrMore(Appendable appendable, boolean first, String start, String delim)
      throws IOException
  {
    if (first)
    {
      appendable.append(start);
    }
    else
    {
      appendable.append(delim);
    }
    return false;
  }

  private void addDependency(MetadataClassDescriptor clazz)
  {
    if (compilerService != null)
    {
      compilerService.addCurrentClassDependency(clazz);
    }
  }

}
