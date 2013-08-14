package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.generated.library.urbanspoon.UrbanSpoonSearch;
import ecologylab.bigsemantics.metadata.builtins.ClippableDocument;
import ecologylab.bigsemantics.metadata.builtins.Clipping;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.TextSelfmade;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.StringFormat;


public class TestRepositoryDeSerialization 
{
	@Before
	public void CleanUpSimplTypesScope()
	{
		SimplTypesScope.ResetAllTypesScopes();
	}
	

  @Test
  public void testRepositoryDeSerialization() throws SIMPLTranslationException, FileNotFoundException
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
    // File savedRepoXml = new File(PropertiesAndDirectories.userDocumentDir(), "repo.xml");
    // MetaMetadataRepository repo1 =
    // (MetaMetadataRepository) mmdTScope.deserialize(savedRepoXml, Format.XML);
    for (String mmdName : repo.keySet())
      assertNotNull(repo1.getMMByName(mmdName));

    assertNotNull(getSampleInheritedMmd(repo1));
  }

  @Test
  public void testRepositoryScopeDeSerializationXML() throws SIMPLTranslationException
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;

    SimplTypesScope scope = RepositoryMetadataTranslationScope.get();
    createDerivedScopes(scope);

    SimplTypesScope scopeWithBasic = SimplTypesScope.get("mmd_and_translation_scope", scope, (Class[]) null);

    scopeWithBasic.addTranslation(SimplTypesScope.class);
    scopeWithBasic.addTranslation(ClassDescriptor.class);
    scopeWithBasic.addTranslation(FieldDescriptor.class);
    // SimplTypesScope.augmentTranslationScope(scopeWithBasic);

    StringBuilder serialized = SimplTypesScope.serialize(scope, StringFormat.XML);

    assertNotNull(serialized);
    assertTrue(serialized.length() > 0);

    String serializedString = serialized.toString();

    System.out.println(serializedString);
    
    saveRepositoryToFile(serializedString, "mmd_repo_scope.xml");

    SimplTypesScope scopeFromSerialized = (SimplTypesScope) scopeWithBasic
        .deserialize(serializedString, StringFormat.XML);
    assertNotNull(scopeFromSerialized);
    System.out.println("----------------------- end xml ----------------------");
  }

  @Test
  public void testRepositoryScopeDeSerializationJSON() throws SIMPLTranslationException
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;

    SimplTypesScope scope = RepositoryMetadataTranslationScope.get();
    createDerivedScopes(scope);

    StringBuilder serialized = SimplTypesScope.serialize(scope, StringFormat.JSON);
    SimplTypesScope scopeWithBasic = SimplTypesScope.get("mmd_and_translation_scope", scope, (Class[]) null);
    scopeWithBasic.addTranslation(SimplTypesScope.class);
    scopeWithBasic.addTranslation(ClassDescriptor.class);
    scopeWithBasic.addTranslation(FieldDescriptor.class);
    // SimplTypesScope.augmentTranslationScope(scopeWithBasic);

    assertNotNull(serialized);
    assertTrue(serialized.length() > 0);

    String serializedString = serialized.toString();
    saveRepositoryToFile(serializedString, "mmd_repo_scope.json");

    SimplTypesScope scopeFromSerialized = (SimplTypesScope) scopeWithBasic
        .deserialize(serializedString, StringFormat.JSON);
    assertNotNull(scopeFromSerialized);
  }

  /**
   * Derived scopes are referred to in simpl_scope annotations. They have to be created so that
   * those annotations can be resolved correctly during de/serialization.
   * 
   * @param repositoryMetadata
   */
  void createDerivedScopes(SimplTypesScope repositoryMetadata)
  {
    SimplTypesScope documentsScope = repositoryMetadata.getAssignableSubset("repository_documents",
                                                                            Document.class);
    SimplTypesScope clippingsTypeScope = repositoryMetadata
        .getAssignableSubset("repository_clippings", Clipping.class);
    SimplTypesScope noAnnotationsScope = repositoryMetadata
        .getSubtractedSubset("repository_no_annotations", TextSelfmade.class);
    SimplTypesScope mediaTypesScope = repositoryMetadata
        .getAssignableSubset("repository_media", ClippableDocument.class);
    mediaTypesScope.addTranslation(Clipping.class);
    mediaTypesScope.addTranslation(TextSelfmade.class);
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

    SimplTypesScope basicScope = SimplTypesScope.get("basic+Urban", scope, (Class[]) null);
    basicScope.addTranslation(SimplTypesScope.class);
    basicScope.addTranslation(ClassDescriptor.class);
    basicScope.addTranslation(FieldDescriptor.class);
    // SimplTypesScope.augmentTranslationScope(basicScope);

    SimplTypesScope scopeFromSerialized = (SimplTypesScope) basicScope
        .deserialize(serializedString, StringFormat.XML);
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

    SimplTypesScope basicScope = SimplTypesScope.get("basic+Urban", scope, (Class[]) null);
    basicScope.addTranslation(SimplTypesScope.class);
    basicScope.addTranslation(ClassDescriptor.class);
    basicScope.addTranslation(FieldDescriptor.class);
    // SimplTypesScope.augmentTranslationScope(basicScope);

    SimplTypesScope scopeFromSerialized = (SimplTypesScope) basicScope
        .deserialize(serializedString, StringFormat.JSON);
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
