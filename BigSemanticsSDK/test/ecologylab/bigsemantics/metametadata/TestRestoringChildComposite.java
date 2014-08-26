package ecologylab.bigsemantics.metametadata;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

public class TestRestoringChildComposite
{

  private SemanticsSessionScope semanticsSessionScope;

  @Test
  public void testRestoringChildComposite() throws SIMPLTranslationException, IOException
  {
    SimplTypesScope metadataTranslationScope = RepositoryMetadataTranslationScope.get();
    semanticsSessionScope = new SemanticsSessionScope(metadataTranslationScope,
                                                      CybernekoWrapper.class);
    MetaMetadataRepository repository = semanticsSessionScope.getMetaMetadataRepository();
    // now, repository is after inheritance and binding

    for (MetaMetadata globalMmd : repository.values())
    {
      globalMmd.recursivelyRestoreChildComposite();
    }
    for (MmdScope packageMmdScope : repository.getPackageMmdScopes().values())
    {
      for (Object obj : packageMmdScope.values())
      {
        if (obj instanceof MetaMetadata)
        {
          MetaMetadata packageMmd = (MetaMetadata) obj;
          packageMmd.recursivelyRestoreChildComposite();
        }
      }
    }

    SimplTypesScope.serialize(repository,
                              new File("data/test-restore-child-composite.xml"),
                              Format.XML);
  }

}
