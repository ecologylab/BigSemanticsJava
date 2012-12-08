package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.cyberneko.CybernekoWrapper;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.generated.library.urbanspoon.UrbanSpoonSearch;
import ecologylab.semantics.metadata.mm_name;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.annotations.FieldUsage;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;

public class TestRepositoryDeSerialization extends Assert
{

  @Test
  public void testRepositoryDeSerialization() throws SIMPLTranslationException
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;

    SemanticsSessionScope scope = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(),
                                                            CybernekoWrapper.class);
    MetaMetadataRepository repo = scope.getMetaMetadataRepository();
    assertNotNull(getSampleInheritedMmd(repo));

    SimplTypesScope mmdTScope = MetaMetadataTranslationScope.get();
    
    StringBuilder sb = mmdTScope.serialize(repo, StringFormat.XML);// has bad hash value
    assertNotNull(sb);
    assertTrue(sb.length() > 0);

    String repoXml = sb.toString();
    saveRepositoryToFile(repoXml, "repo.xml"); // for easier debugging

    MetaMetadataRepository repo1 =
        (MetaMetadataRepository) mmdTScope.deserialize(repoXml, StringFormat.XML);
//    File savedRepoXml = new File(PropertiesAndDirectories.userDocumentDir(), "repo.xml");
//    MetaMetadataRepository repo1 =
//        (MetaMetadataRepository) mmdTScope.deserialize(savedRepoXml, Format.XML);
    for (String mmdName : repo.keySet())
      assertNotNull(repo1.getMMByName(mmdName));
    
    assertNotNull(getSampleInheritedMmd(repo1));
  }
  @Test
  public void testRepositoryScopeDeSerializationXML() throws SIMPLTranslationException
  {
	    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;

	    SimplTypesScope scope = RepositoryMetadataTranslationScope.get();
	    
	    SimplTypesScope scopeWithBasic = SimplTypesScope.get("mmd_and_translation_scope", scope);
	    scopeWithBasic.addTranslation(SimplTypesScope.class);
	    scopeWithBasic.addTranslation(ClassDescriptor.class);
	    scopeWithBasic.addTranslation(FieldDescriptor.class);

	    StringBuilder serialized = SimplTypesScope.serialize(scope, StringFormat.XML);
	    
	    assertNotNull(serialized);
	    assertTrue(serialized.length() > 0);

	    String serializedString = serialized.toString();
	    saveRepositoryToFile(serializedString, "mmd_repo_scope.xml");

	    SimplTypesScope scopeFromSerialized =  (SimplTypesScope) scopeWithBasic.deserialize(serializedString, StringFormat.XML);
	    assertNotNull(scopeFromSerialized);
  }
  
  @Test
  public void testRepositoryScopeDeSerializationJSON() throws SIMPLTranslationException
  {
	    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;

	    SimplTypesScope scope = RepositoryMetadataTranslationScope.get();
	    StringBuilder serialized = SimplTypesScope.serialize(scope, StringFormat.JSON);
	    SimplTypesScope scopeWithBasic = SimplTypesScope.get("mmd_and_translation_scope", scope);
	    scopeWithBasic.addTranslation(SimplTypesScope.class);
	    scopeWithBasic.addTranslation(ClassDescriptor.class);
	    scopeWithBasic.addTranslation(FieldDescriptor.class);
	    
	    assertNotNull(serialized);
	    assertTrue(serialized.length() > 0);

	    String serializedString = serialized.toString();
	    saveRepositoryToFile(serializedString, "mmd_repo_scope.json");

	    SimplTypesScope scopeFromSerialized =  (SimplTypesScope) scopeWithBasic.deserialize(serializedString, StringFormat.JSON);
	    assertNotNull(scopeFromSerialized);
  }
  
  @Test
  public void testSpecificMetadataXML() throws SIMPLTranslationException
  {
	    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
	    
		SimplTypesScope scope = SimplTypesScope.get("urbanSpoonSearch", UrbanSpoonSearch.class);		
	    StringBuilder serialized = SimplTypesScope.serialize(scope, StringFormat.XML);
	    
	    assertNotNull(serialized);
	    assertTrue(serialized.length() > 0);

	    String serializedString = serialized.toString();
	    saveRepositoryToFile(serializedString, "urbanSpoonSearch.xml");

	    SimplTypesScope basicScope = SimplTypesScope.get("basic+Urban", SimplTypesScope.getBasicTranslations(), UrbanSpoonSearch.class);
	    
	    SimplTypesScope scopeFromSerialized =  (SimplTypesScope) basicScope.deserialize(serializedString, StringFormat.XML);
	    assertNotNull(scopeFromSerialized);
  }
  
  @Test
  public void testSpecificMetadataJSON() throws SIMPLTranslationException
  {
	  	SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
		
		SimplTypesScope scope = SimplTypesScope.get("urbanSpoonSearch", UrbanSpoonSearch.class);		
		StringBuilder serialized = SimplTypesScope.serialize(scope, StringFormat.JSON);
		
		assertNotNull(serialized);
		assertTrue(serialized.length() > 0);
		
		String serializedString = serialized.toString();
		saveRepositoryToFile(serializedString, "urbanSpoonSearch.json");
		
		SimplTypesScope basicScope = SimplTypesScope.get("basic+Urban", SimplTypesScope.getBasicTranslations(), UrbanSpoonSearch.class);
		    
		SimplTypesScope scopeFromSerialized =  (SimplTypesScope) basicScope.deserialize(serializedString, StringFormat.JSON);
		assertNotNull(scopeFromSerialized);
  }  

  void saveRepositoryToFile(String repoStr, String fileName)
  {
    File f = new File(PropertiesAndDirectories.userDocumentDir(), fileName);
    FileWriter fw = null;
    try
    {
      fw = new FileWriter(f);
      fw.write(repoStr);
      fw.close();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  MetaMetadata getSampleInheritedMmd(MetaMetadataRepository repository)
  {
    MetaMetadata photostream = repository.getMMByName("photostream");
    MetaMetadataCollectionField stream =
        (MetaMetadataCollectionField) photostream.getChildMetaMetadata().get("stream");
    MetaMetadataCompositeField flickr_photo = stream.getChildComposite();
    MetaMetadata mmd = flickr_photo.getInheritedMmd();
    System.out.println("sampled inheritedMmd: " + mmd);
    return mmd;
  }

}
