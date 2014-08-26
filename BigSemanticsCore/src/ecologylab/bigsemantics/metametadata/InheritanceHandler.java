package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.generic.StringBuilderBaseUtils;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.types.ScalarType;

/**
 * All inheritance magic happens here!
 * 
 * @author quyin
 */
@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class InheritanceHandler
{

  private static final Logger          logger;

  static
  {
    logger = LoggerFactory.getLogger(InheritanceHandler.class);
  }

  private ArrayList<MetaMetadataField> stack = new ArrayList<MetaMetadataField>();

  public void push(MetaMetadataField field)
  {
    stack.add(field);
  }

  public MetaMetadataField pop()
  {
    return stack.remove(stack.size() - 1);
  }

  public MetaMetadataField top()
  {
    return stack.get(stack.size() - 1);
  }

  public int search(MetaMetadataField field)
  {
    for (int i = 0; i < stack.size(); ++i)
    {
      if (field == stack.get(i))
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * @param fieldToInclude
   *          If this is not null, the result string will include this field as if it was pushed to
   *          the top of the stack. Nothing actually pushed though.
   * @return A string representing the current content of the stack.
   */
  public String getFieldStackTrace(MetaMetadataField fieldToInclude)
  {
    StringBuilder sb = StringBuilderBaseUtils.acquire();
    for (int i = 0; i <= stack.size(); ++i)
    {
      // virtually push fieldToInclude to the stack
      MetaMetadataField field = (i == stack.size()) ? fieldToInclude : stack.get(i);
      if (field instanceof MetaMetadataCompositeField)
      {
        // skip the element composite in a collection field for printing the stack.
        MetaMetadataCollectionField enclosingCollectionField =
            ((MetaMetadataCompositeField) field).getEnclosingCollectionField();
        if (enclosingCollectionField != null
            && enclosingCollectionField == stack.get(i - 1))
        {
          continue;
        }
      }
      if (field != null)
      {
        sb.append(i == 0 ? "" : ".").append(field.getName());
      }
    }
    String result = sb.toString();
    StringBuilderBaseUtils.release(sb);
    return result;
  }

  public void handleMmdRepository(MetaMetadataRepository repository)
  {
    List<MetaMetadata> mmds = new ArrayList<MetaMetadata>(repository.getMetaMetadataCollection());

    MmdScope repoScope = new MmdScope("repo");
    for (MetaMetadata mmd : mmds)
    {
      repoScope.put(mmd.getName(), mmd);
      mmd.scope().addAncestor(repoScope);
      mmd.setRepository(repository);
    }

    RepositoryOrdering ordering = new RepositoryOrderingByGeneration();
    mmds = ordering.orderMetaMetadataForInheritance(mmds);

    for (MetaMetadata mmd : mmds)
    {
      handleMmd(mmd);
    }
  }

  public void handleMmd(MetaMetadata mmd)
  {
    if (mmd != null && !mmd.isInheritDone() && search(mmd) < 0)
    {
      logger.debug("{}: Handling mmd {}", mmd, mmd);

      push(mmd);

      try
      {
        MetaMetadata superMmd = findSuperMmd(mmd);
        inheritScope(mmd, superMmd);

        if (superMmd == null)
        {
          if (!MetaMetadata.isRootMetaMetadata(mmd))
          {
            throw new MetaMetadataException("Can't find super type for " + mmd);
          }

          // mmd is the root
          mmd.setNewMetadataClass(true);
          mergeChildren(mmd, null);
        }
        else
        {
          // mmd is not the root
          handleMmd(superMmd);

          mergeAttributes(mmd, superMmd);
          mergeChildren(mmd, superMmd);
        }

        mmd.setInheritDone(true);
        logger.debug("{}: Inheritance done", mmd);
      }
      finally
      {
        pop();
      }
    }
  }

  /**
   * Merge attributes from superField to field.
   * 
   * Attributes include not only scalar values (e.g. hide, label) but also some collections (such as
   * xpaths). If an attribute is a collection, it is expected that its elements have a by-content
   * equals() defined.
   * 
   * Attributes explicitly specified on field will take precedence.
   * 
   * @param field
   * @param superField
   */
  private void mergeAttributes(MetaMetadataField field, MetaMetadataField superField)
  {
    logger.debug("{}: Merging attributes from {}...", field, superField);

    MetaMetadataClassDescriptor superClassDescriptor =
        (MetaMetadataClassDescriptor) ClassDescriptor.getClassDescriptor(superField);
    MetaMetadataClassDescriptor classDescriptor =
        (MetaMetadataClassDescriptor) ClassDescriptor.getClassDescriptor(field);

    for (MetaMetadataFieldDescriptor attributeFieldDescriptor : superClassDescriptor)
    {
      String attributeName = attributeFieldDescriptor.getName();
      if (classDescriptor.getFieldDescriptorByFieldName(attributeName) == null)
      {
        continue;
      }

      if (attributeFieldDescriptor.isInheritable())
      {
        try
        {
          mergeAttributeHelper(field, superField, attributeFieldDescriptor);
        }
        catch (Exception e)
        {
          String msg = String.format("%s.%s: Attribute inheritance failed from %s",
                                     field,
                                     attributeName,
                                     superField);
          logger.error(msg, e);
        }
      }
    }
  }

  /**
   * Merge one attribute.
   * 
   * @param field
   * @param superField
   * @param attributeFieldDescriptor
   */
  private void mergeAttributeHelper(MetaMetadataField field,
                                    MetaMetadataField superField,
                                    MetaMetadataFieldDescriptor attributeFieldDescriptor)
  {
    String attributeName = attributeFieldDescriptor.getName();
    Object superValue = attributeFieldDescriptor.getValue(superField);
    Object localValue = attributeFieldDescriptor.getValue(field);

    boolean attributeInherited = false;

    if (attributeFieldDescriptor.isCollection())
    {
      // Append elements from inheritFrom to this field's list
      List superList = (List) superValue;
      if (superList != null && superList.size() > 0)
      {
        List localList = (List) localValue;
        if (localList == null)
        {
          localList = new ArrayList();
          attributeFieldDescriptor.setField(field, localList);
        }
        for (Object element : superList)
        {
          // List.contains() uses equals() to compare, so element should have its own equals()
          // defined, otherwise there can be problems!
          if (!localList.contains(element))
          {
            localList.add(element);
            attributeInherited = true;
          }
        }
      }
    }
    else
    {
      // For a scalar field, scalarType will be the scalarType for that field.
      // For a composite field or a collection-of-composite field, scalarType will be null.
      // For a collection-of-scalar field, scalarType will be the child scalarType for that
      // field.
      ScalarType scalarType = attributeFieldDescriptor.getScalarType();

      if (scalarType != null
          && scalarType.isDefaultValue(localValue)
          && !scalarType.isDefaultValue(superValue))
      {
        attributeFieldDescriptor.setField(field, superValue);
        attributeInherited = true;
      }
    } // if (fieldDescriptor.isCollection)

    if (attributeInherited)
    {
      logger.debug("{}.{}: Field attribute inherited from {}: {}",
                   field,
                   attributeName,
                   superField,
                   localValue);
    }
  }

  /**
   * Merge children, in a breadth-first way: immediate children get processed first, then the next
   * generation, and so on.
   * 
   * @param field
   * @param superField
   */
  private void mergeChildren(MetaMetadataField field, MetaMetadataField superField)
  {
    logger.debug("{}: Merging children from {}...", field, superField);

    Set<String> childrenNames = new HashSet<String>();
    collectChildrenNames(field, childrenNames);
    collectChildrenNames(superField, childrenNames);

    Set<MetaMetadataField> reincarnated = new HashSet<MetaMetadataField>();

    // first, for all immediate children, merge attributes or copy the field object over from
    // superField.
    // this "breadth-first" process is critical for dealing with recursions.
    for (String childName : childrenNames)
    {
      MetaMetadataField f0 = superField == null ? null : superField.lookupChild(childName);
      MetaMetadataField f1 = field.lookupChild(childName);

      if (f1 != null)
      {
        f1.scope().addAncestor(field.scope());
      }

      if (field instanceof MetaMetadata && f0 == null && f1 != null)
      {
        // f1 is a newly declared field
        f1.setDeclaringMmd((MetaMetadata) field);
      }

      if (f0 != null && f1 == null && isUsingGenerics(f0))
      {
        // if the super field is using generics, we will need to re-evaluate generic type vars, and
        // this can change the type that is being used for f1
        f1 = ReflectionTools.getInstance(f0.getClass());
        f1.setName(childName);
        f1.setParent(field);
        if (f1 instanceof MetaMetadataCollectionField)
        {
          ((MetaMetadataCollectionField) f1).createElementComposite();
        }
        field.getChildrenMap().put(childName, f1);
        reincarnated.add(f1);
        logger.debug("{}: Reincarnated since superField {} is generic", f1, f0);
      }

      if (f0 != null && f1 != null && f0 != f1)
      {
        mergeAttributes(f1, f0);
        // set superField early. this is important because in the next for loop, when we do
        // inheritField(f1, f0), it is possible that f0 is not yet done inheritance. in that case,
        // we may want to deal with f0 first.
        f1.setSuperField(f0);
      }
      else if (f0 != null && f1 == null)
      {
        field.childrenMap().put(childName, f0);
        logger.debug("{}: Directly put {} into it", field, f0);
      }
    }

    // then, merge further nested structures in those immediate children
    for (String childName : childrenNames)
    {
      MetaMetadataField f0 = superField == null ? null : superField.lookupChild(childName);
      MetaMetadataField f1 = field.lookupChild(childName);
      if (f1 != null && f0 != f1)
      {
        FieldType f1type = f1.getFieldType();
        if (f1type != FieldType.SCALAR && f1type != FieldType.COLLECTION_SCALAR)
        {
          // currently, we only do scope inheritance for nested fields.
          f1.scope().addAncestor(field.scope());
        }
        inheritField(f1, f0);

        if (reincarnated.contains(f1) && !isTypeDifferent(f1, f0))
        {
          field.getChildrenMap().put(childName, f0);
          logger.debug("{}: Field unchanged, reincarnated instance destroyed", f1);
        }
      }
    }
  }

  private void collectChildrenNames(MetaMetadataField field, Set<String> childrenNames)
  {
    if (field != null)
    {
      for (int i = 0; i < field.getChildrenSize(); ++i)
      {
        childrenNames.add(field.getChild(i).getName());
      }
    }
  }

  /**
   * Entrance method for inheriting attributes and children from superField to field. It merges
   * attributes, deals with scopes, finds the typeMmd, then call other methods to do the actual
   * dirty work.
   * 
   * @param field
   *          The field being processed.
   * @param superField
   *          Can be null.
   */
  private void inheritField(MetaMetadataField field, MetaMetadataField superField)
  {
    String fieldStackTrace = getFieldStackTrace(field);
    logger.debug("{}: Inheriting from {}, stack trace: {}", field, superField, fieldStackTrace);

    if (search(field) < 0)
    {
//      if (superField != null)
//      {
//        if (field.getSuperField() == null)
//        {
//          field.setSuperField(superField);
//        }
//        mergeAttributes(field, superField);
//      }

      FieldType fieldType = field.getFieldType();
      logger.debug("{}: Field type {}", field, fieldType);
      if (fieldType != FieldType.SCALAR && fieldType != FieldType.COLLECTION_SCALAR)
      {
        MetaMetadataNestedField nested = (MetaMetadataNestedField) field;
        if (superField != null)
        {
          inheritScope(field, superField);
        }

        MetaMetadata typeMmd = null;
        if (nested.isInlineDefinition())
        {
          typeMmd = createInlineMmd(nested.metaMetadataCompositeField());
        }
        else
        {
          typeMmd = findTypeMmd(nested);
          inheritScope(nested, typeMmd);
        }
        if (typeMmd == null)
        {
          throw new MetaMetadataException("Cannot find typeMmd for " + field
                                          + ", type=" + nested.getType()
                                          + ", extends=" + nested.getExtendsAttribute());
        }

        handleMmd(typeMmd);

        if (superField == null)
        {
          inheritFromTypeMmd(nested, typeMmd);
        }
        else
        {
          inheritFromSuperField(nested, typeMmd, superField);
        }
      }
      field.setInheritDone(true);
      logger.debug("{}: Inheritance done", field);
    }
  }

  /**
   * Create inline mmd type using a composite.
   * 
   * @param composite
   * @return The created inline mmd.
   */
  private MetaMetadata createInlineMmd(MetaMetadataCompositeField composite)
  {
    logger.debug("{}: Creating inline mmd...", composite);

    MmdScope scope = composite.scope();

    // determine the new type name
    String typeName = composite.getTypeOrName();
    if (typeName == null)
    {
      throw new MetaMetadataException("Cannot create inline type for " + composite
                                      + ", did you specify type or child_type?");
    }
    if (scope.get(typeName) != null)
    {
      throw new MetaMetadataException("Type name " + typeName + " already in use: " + composite
                                      + ", use another type name.");
    }

    // determine which existing mmd to inherit from
    String extendsName = composite.getExtendsAttribute();
    Object superMmdObj = scope.get(extendsName);
    if (superMmdObj == null)
    {
      throw new MetaMetadataException("Cannot find super type " + extendsName + " for " + composite);
    }
    if (!(superMmdObj instanceof MetaMetadata))
    {
      throw new MetaMetadataException(extendsName + " is not a mmd type: " + composite);
    }
    MetaMetadata superMmd = (MetaMetadata) superMmdObj;
    logger.debug("{}: Super type of inline mmd: {}", composite, superMmd);

    // create inline mmd and add it to parent's scope
    MetaMetadata inlineMmd = createInlineMmdHelper(composite, typeName, superMmd);
    MmdScope parentScope = getParentScope(composite);
    parentScope.put(inlineMmd.getName(), inlineMmd);

    handleMmd(inlineMmd);

    return inlineMmd;
  }

  private MetaMetadata createInlineMmdHelper(MetaMetadataCompositeField composite,
                                             String typeName,
                                             MetaMetadata superMmd)
  {
    // create and set inlineMmd attributes
    MetaMetadata inlineMmd = new MetaMetadata();
    inlineMmd.setName(typeName);
    inlineMmd.setPackageName(composite.packageName());
    inlineMmd.setType(null);
    inlineMmd.setTypeMmd(superMmd);
    inlineMmd.setExtendsAttribute(superMmd.getName());
    inlineMmd.setRepository(composite.getRepository());
    inlineMmd.scope().addAncestors(superMmd.scope(), composite.scope());
    if (composite.getSchemaOrgItemtype() != null)
    {
      inlineMmd.setSchemaOrgItemtype(composite.getSchemaOrgItemtype());
    }
    inlineMmd.setNewMetadataClass(true);
    logger.debug("{}: Inline mmd created", inlineMmd);

    // set composite attributes and fields
    composite.setInlineMmd(inlineMmd);
    composite.setTypeMmd(inlineMmd);
    composite.setType(inlineMmd.getName());
    composite.setExtendsAttribute(null);
    if (composite.getTag() == null)
    {
      composite.setTag(inlineMmd.getName()); // keep the tag name
    }
    HashMapArrayList<String, MetaMetadataField> kids = composite.getChildrenMap();
    for (String kidKey : kids.keySet())
    {
      MetaMetadataField kid = kids.get(kidKey);
      inlineMmd.getChildrenMap().put(kidKey, kid);
      kid.setParent(inlineMmd);
    }
    kids.clear();
    logger.debug("{}: Attributes and fields set after creating inline mmd", composite);

    return inlineMmd;
  }

  /**
   * A convenient method to deal with the element composite object inside a collection field.
   * 
   * @param composite
   * @return
   */
  private MmdScope getParentScope(MetaMetadataCompositeField composite)
  {
    if (composite.getEnclosingCollectionField() == composite.parent())
    {
      return ((MetaMetadataNestedField) composite.parent().parent()).scope();
    }
    return ((MetaMetadataNestedField) composite.parent()).scope();
  }

  /**
   * Finds the superMmd for a given mmd. The superMmd is specified by type or extends.
   * 
   * @param mmd
   * @return
   */
  private MetaMetadata findSuperMmd(MetaMetadata mmd)
  {
    if (mmd.getSuperMmd() != null)
    {
      return mmd.getSuperMmd();
    }
    MetaMetadata result = findMmdFromScope(mmd);
    mmd.setSuperMmd(result);
    logger.debug("{}: Super type {}", mmd, result);
    return result;
  }

  /**
   * Finds the typeMmd for a (nested) field. The typeMmd is the mmd that is used as the type of a
   * composite field or the child type of a collection field.
   * 
   * @param field
   * @return
   */
  private MetaMetadata findTypeMmd(MetaMetadataField field)
  {
    if (field instanceof MetaMetadata)
    {
      return (MetaMetadata) field;
    }
    if (field.getTypeMmd() != null)
    {
      return field.getTypeMmd();
    }
    MetaMetadata result = findMmdFromScope(field);
    field.setTypeMmd(result);
    logger.debug("{}: Type mmd {}", field, result);
    return result;
  }

  /**
   * Helper method that looks for mmd from the field's scope.
   * 
   * @param field
   * @return
   */
  private MetaMetadata findMmdFromScope(MetaMetadataField field)
  {
    MmdScope scope = field.scope();

    String type = field.getType();
    String extendsAttribute = field.getExtendsAttribute();

    MetaMetadata result = null;
    if (result == null && type != null)
    {
      result = resolveTypeName(scope, type);
      logger.debug("{}: Type name {} resolved to {}", field, type, result);
    }
    if (result == null && extendsAttribute != null)
    {
      result = resolveTypeName(scope, extendsAttribute);
      logger.debug("{}: Type name {} resolved to {}", field, extendsAttribute, result);
      if (result != null && field instanceof MetaMetadata)
      {
        ((MetaMetadata) field).setNewMetadataClass(true);
      }
    }

    return result;
  }

  /**
   * Resolve a name to a mmd. The name can be a concrete mmd name or a generic type var. In the
   * later case, this method will use the scope to resolve the generic type var to the most general
   * concrete mmd type that applies.
   * 
   * @param scope
   * @param typeName
   * @return
   */
  private MetaMetadata resolveTypeName(MmdScope scope, String typeName)
  {
    Object obj = scope.get(typeName);
    if (obj instanceof MetaMetadata)
    {
      return (MetaMetadata) obj;
    }
    else if (obj instanceof MmdGenericTypeVar)
    {
      MmdGenericTypeVar gtv = (MmdGenericTypeVar) obj;
      Map<String, MmdGenericTypeVar> nestedGtvScope = gtv.getNestedGenericTypeVarScope();
      if (nestedGtvScope != null && nestedGtvScope.size() > 0)
      {
        scope.putAll(nestedGtvScope);
      }
      if (gtv.getArg() != null)
      {
        return resolveTypeName(scope, gtv.getArg());
      }
      else if (gtv.getExtendsAttribute() != null)
      {
        return resolveTypeName(scope, gtv.getExtendsAttribute());
      }
      else
      {
        // neither arg nor extends specified for gtv
        return resolveTypeName(scope, MetaMetadata.ROOT_MMD_NAME);
      }
    }
    else
    {
      throw new MetaMetadataException("MetaMetadata or GenericTypeVar expected, "
                                      + "but got " + obj + " with " + typeName);
    }
  }

  /**
   * Helper method that deals with collection fields and their element composites.
   * 
   * @param newField
   * @param typeMmd
   */
  private void inheritFromTypeMmd(MetaMetadataField newField, MetaMetadata typeMmd)
  {
    logger.debug("{}: inheriting from type mmd {}", newField, typeMmd);
    if (newField instanceof MetaMetadataCollectionField)
    {
      push(newField);
      try
      {
        MetaMetadataCompositeField elementComposite =
            ((MetaMetadataCollectionField) newField).getPreparedElementComposite();
        inheritField(elementComposite, typeMmd);
      }
      finally
      {
        pop();
      }
    }
    else
    {
      inheritField(newField, typeMmd);
    }
  }

  /**
   * Inherit attributes and children from superField. This method also deals with cases where a more
   * specific type (that is different from the type used on the superField) is used on the
   * (sub)field.
   * 
   * @param field
   * @param fieldTypeMmd
   * @param superField
   */
  private void inheritFromSuperField(MetaMetadataField field,
                                     MetaMetadata fieldTypeMmd,
                                     MetaMetadataField superField)
  {
    logger.debug("{}: inheriting from super field {}", field, superField);
    if (field instanceof MetaMetadataCollectionField)
    {
      push(field);
      try
      {
        MetaMetadataCompositeField elementComposite =
            ((MetaMetadataCollectionField) field).getPreparedElementComposite();
        MetaMetadataCompositeField superElementComposite =
            ((MetaMetadataCollectionField) superField).getPreparedElementComposite();
        inheritField(elementComposite, superElementComposite);
      }
      finally
      {
        pop();
      }
    }
    else
    {
      push(field);
      try
      {
        if (!(superField instanceof MetaMetadata) && !superField.isInheritDone())
        {
          // if superField is not done, try to deal with superField first
          // note that superField can be a MetaMetadata and we don't want to deal with that case.
          inheritField(superField, superField.getSuperField());
        }

        if (fieldTypeMmd == findTypeMmd(superField))
        {
          // not changing type on the sub field.
          mergeAttributes(field, superField);
          mergeChildren(field, superField);
        }
        else
        {
          // changing type on the sub field.
          mergeAttributes(field, superField);
          mergeChildren(field, fieldTypeMmd);
          mergeChildren(field, superField);
        }
        field.setInheritDone(true);
      }
      finally
      {
        pop();
      }
    }
  }

  /**
   * Key method to handle lexical scopes. For generic type vars, it also checks bounds.
   * 
   * @param dest
   * @param src
   */
  private void inheritScope(ScopeProvider dest, ScopeProvider src)
  {
    if (src == null)
    {
      return;
    }

    MmdScope srcScope = src.getScope();

    if (srcScope != null)
    {
      MmdScope destScope = dest.scope();

      if (srcScope.size() > 0)
      {
        // handle generic type vars (inheriting, checking bounds, etc)
        logger.debug("{}: Inheriting and validating generic type vars from {}", dest, src);
        for (Object obj : srcScope.values())
        {
          if (obj instanceof MmdGenericTypeVar)
          {
            MmdGenericTypeVar gtv0 = (MmdGenericTypeVar) obj;

            if (gtv0.nothingSpecified())
            {
              gtv0.setExtendsAttribute(MetaMetadata.ROOT_MMD_NAME);
            }

            String gtvName = gtv0.getName();
            MmdGenericTypeVar gtv1 = (MmdGenericTypeVar) destScope.get(gtvName);
            if (gtv1 != null)
            {
              if (gtv1.nothingSpecified())
              {
                // a GTV can be omitted if nothing changes about it. in this case, ignore it.
                destScope.remove(gtvName);
              }
              else
              {
                // otherwise, inherited nested type vars and check bounds
                inheritScope(gtv1, gtv0);
                validateGenericTypeVar(dest, src, gtvName, gtv1, gtv0);
              }
            } // if (gtv1 != null)
          } // if (obj instanceof MmdGenericTypeVar)
        } // for
      } // if (srcScope.size() > 0)

      // handle general scope
      destScope.addAncestor(srcScope);
    }
  }

  /**
   * Validate generic type var bounds. Parameter dest, src, and gtvName are just for error
   * reporting.
   * 
   * @param dest
   * @param src
   * @param gtvName
   * @param destGtv
   * @param srcGtv
   */
  private void validateGenericTypeVar(ScopeProvider dest,
                                      ScopeProvider src,
                                      String gtvName,
                                      MmdGenericTypeVar destGtv,
                                      MmdGenericTypeVar srcGtv)
  {
    if (srcGtv.isAssignment() && destGtv.isAssignment())
    {
      // note that when evaluating srcMmd, we need to use destGtv because that is what will be used
      // when determining field type. and srcGtv.scope should be an ancestor of destGtv.scope.
      MetaMetadata srcMmd = resolveTypeName(destGtv.getScope(), srcGtv.getArg());
      MetaMetadata destMmd = resolveTypeName(destGtv.getScope(), destGtv.getArg());
      if (srcMmd != destMmd)
      {
        throw new MetaMetadataException("Incompatiable assignments to generic type var "
                                        + gtvName + " from " + src + " to " + dest);
      }
    }
    else if (srcGtv.isAssignment() && destGtv.isBound())
    {
      throw new MetaMetadataException("Generic type var " + gtvName + " already assigned in "
                                      + src + ", cannot respecify bound in " + dest);
    }
    else if (srcGtv.isBound() && destGtv.isAssignment())
    {
      if (!checkAssignmentWithBounds(destGtv, srcGtv))
      {
        throw new MetaMetadataException("Generic type bound(s) not satisfied for "
                                        + gtvName + " in " + dest);
      }
    }
    else
    {
      if (!checkBoundsWithBounds(destGtv, srcGtv))
      {
        throw new MetaMetadataException("Generic type bound(s) not compatible for "
                                        + gtvName + " in " + dest);
      }
    }

    logger.debug("{}: Generic type var {} validated", dest, destGtv);
  }

  private boolean checkAssignmentWithBounds(MmdGenericTypeVar argGtv, MmdGenericTypeVar boundGtv)
  {
    MmdScope scope = argGtv.getScope();
    MetaMetadata argMmd = resolveTypeName(scope, argGtv.getArg());
    handleMmd(argMmd);
    MetaMetadata lowerBoundMmd = resolveTypeName(scope, boundGtv.getExtendsAttribute());
    handleMmd(lowerBoundMmd);
    boolean satisfyLowerBound = lowerBoundMmd == null || argMmd.isDerivedFrom(lowerBoundMmd);

    // future work: upper bound

    return satisfyLowerBound;
  }

  private boolean checkBoundsWithBounds(MmdGenericTypeVar local, MmdGenericTypeVar other)
  {
    MmdScope scope = local.getScope();
    MetaMetadata lowerBoundMmdLocal = resolveTypeName(scope, local.getExtendsAttribute());
    handleMmd(lowerBoundMmdLocal);
    MetaMetadata lowerBoundMmdOther = resolveTypeName(scope, other.getExtendsAttribute());
    handleMmd(lowerBoundMmdOther);

    boolean lowerBoundsCompatible = lowerBoundMmdOther == null
                                    || lowerBoundMmdLocal.isDerivedFrom(lowerBoundMmdOther);

    // future work: upper bound

    return lowerBoundsCompatible;
  }

  /**
   * Determines if a field involves generics. If so, the inheritance code may need to re-evaluate
   * the generic type vars.
   * 
   * @param field
   * @return
   */
  private boolean isUsingGenerics(MetaMetadataField field)
  {
    Map<String, MmdGenericTypeVar> gtvScope = field.getGenericTypeVars();
    if (gtvScope != null && gtvScope.size() > 0)
    {
      return true;
    }

    Object obj = field.scope().get(field.getType());
    if (obj == null)
    {
      obj = field.scope().get(field.getExtendsAttribute());
    }

    if (obj instanceof MmdGenericTypeVar)
    {
      return true;
    }

    return false;
  }

  /**
   * @param f1
   * @param f0
   * @return If the type of the two fields, or the type of any of their children, is different.
   */
  private boolean isTypeDifferent(MetaMetadataField f1, MetaMetadataField f0)
  {
    if (f1 != null && f0 != null)
    {
      if (f0.getTypeMmd() != f1.getTypeMmd())
      {
        return true;
      }
      for (MetaMetadataField child1 : f1)
      {
        MetaMetadataField child0 = f0.lookupChild(child1.getName());
        if (child0 == null || child0.getTypeMmd() != child1.getTypeMmd())
        {
          return true;
        }
      }
    }
    return false;
  }

}
