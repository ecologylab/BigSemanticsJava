package ecologylab.semantics.metametadata.test;

import java.io.File;

import ecologylab.semantics.collecting.MetaMetadataRepositoryInit;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataRepositoryLoader;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

public class TestMmdSerialization
{
	
	public static void main(String[] args) throws SIMPLTranslationException
	{
		MetaMetadataRepositoryLoader loader = new MetaMetadataRepositoryLoader();
		
		File repositoryDir = MetaMetadataRepositoryInit.findRepositoryLocation();
		
		MetaMetadataRepository repository = loader.loadFromDir(repositoryDir, Format.XML);
		repository.traverseAndInheritMetaMetadata();
		
		MetaMetadata acmMmd = repository.getMMByName("acm_portal");
		
		SimplTypesScope.serialize(acmMmd, new File("c:/tmp/test_mmd.xml"), Format.XML);
		
		MetaMetadataField authorsField = acmMmd.getChildMetaMetadata().get("authors");
		
		System.out.println(authorsField);
	}

}
