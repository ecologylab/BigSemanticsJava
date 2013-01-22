package ecologylab.semantics.tools;

import java.io.File;
import java.io.FileFilter;
import ecologylab.semantics.actions.SemanticActionTranslationScope;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataScalarField;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.formatenums.Format;

public class AddHintsToRepository
{

	public AddHintsToRepository(File srcDir, File destDir) throws SIMPLTranslationException
	{
		File srcPowerUserDir = new File(srcDir, "powerUser");
		File srcRepositorySources = new File(srcDir, "repositorySources");

		File destPowerUserDir = new File(destDir, "powerUser");
		File destRepositorySources = new File(destDir, "repositorySources");

		// need to instantiate scope so that meta-metadata translation works properly.
		SemanticActionTranslationScope.get();

		processDir(srcDir, destDir);
		processDir(srcPowerUserDir, destPowerUserDir);
		processDir(srcRepositorySources, destRepositorySources);
	}

	private void processDir(File srcDir, File destDir) throws SIMPLTranslationException
	{
		assert srcDir.exists() : "directory not exists: " + srcDir;

		File[] files = srcDir.listFiles(new FileFilter()
		{
			public boolean accept(File dir)
			{
				return dir.getName().endsWith(".xml");
			}
		});
		SimplTypesScope mmdTScope = MetaMetadataTranslationScope.get();
		for (File file : files)
		{
			MetaMetadataRepository repo = (MetaMetadataRepository) mmdTScope.deserialize(file, Format.XML); 
			if (repo.values() != null)
			{
				for (MetaMetadata mmd : repo.values())
				{
					addHints(mmd);
				}
			}
			File out = new File(destDir, file.getName());
			try
			{
				
				SimplTypesScope.serialize(repo, out, Format.XML);
			}
			catch (SIMPLTranslationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void addHints(MetaMetadataField mmf)
	{
		if (mmf.getChildMetaMetadata() != null)
		{
			for (MetaMetadataField child : mmf.getChildMetaMetadata())
			{
				if (child instanceof MetaMetadataScalarField)
				{
					MetaMetadataScalarField scalar = (MetaMetadataScalarField) child;
					if (scalar.getScalarType() != null)
						scalar.setHint(Hint.XML_LEAF);
				}
				else
				{
					addHints(child);
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws SIMPLTranslationException 
	 */
	public static void main(String[] args) throws SIMPLTranslationException
	{
		File src = new File("../cf/config/semantics/metametadata");
		File dest = new File("../cf/config/semantics/metametadata/hintsAdded");

		AddHintsToRepository ahtr = new AddHintsToRepository(src, dest);
	}

}
