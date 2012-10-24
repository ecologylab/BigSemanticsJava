package ecologylab.semantics.metadata;

import java.util.EmptyStackException;
import java.util.Stack;

import ecologylab.generic.Debug;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataNestedField;
import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.FieldDescriptor;

public class MetadataDeserializationHookStrategy implements
    DeserializationHookStrategy<Object, FieldDescriptor>
{
  SemanticsGlobalScope           semanticsSessionScope;

  Stack<MetaMetadataNestedField> currentMMStack = new Stack<MetaMetadataNestedField>();

  boolean                        polymorphMmd   = false;

  public MetadataDeserializationHookStrategy(SemanticsGlobalScope sss)
  {
    this.semanticsSessionScope = sss;
  }

  @Override
  public void deserializationPreHook(Object e, FieldDescriptor fd)
  {
    if (e instanceof Metadata)
    {
      Metadata deserializedMetadata = (Metadata) e;
      if (currentMMStack.isEmpty())
      {
        MetaMetadataCompositeField deserializationMM = deserializedMetadata.getMetaMetadata();
        currentMMStack.push(deserializationMM);
      }
      else if (fd instanceof MetadataFieldDescriptor)
      {
        MetadataFieldDescriptor mfd = (MetadataFieldDescriptor) fd;
        String mmName = mfd.getMmName();
        MetaMetadataNestedField currentMM = currentMMStack.peek();
        MetaMetadataNestedField childMMNested =
            (MetaMetadataNestedField) currentMM.lookupChild(mmName);
        MetaMetadataCompositeField childMMComposite = null;
        if (childMMNested.isPolymorphicInherently())
        {
          String tagName = deserializedMetadata.getMetadataClassDescriptor().getTagName();
          childMMComposite = semanticsSessionScope.getMetaMetadataRepository().getMMByName(tagName);
          polymorphMmd = true;
        }
        else
        {
          childMMComposite = childMMNested.metaMetadataCompositeField();
        }
        deserializedMetadata.setMetaMetadata(childMMComposite);
        currentMMStack.push(childMMComposite);
      }
      else if (fd instanceof FieldDescriptor)
      {
        String tagName = ((Metadata) e).getMetadataClassDescriptor().getTagName();
        MetaMetadataCompositeField childMMComposite =
            semanticsSessionScope.getMetaMetadataRepository().getMMByName(tagName);
        deserializedMetadata.setMetaMetadata(childMMComposite);
        currentMMStack.push(childMMComposite);
      }

      if (e instanceof Document)
        ((Document) e).setSemanticsSessionScope(semanticsSessionScope);
    }
  }

  @Override
  public void deserializationInHook(Object e, FieldDescriptor fd)
  {
    if (e instanceof Metadata)
    {
      if (polymorphMmd) // for efficiency; if it is not polymorphic case we don't have to look up
                        // mmd at this point of time
      {
        Metadata metadata = (Metadata) e;
        String mmName = metadata.getMetaMetadataName();
        if (mmName != null && mmName.length() > 0)
        {
          MetaMetadata trueMm = semanticsSessionScope.getMetaMetadataRepository()
              .getMMByName(mmName);
          if (trueMm != null)
          {
            Debug.println(String.format("setting [%s].metaMetadata to %s (mm_name=%s)...",
                                        metadata,
                                        trueMm,
                                        mmName));
            metadata.setMetaMetadata(trueMm);
          }
          else
          {
            Debug.warning(this.getClass(),
                          "polymorphicly looking up meta-metadata failed: cannot find mmd named as "
                              + mmName);
          }
        }
        polymorphMmd = false;
      }
    }
  }

  @Override
  public void deserializationPostHook(Object e, FieldDescriptor fd)
  {
    if (e instanceof Metadata)
    {
      if (fd != null && !(fd instanceof MetadataFieldDescriptor))
        Debug.warning(this, "deserializationPostHook(): call with non-metadata field descriptor! " +
            "probably this is a mistake!");
      else
      {
        try
        {
          currentMMStack.pop();
        }
        catch (EmptyStackException exception)
        {
          exception.printStackTrace();
        }
      }
    }
  }

  @Override
  public Object changeObjectIfNecessary(Object o, FieldDescriptor fd)
  {
    return o;
  }

}