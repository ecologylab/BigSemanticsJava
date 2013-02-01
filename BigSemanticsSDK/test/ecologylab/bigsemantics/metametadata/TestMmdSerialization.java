package ecologylab.bigsemantics.metametadata;

import java.io.File;
import java.io.FileNotFoundException;

import ecologylab.bigsemantics.collecting.MetaMetadataRepositoryLocator;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

public class TestMmdSerialization
{
	
	public static void main(String[] args) throws SIMPLTranslationException, FileNotFoundException
	{
		MetaMetadataRepositoryLoader loader = new MetaMetadataRepositoryLoader();
		
		File repositoryDir = MetaMetadataRepositoryLocator.locateRepositoryByDefaultLocations();
		
		MetaMetadataRepository repository = loader.loadFromDir(repositoryDir, Format.XML);
		repository.traverseAndInheritMetaMetadata();
		
		MetaMetadata acmMmd = repository.getMMByName("acm_portal");
		
		SimplTypesScope.serialize(acmMmd, new File("c:/tmp/test_mmd.xml"), Format.XML);
		
		MetaMetadataField authorsField = acmMmd.getChildMetaMetadata().get("authors");
		
		System.out.println(authorsField);
	}

}
