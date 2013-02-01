package ecologylab.bigsemantics.metametadata;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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
		semanticsSessionScope = new SemanticsSessionScope(metadataTranslationScope, CybernekoWrapper.class);
		MetaMetadataRepository repository = semanticsSessionScope.getMetaMetadataRepository(); // after inheritance and binding
		for (MetaMetadata globalMmd : repository.values())
			globalMmd.recursivelyRestoreChildComposite();
		for (Map<String, MetaMetadata> packageMmdScope : repository.getPackageMmdScopes().values())
		{
			for (MetaMetadata packageMmd : packageMmdScope.values())
				packageMmd.recursivelyRestoreChildComposite();
		}
		
		SimplTypesScope.serialize(repository, new File("data/test-restore-child-composite.xml"), Format.XML);
	}
	
}
