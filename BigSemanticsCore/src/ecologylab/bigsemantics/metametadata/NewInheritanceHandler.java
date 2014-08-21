package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;

import ecologylab.serialization.FieldType;

/**
 * 
 * @author quyin
 */
public class NewInheritanceHandler
{
  
  ArrayList<MetaMetadataField> stack = new ArrayList<MetaMetadataField>();
  
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
  
  public boolean inheritMmd(MetaMetadata mmd)
  {
    if (search(mmd) < 0)
    {
      push(mmd);
      
      MetaMetadata superMmd = findSuperMmd(mmd);
      
      if (superMmd == null)
      {
        // mmd is the root
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
          inheritMmd(superMmd);
        }
        
        mergeAttributes(mmd, superMmd);
        mergeChildren(mmd, superMmd);
      }
      
      mmd.setInheritDone(true);
      pop();
    }
    return mmd.isInheritDone();
  }
  
  public boolean mergeAttributes(MetaMetadataField field, MetaMetadataField superField)
  {
    field.setSuperField(superField);
    
    // TODO
    return false;
  }
  
  public boolean mergeChildren(MetaMetadataField field, MetaMetadataField superField)
  {
    if (superField.getChildren() != null)
    {
      for (MetaMetadataField f0 : superField.getChildren())
      {
        String childName = f0.getName();
        MetaMetadataField f1 = field.lookupChild(childName);
        if (f1.isAuthoredChildOf(field))
        {
          inheritField(f1, f0);
        }
        else
        {
          field.childrenMap().put(childName, f0);
        }
      }
      return true;
    }
    return false;
  }
  
  public boolean inheritField(MetaMetadataField field, MetaMetadataField superField)
  {
    if (search(field) < 0)
    {
      push(field);
      
      MetaMetadata typeMmd = findTypeMmd(field);

      if (superField == null)
      {
        // new field
        FieldType fieldType = field.getFieldType();
        if (fieldType == FieldType.COMPOSITE_ELEMENT || fieldType == FieldType.COLLECTION_ELEMENT)
        {
          return inheritField(field, typeMmd);
        }
      }
      else
      {
        // not new field
        assert superField.isInheritDone() : "Super field inheritance not done: " + superField;
        if (typeMmd == findTypeMmd(superField))
        {
          mergeChildren(field, superField);
        }
        else
        {
          mergeChildren(field, typeMmd);
          mergeChildren(field, superField);
        }
      }
      
      field.setInheritDone(true);
      pop();
    }
    return field.isInheritDone();
  }

  private MetaMetadata findSuperMmd(MetaMetadata mmd)
  {
    // TODO Auto-generated method stub
    return null;
  }

  private MetaMetadata findTypeMmd(MetaMetadataField field)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
