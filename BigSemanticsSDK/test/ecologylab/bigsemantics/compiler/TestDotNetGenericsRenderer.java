package ecologylab.bigsemantics.compiler;

import java.io.IOException;

import org.junit.Test;

import ecologylab.bigsemantics.BaseMmdTest;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.builtins.declarations.MetadataBuiltinDeclarationsTranslationScope;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.MmdScope;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.SimplTypesScope;

public class TestDotNetGenericsRenderer extends BaseMmdTest
{

  @Test
  public void testRenderingProdRepo() throws IOException
  {
    DotNetGenericsRenderer renderer = new DotNetGenericsRenderer();

    MetaMetadataRepository repo = loadRealRepository();
    MmdScope repoScope = repo.getMMByName(MetaMetadata.ROOT_MMD_NAME).getScope();

    SimplTypesScope metadataTypeScope = MetadataBuiltinDeclarationsTranslationScope.get();
    for (ClassDescriptor clazz : metadataTypeScope.getClassDescriptors())
    {
      clazz = clazz.getSuperClass();
      if (clazz instanceof MetadataClassDescriptor)
      {
        MetadataClassDescriptor metadataClazz = (MetadataClassDescriptor) clazz;
        StringBuilder sb = new StringBuilder();
        renderer.render(sb, metadataClazz, repoScope);
        if (sb.indexOf("<") > 0)
        {
          System.out.println(sb.toString());
        }
      }
    }
  }

}
