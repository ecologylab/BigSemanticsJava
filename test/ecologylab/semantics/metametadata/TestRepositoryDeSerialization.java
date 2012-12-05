package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.cyberneko.CybernekoWrapper;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
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
    StringBuilder sb = mmdTScope.serialize(repo, StringFormat.XML);
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
