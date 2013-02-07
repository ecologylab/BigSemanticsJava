package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.serialization.SimplTypesScope;

@RunWith(Parameterized.class)
public class TestURLToSelectorMappings
{

  private String testUrl = "";

  private String expectedMmdName;

  private String foundMmdName;

  public TestURLToSelectorMappings(Object[] tripple)
  {
    this.testUrl = (String) tripple[0];
    this.expectedMmdName = (String) tripple[1];
    this.foundMmdName = (String) tripple[2];
  }

  // Found structrue from
  // http://stackoverflow.com/questions/358802/junit-test-with-dynamic-number-of-tests
  @Parameters
  public static Collection<Object[]> getFiles()
  {
    // Setup that constucts a parameterized test for each example URL
    Collection<Object[]> params = new ArrayList<Object[]>();

    SimplTypesScope metadataTScope = RepositoryMetadataTranslationScope.get();
    SemanticsSessionScope sss = new SemanticsSessionScope(metadataTScope, null);
    MetaMetadataRepository repository = sss.getMetaMetadataRepository();

    Collection<MetaMetadata> mmds = repository.getMetaMetadataCollection();

    for (MetaMetadata mmd : mmds)
    {
      ArrayList<ExampleUrl> examples = mmd.getExampleUrls();
      if (examples != null)
      {
        for (ExampleUrl example : examples)
        {
          if (example.getUrl() == null)
          {
            continue;
          }
          Document doc = repository.constructDocument(example.getUrl());
          // System.out.println(mmd.getName());
          // System.out.println(doc.getMetaMetadataName());
          // System.out.println(example.getUrl().toString());
          Object[] arr = new Object[]
          {
              new Object[]
              {
                  example.getUrl().toString(),
                  mmd.getName(),
                  doc.getMetaMetadataName()
              }
          };
          params.add(arr);
        }
      }
    }

    return params;
  }

  @Test
  public void test()
  {
    String message = String.format("MMD named %s expects URL [%s] to reslove to itself.\n",
                                   expectedMmdName,
                                   testUrl);
    message += " Please check the selector(s).";
    assertEquals(message, expectedMmdName, foundMmdName);
  }
}
