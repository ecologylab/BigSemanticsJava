package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecologylab.collections.MultiAncestorScope;
import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.CookieProcessing;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.semantics.namesandnums.DocumentParserTagNames;
import ecologylab.serialization.Format;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;

/**
 * Take charge in loading the repository.
 * 
 * @author quyin
 * 
 */
public class MetaMetadataRepositoryLoader extends Debug implements DocumentParserTagNames
{

	/**
	 * registry of formats to file name extensions.
	 */
	private static final Map<Format, String>	fileNameExts	= new HashMap<Format, String>();

	static
	{
		fileNameExts.put(Format.XML, ".xml");
		fileNameExts.put(Format.JSON, ".json");
	}

	/**
	 * Load the repository from a set of files, in a specified format.
	 * <p />
	 * Meta-metadata types will be placed into corresponding packages, each with a package-wide type
	 * scope. The local type scope for each meta-metadata is initialized.
	 * <p />
	 * MIME type and suffix based selectors are processed. Location based selectors will not be
	 * processed here because that requires the metadata translation scope, which is not yet
	 * determined at this stage.
	 * 
	 * @param files
	 *          The list of files storing the repository.
	 * @param format
	 *          The format of the repository.
	 * @return An integrated representation of the repository.
	 */
	public MetaMetadataRepository loadFromFiles(List<File> files, Format format)
	{
		MetaMetadataRepository result = new MetaMetadataRepository();
		result.repositoryByName = new HashMapArrayList<String, MetaMetadata>();
		result.packageMmdScopes = new HashMap<String, MultiAncestorScope<MetaMetadata>>();

		SimplTypesScope mmdTScope = MetaMetadataTranslationScope.get();

		for (File file : files)
		{
			if (file == null || !file.exists())
			{
				result.warning("ignoring " + file);
				continue;
			}

			println("MetaMetadataRepository read:\t" + file.getPath());

			try
			{
				MetaMetadataRepository repoData = (MetaMetadataRepository) mmdTScope.deserialize(file,
						format);
				if (repoData != null)
				{
					repoData.file = file;

					// sort meta-metadata into result.repositoryByName and mmd scope for that package.
					if (repoData.repositoryByName != null)
					{
						for (String mmdName : repoData.repositoryByName.keySet())
						{
							MetaMetadata mmd = repoData.repositoryByName.get(mmdName);
							mmd.setFile(file);
							mmd.setParent(result);
							mmd.setRepository(result);

							String packageName = mmd.packageName();
							if (packageName == null)
							{
								packageName = repoData.packageName();
								if (packageName == null)
									throw new MetaMetadataException("no package name specified for " + mmd);
								mmd.setPackageName(packageName);
							}
							MultiAncestorScope<MetaMetadata> packageMmdScope = result.packageMmdScopes
									.get(packageName);
							if (packageMmdScope == null)
							{
								packageMmdScope = new MultiAncestorScope<MetaMetadata>(result.repositoryByName);
								result.packageMmdScopes.put(packageName, packageMmdScope);
							}

							switch (mmd.visibility)
							{
							case GLOBAL:
							{
								MetaMetadata existingMmd = result.repositoryByName.get(mmdName);
								if (existingMmd != null && existingMmd != mmd)
									throw new MetaMetadataException("meta-metadata already exists: " + mmdName
											+ " in " + file);
								result.repositoryByName.put(mmdName, mmd);
								break;
							}
							case PACKAGE:
							{
								MetaMetadata existingMmd = packageMmdScope.get(mmdName);
								if (existingMmd != null && existingMmd != mmd)
									throw new MetaMetadataException("meta-metadata already exists: " + mmdName
											+ " in " + file);
								packageMmdScope.put(mmdName, mmd);
								break;
							}
							}
						}

						for (MetaMetadata mmd : repoData.repositoryByName.values())
						{
							MultiAncestorScope<MetaMetadata> packageMmdScope = result.packageMmdScopes.get(mmd
									.packageName());
							mmd.setMmdScope(packageMmdScope);
						}
					}

					// combine other parts
					result.integrateRepositoryWithThis(repoData);
				}
			}
			catch (SIMPLTranslationException e)
			{
				Debug.error(this.getClassSimpleName(),
						"translating repository source file " + file.getAbsolutePath());
				e.printStackTrace();
			}
		}

		// initialize meta-metadata look-up maps
		// result.initializeLocationBasedMaps(); // cannot do this since it needs the metadata TScope.
		result.initializeSuffixAndMimeBasedMaps();

		// We might want to do this only if we have some policies worth enforcing.
		ParsedURL.cookieManager.setCookiePolicy(CookieProcessing.semanticsCookiePolicy);

		// FIXME -- get rid of this?!
		Metadata.setRepository(result);

		MetaMetadataRepository.baseDocumentMM = result.getMMByName(DOCUMENT_TAG);
		MetaMetadataRepository.baseImageMM = result.getMMByName(IMAGE_TAG);

		return result;
	}

	/**
	 * Load meta-metadata from repository files from a directory.
	 * <p />
	 * Order: base level, then repositorySources, then powerUser.
	 * 
	 * @see loadFromFiles()
	 * 
	 * @param dir
	 *          The repository directory.
	 * @param format
	 *          The format of the repository.
	 * @return An integrated representation of the repository.
	 */
	public MetaMetadataRepository loadFromDir(File dir, Format format)
	{
		if (!dir.exists())
		{
			throw new MetaMetadataException("MetaMetadataRepository directory does not exist : "
					+ dir.getAbsolutePath());
		}
		final String fileNameSuffix = fileNameExts.get(format);
		if (fileNameSuffix == null)
		{
			throw new MetaMetadataException("Unregistered or unknown format: " + format);
		}

		println("MetaMetadataRepository directory : " + dir + "\n");

		FileFilter fileFilter = new FileFilter()
		{
			public boolean accept(File dir)
			{
				return dir.getName().endsWith(fileNameSuffix);
			}
		};

		File repositorySources = new File(dir, "repositorySources");
		File powerUserDir = new File(dir, "powerUser");

		List<File> allFiles = new ArrayList<File>();
		addFilesInDirToList(dir, fileFilter, allFiles);
		addFilesInDirToList(repositorySources, fileFilter, allFiles);
		addFilesInDirToList(powerUserDir, fileFilter, allFiles);

		return loadFromFiles(allFiles, format);
	}

	private static void addFilesInDirToList(File dir, FileFilter filter, List<File> buf)
	{
		if (dir == null || !dir.exists())
			return;
		for (File f : dir.listFiles(filter))
			buf.add(f);
	}
	
}
