package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.collections.MultiAncestorScope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.StringBuilderBaseUtils;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.types.ScalarType;

/**
 * 
 * @author quyin
 */
@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class NewInheritanceHandler
{

  private static final Logger          logger;

  static
  {
    logger = LoggerFactory.getLogger(NewInheritanceHandler.class);
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

    MultiAncestorScope<Object> repoScope = new MultiAncestorScope<Object>("repo");
    for (MetaMetadata mmd : mmds)
    {
      repoScope.put(mmd.getName(), mmd);
      mmd.scope().addAncestor(repoScope);
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
    if (search(mmd) < 0)
    {
      logger.debug("{}: Handling...", mmd);

      push(mmd);

      try
      {
        MetaMetadata superMmd = findSuperMmd(mmd);
        mmd.scope().addAncestor(superMmd == null ? null : superMmd.scope());

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
          if (!superMmd.isInheritDone())
          {
            handleMmd(superMmd);
          }

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
   * @return
   */
  protected void mergeAttributes(MetaMetadataField field, MetaMetadataField superField)
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

  protected void mergeChildren(MetaMetadataField field, MetaMetadataField superField)
  {
    logger.debug("{}: Merging children from {}...", field, superField);

    Set<String> childrenNames = new HashSet<String>();
    addChildrenNames(field, childrenNames);
    addChildrenNames(superField, childrenNames);

    // first, for all immediate children, merge attributes or copy the field object over from
    // superField.
    // this "breadth-first" process is critical for dealing with recursions.
    for (String childName : childrenNames)
    {
      MetaMetadataField f0 = superField == null ? null : superField.lookupChild(childName);
      MetaMetadataField f1 = field.lookupChild(childName);
      if (f0 != null && f1 != null && f0 != f1)
      {
        mergeAttributes(f1, f0);
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
        if (f1 instanceof MetaMetadataNestedField)
        {
          MultiAncestorScope<Object> f1scope = ((MetaMetadataNestedField) f1).scope();
          f1scope.addAncestor(((MetaMetadataNestedField) field).scope());
        }
        inheritField(f1, f0);
      }
    }
  }

  private void addChildrenNames(MetaMetadataField field, Set<String> childrenNames)
  {
    if (field != null)
    {
      for (int i = 0; i < field.getChildrenSize(); ++i)
      {
        childrenNames.add(field.getChild(i).getName());
      }
    }
  }

  protected void inheritField(MetaMetadataField field, MetaMetadataField superField)
  {
    String fieldStackTrace = getFieldStackTrace(field);
    logger.debug("{}: Inheriting from {}, stack trace: {}", field, superField, fieldStackTrace);

    if (search(field) < 0)
    {
      if (superField != null)
      {
        if (field.getSuperField() == null)
        {
          field.setSuperField(superField);
        }
        mergeAttributes(field, superField);
      }

      FieldType fieldType = field.getFieldType();
      logger.debug("{}: Field type {}", field, fieldType);
      if (fieldType != FieldType.SCALAR && fieldType != FieldType.COLLECTION_SCALAR)
      {
        MetaMetadataNestedField nested = (MetaMetadataNestedField) field;
        if (superField != null)
        {
          MetaMetadataNestedField superNested = (MetaMetadataNestedField) superField;
          nested.scope().addAncestor(superNested.scope());
        }

        MetaMetadata typeMmd = null;

        if (nested.isInlineDefinition())
        {
          typeMmd = createInlineMmd(nested.metaMetadataCompositeField());
        }
        else
        {
          typeMmd = findTypeMmd(nested);
          nested.scope().addAncestor(typeMmd == null ? null : typeMmd.scope());
        }

        if (typeMmd == null)
        {
          logger.error("{}: Cannot find typeMmd: type={}, extends={}",
                       nested,
                       nested.getType(),
                       nested.getExtendsAttribute());
        }

        if (!typeMmd.isInheritDone())
        {
          handleMmd(typeMmd);
        }

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

  protected MetaMetadata createInlineMmd(MetaMetadataCompositeField composite)
  {
    logger.debug("{}: Creating inline mmd...", composite);

    MultiAncestorScope<Object> scope = composite.scope();

    // determine the new type name
    String typeName = composite.getType();
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
    MultiAncestorScope<Object> parentScope = getParentScope(composite);
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

  private MultiAncestorScope<Object> getParentScope(MetaMetadataCompositeField composite)
  {
    if (composite.getEnclosingCollectionField() == composite.parent())
    {
      return ((MetaMetadataNestedField) composite.parent().parent()).scope();
    }
    return ((MetaMetadataNestedField) composite.parent()).scope();
  }

  protected MetaMetadata findSuperMmd(MetaMetadata mmd)
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

  protected MetaMetadata findTypeMmd(MetaMetadataField field)
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

  private MetaMetadata findMmdFromScope(MetaMetadataField field)
  {
    MultiAncestorScope<Object> scope = field.scope();

    String type = field.getType();
    String extendsAttribute = field.getExtendsAttribute();

    MetaMetadata result = null;
    if (result == null && type != null)
    {
      result = (MetaMetadata) scope.get(type);
    }
    if (result == null && extendsAttribute != null)
    {
      result = (MetaMetadata) scope.get(extendsAttribute);
      if (result != null && field instanceof MetaMetadata)
      {
        ((MetaMetadata) field).setNewMetadataClass(true);
      }
    }

    return result;
  }

  protected void inheritFromTypeMmd(MetaMetadataField newField, MetaMetadata typeMmd)
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

  protected void inheritFromSuperField(MetaMetadataField field,
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

}
