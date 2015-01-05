package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class TestConcatenateValues
{

  @Test
  public void testConcatenateValues() throws SIMPLTranslationException
  {
    MetaMetadataScalarField field = new MetaMetadataScalarField();
    field.setName("scalar");
    ArrayList<MetaMetadataValueField> concatValues = new ArrayList<MetaMetadataValueField>();
    MetaMetadataValueField concatValue = new MetaMetadataValueField();
    concatValue.fromScalar = "foo";
    concatValue.constantValue = "bar";
    concatValues.add(concatValue );
    field.setConcatenateValues(concatValues);
    
    System.out.println(field);
    
    SimplTypesScope s = MetaMetadataTranslationScope.get();
    
    @SuppressWarnings("static-access")
	String json = s.serialize(field, StringFormat.JSON).toString();
    System.out.println(json);
    Assert.assertTrue(json.contains("bar"));
  }

}