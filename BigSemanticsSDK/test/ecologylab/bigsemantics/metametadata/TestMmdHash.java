package ecologylab.bigsemantics.metametadata;

import junit.framework.Assert;

import org.junit.Test;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTypesScope;
import ecologylab.serialization.SimplTypesScope;

public class TestMmdHash
{

  @Test
  public void testAllHash()
  {
		SimplTypesScope metadataTranslationScope = RepositoryMetadataTypesScope.get();
		SemanticsSessionScope semanticsSessionScope =
		    new SemanticsSessionScope(metadataTranslationScope, CybernekoWrapper.class);
		MetaMetadataRepository repository = semanticsSessionScope.getMetaMetadataRepository();
		for (MetaMetadata mmd : repository.values())
		{
		  String hash = mmd.getHashForExtraction();
		  System.out.println(mmd.getName() + " : " + hash);
		}
		Assert.assertEquals("30d", repository.getDefaultCacheLife());
  }

}
