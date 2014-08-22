package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.generic.StringBuilderBaseUtils;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.types.ScalarType;

// FIXME use the stack to print messages, instead of MetaMetadataField.toString()

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

  private MetaMetadataRepository       repository;

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

  private String getFieldStackTrace(MetaMetadataField fieldToInclude)
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

  public boolean handleMmdRepository(MetaMetadataRepository repository)
  {
    this.repository = repository;

    List<MetaMetadata> mmds = new ArrayList<MetaMetadata>(repository.getMetaMetadataCollection());

    RepositoryOrdering ordering = new RepositoryOrderingByGeneration();
    mmds = ordering.orderMetaMetadataForInheritance(mmds);

    for (MetaMetadata mmd : mmds)
    {
      if (!handleMmd(mmd))
      {
        return false;
      }
    }
    return true;
  }

  public boolean handleMmd(MetaMetadata mmd)
  {
    if (search(mmd) < 0)
    {
      push(mmd);

      try
      {
        MetaMetadata superMmd = findSuperMmd(mmd);

        if (superMmd == null)
        {
          if (!MetaMetadata.isRootMetaMetadata(mmd))
          {
            throw new MetaMetadataException("Can't find super type for " + mmd);
            // TODO better error reporting
          }

          // mmd is the root
          mmd.setNewMetadataClass(true);
          for (MetaMetadataField child : mmd.getChildren())
          {
            inheritField(child, null);
          }
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
      }
      finally
      {
        pop();
      }
    }
    return mmd.isInheritDone();
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
  protected boolean mergeAttributes(MetaMetadataField field, MetaMetadataField superField)
  {
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
          String msg = String.format("Attribute inheritance failed: %s.%s from %s",
                                     field,
                                     attributeName,
                                     superField);
          logger.error(msg, e);
        }
      }
    }

    return true;
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
      logger.debug("Field attribute inherited: {}.{} = {}, from {}",
                   field,
                   attributeName,
                   localValue,
                   superField);
    }
  }

  protected boolean mergeChildren(MetaMetadataField field, MetaMetadataField superField)
  {
    Set<String> childrenNames = new HashSet<String>();
    addChildrenNames(field, childrenNames);
    addChildrenNames(superField, childrenNames);

    // first, for all immediate children, merge attributes or copy the field object over from
    // superField.
    // this "breadth-first" process is critical for dealing with recursions.
    for (String childName : childrenNames)
    {
      MetaMetadataField f0 = superField.lookupChild(childName);
      MetaMetadataField f1 = field.lookupChild(childName);
      if (f0 != null && f1 != null && f0 != f1)
      {
        mergeAttributes(f1, f0);
      }
      else if (f0 != null && f1 == null)
      {
        field.childrenMap().put(childName, f0);
      }
    }

    // then, merge further nested structures in those immediate children
    for (String childName : childrenNames)
    {
      MetaMetadataField f0 = superField.lookupChild(childName);
      MetaMetadataField f1 = field.lookupChild(childName);
      if (f1 != null && f0 != f1)
      {
        inheritField(f1, f0);
      }
    }

    return false;
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

  protected boolean inheritField(MetaMetadataField field, MetaMetadataField superField)
  {
    String fieldStackTrace = getFieldStackTrace(field);
    logger.debug("Handling field: " + fieldStackTrace);

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
      if (fieldType != FieldType.SCALAR && fieldType != FieldType.COLLECTION_SCALAR)
      {
        MetaMetadata typeMmd = findTypeMmd(field);
        if (typeMmd == null)
        {
          logger.error("Cannot find typeMmd for {}", field);
          return false;
        }
        
        if (!typeMmd.isInheritDone())
        {
          handleMmd(typeMmd);
        }

        if (superField == null)
        {
          return inheritFromTypeMmd(field, typeMmd);
        }
        else
        {
          return inheritFromSuperField(field, typeMmd, superField);
        }
      }
      field.setInheritDone(true);
    }

    return field.isInheritDone();
  }

  private boolean inheritFromTypeMmd(MetaMetadataField newField, MetaMetadata typeMmd)
  {
    // new field
    if (newField instanceof MetaMetadataCollectionField)
    {
      push(newField);
      try
      {
        MetaMetadataCompositeField elementComposite =
            ((MetaMetadataCollectionField) newField).getPreparedElementComposite();
        return inheritField(elementComposite, typeMmd);
      }
      finally
      {
        pop();
      }
    }
    else
    {
      return inheritField(newField, typeMmd);
    }
  }

  private boolean inheritFromSuperField(MetaMetadataField field,
                                        MetaMetadata fieldTypeMmd,
                                        MetaMetadataField superField)
  {
    if (field instanceof MetaMetadataCollectionField)
    {
      push(field);
      try
      {
        MetaMetadataCompositeField elementComposite =
            ((MetaMetadataCollectionField) field).getPreparedElementComposite();
        MetaMetadataCompositeField superElementComposite =
            ((MetaMetadataCollectionField) superField).getPreparedElementComposite();
        return inheritField(elementComposite, superElementComposite);
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
        return true;
      }
      finally
      {
        pop();
      }
    }
  }

  protected MetaMetadata findSuperMmd(MetaMetadata mmd)
  {
    if (mmd.getSuperMmd() != null)
    {
      return mmd.getSuperMmd();
    }

    // TODO we need a thorough solution for scopes

    String type = mmd.getType();
    String extendsAttribute = mmd.getExtendsAttribute();

    MetaMetadata result = null;

    if (result == null && type != null)
    {
      result = repository.getMMByName(type);
    }

    if (result == null && extendsAttribute != null)
    {
      result = repository.getMMByName(extendsAttribute);
      if (result != null)
      {
        mmd.setNewMetadataClass(true);
      }
    }

    // TODO new scope = current scope (+) result.scope; associate new scope with mmd.

    mmd.setSuperMmd(result);

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

    // TODO we need a thorough solution for scopes

    String type = field.getType();
    String extendsAttribute = field.getExtendsAttribute();

    MetaMetadata result = null;

    if (result == null && type != null)
    {
      result = repository.getMMByName(type);
    }

    if (result == null && extendsAttribute != null)
    {
      result = repository.getMMByName(extendsAttribute);
    }

    // TODO new scope = current scope (+) result.scope; associate new scope with field.

    field.setTypeMmd(result);

    return result;
  }

}
