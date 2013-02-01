package ecologylab.simpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.metadata.builtins.MetadataBuiltinsTypesScope;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.JSONTools;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;

public class DeSerializingDescriptors extends Assert
{

  @Before
  public void init()
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
  }

  @Test
  public void deSerializeDescriptorsJSON() throws SIMPLTranslationException, IOException
  {
    SimplTypesScope tscope = SimplTypesScope.get("test-de/serialize descriptors in json",
                                                 FieldDescriptor.class,
                                                 ClassDescriptor.class,
                                                 SimplTypesScope.class);
    String json = SimplTypesScope.serialize(tscope, StringFormat.JSON).toString();
    System.out.println("serialized json string:\n" + json);
    assertTrue(JSONTools.validate(json));

//    File testScopeFile = new File("data/test-deserialize-descriptors-scope.json");
//    assertTrue(testScopeFile.exists());
//    json = loadJSONStringFromFile(testScopeFile);

     SimplTypesScope newTscope = (SimplTypesScope) tscope.deserialize(json, StringFormat.JSON);
//    SimplTypesScope newTscope = (SimplTypesScope) tscope.deserialize(testScopeFile, Format.JSON);
    assertNotNull(newTscope);
    assertNotNull(newTscope.getClassDescriptorBySimpleName("FieldDescriptor"));
    assertNotNull(newTscope.getClassDescriptorBySimpleName("ClassDescriptor"));
    assertNotNull(newTscope.getClassDescriptorBySimpleName("SimplTypesScope"));
  }

  @Test
  public void deSerializeMetadataClassDescriptorJSON() throws SIMPLTranslationException
  {
    SimplTypesScope tscope = MetadataBuiltinsTypesScope.get();
    ClassDescriptor cd = tscope.getClassDescriptorBySimpleName("Metadata");
    assertNotNull(cd);
    String json = SimplTypesScope.serialize(cd, StringFormat.JSON).toString();
    System.out.println("serialized json string:\n" + json);
    assertTrue(JSONTools.validate(json));
  }

  @Test
  public void deSerializeMetadataBuiltInScopeJSON() throws SIMPLTranslationException
  {
    SimplTypesScope tscope = MetadataBuiltinsTypesScope.get();
    String json = SimplTypesScope.serialize(tscope, StringFormat.JSON).toString();
    System.out.println("serialized json string:\n" + json);
    assertTrue(JSONTools.validate(json));
  }

  static String loadJSONStringFromFile(File file) throws IOException
  {
    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader(new FileReader(file));
    while (true)
    {
      String line = br.readLine();
      if (line == null)
        break;
      sb.append(line.trim());
    }
    return sb.toString();
  }

}
