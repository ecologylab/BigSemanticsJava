package ecologylab.bigsemantics.compiler;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.collecting.MetaMetadataRepositoryLocator;
import ecologylab.bigsemantics.metadata.scalar.types.SemanticsTypes;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepositoryLoader;
import ecologylab.serialization.JSONTools;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;

public class TestSerializingPostInheritanceRepository
{

  Map<StringFormat, String> strs;

  @Before
  public void prepareSerializedStrings() throws IOException, SIMPLTranslationException
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
    new SemanticsTypes();
    
    File repoDir = MetaMetadataRepositoryLocator.locateRepositoryByDefaultLocations();
    MetaMetadataRepositoryLoader loader = new MetaMetadataRepositoryLoader();
    MetaMetadataRepository repository = loader.loadFromDir(repoDir, Format.XML);

    strs = new HashMap<StringFormat, String>();

    MetaMetadataCompiler.serializeRepositoryIntoFormats(repository, strs);
  }

  @Test
  public void testSerializingPostInheritanceRepositoryToValidXml()
  {
    String xml = strs.get(StringFormat.XML);
    assertTrue(validateXml(xml));
  }

  private static boolean validateXml(String xml)
  {
    return XMLTools.buildDOMFromXMLString(xml) != null;
  }

  @Test
  public void testSerializingPostInheritanceRepositoryToValidJson()
  {
    String json = strs.get(StringFormat.JSON);
    assertTrue(JSONTools.validate(json));
  }

}
