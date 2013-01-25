package ecologylab.semantics.metametadata;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.formatenums.Format;

/**
 * 
 * @author quyin
 */
public class TestMetaMetadataRepositoryLoader
{
  
  MetaMetadataRepositoryLoader loader;
  
  @Before
  public void initLoader()
  {
    loader = new MetaMetadataRepositoryLoader();
  }
  
  @Test
  public void testDeserializeRepositoryFile() throws FileNotFoundException, SIMPLTranslationException
  {
    File sampleRepoFile = new File("data/sampleRepositoryFileSearch.xml");
    assertTrue(sampleRepoFile.exists());
    InputStream istream = new FileInputStream(sampleRepoFile);
    List<InputStream> istreams = new ArrayList<InputStream>();
    istreams.add(istream);
    
    List<MetaMetadataRepository> repos = loader.deserializeRepositories(istreams, Format.XML);
    assertEquals(1, repos.size());
    MetaMetadataRepository repo = repos.get(0);
    assertNotNull(repo);
    for (MetaMetadata mmd : repo.repositoryByName)
      System.out.println(mmd.getName());
    MetaMetadata search = repo.getMMByName("search");
    assertNotNull(search);
    MetaMetadata googleSearch = repo.getMMByName("google_search");
    assertNotNull(googleSearch);
  }

}
