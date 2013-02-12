package ecologylab.bigsemantics.metametadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ecologylab.bigsemantics.collecting.CookieProcessing;
import ecologylab.bigsemantics.collecting.MetaMetadataRepositoryLocator;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.bigsemantics.namesandnums.DocumentParserTagNames;
import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

/**
 * Take charge in loading the repository.
 * 
 * @author quyin
 * 
 */
public class MetaMetadataRepositoryLoader extends Debug implements DocumentParserTagNames
{

  static final SimplTypesScope     mmdTScope    = MetaMetadataTranslationScope.get();

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
   * @throws FileNotFoundException
   * @throws SIMPLTranslationException
   */
  public MetaMetadataRepository loadFromDir(File dir, Format format) throws FileNotFoundException,
      SIMPLTranslationException
  {
    if (!dir.exists())
    {
      throw new MetaMetadataException("MetaMetadataRepository directory does not exist : "
          + dir.getAbsolutePath());
    }
    
    println("MetaMetadataRepository directory : " + dir + "\n");
    
    List<File> allFiles = MetaMetadataRepositoryLocator.listRepositoryFiles(dir, format);
    return loadFromFiles(allFiles, format);
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
   * @throws FileNotFoundException
   * @throws SIMPLTranslationException
   */
  public MetaMetadataRepository loadFromFiles(List<File> files, Format format)
      throws FileNotFoundException, SIMPLTranslationException
  {
    List<InputStream> istreams = new ArrayList<InputStream>();
    for (File file : files)
    {
      if (file == null || !file.exists())
      {
        warning("Ignoring " + file);
        continue;
      }
      println("Opening MetaMetadataRepository:\t" + file.getPath());
      InputStream istream = new FileInputStream(file);
      istreams.add(istream);
    }

    return loadFromInputStreams(istreams, format);
  }

  /**
   * Load the repository from a list of InputStreams. This is useful for loading the repository from
   * jar'ed resources.
   * 
   * @param istreams
   * @param format
   * @return
   * @throws SIMPLTranslationException
   */
  public MetaMetadataRepository loadFromInputStreams(List<InputStream> istreams, Format format)
  {
    List<MetaMetadataRepository> repositories = deserializeRepositories(istreams, format);
    MetaMetadataRepository result = mergeRepositories(repositories);
    initializeRepository(result);
    return result;
  }

  List<MetaMetadataRepository> deserializeRepositories(List<InputStream> streams,
                                                       Format format)
  {
    List<MetaMetadataRepository> result = new ArrayList<MetaMetadataRepository>(streams.size());
    for (InputStream istream : streams)
    {
      MetaMetadataRepository repo = null;
      try
      {
        repo = (MetaMetadataRepository) mmdTScope.deserialize(istream, format);
      }
      catch (SIMPLTranslationException e)
      {
        error("Cannot deserialize repository from InputStream: " + istream);
        e.printStackTrace();
      }
      if (repo != null)
      {
        result.add(repo);
        println("Deserialized " + repo);
      }
    }
    return result;
  }

  MetaMetadataRepository mergeRepositories(List<MetaMetadataRepository> repositories)
  {
    MetaMetadataRepository result = new MetaMetadataRepository();
    result.repositoryByName = new HashMapArrayList<String, MetaMetadata>();
    result.packageMmdScopes = new HashMap<String, MmdScope>();

    for (MetaMetadataRepository repo : repositories)
    {
      mergeOneRepositoryIntoAnother(result, repo);
    }
    return result;
  }

  void mergeOneRepositoryIntoAnother(MetaMetadataRepository toRepository,
                                     MetaMetadataRepository fromRepository)
  {
    if (fromRepository != null)
    {
      // sort meta-metadata into toRepository.repositoryByName and mmdScope for that package.
      if (fromRepository.repositoryByName != null)
      {
        for (String mmdName : fromRepository.repositoryByName.keySet())
        {
          MetaMetadata mmd = fromRepository.repositoryByName.get(mmdName);
          mmd.setParent(toRepository);
          mmd.setRepository(toRepository);

          String packageName = mmd.packageName();
          if (packageName == null)
          {
            packageName = fromRepository.packageName();
            if (packageName == null)
              throw new MetaMetadataException("no package name specified for " + mmd);
            mmd.setPackageName(packageName);
          }

          MmdScope packageMmdScope = toRepository.packageMmdScopes.get(packageName);
          if (packageMmdScope == null)
          {
            packageMmdScope = new MmdScope(toRepository.repositoryByName);
            packageMmdScope.name = packageName;
            toRepository.packageMmdScopes.put(packageName, packageMmdScope);
          }

          switch (mmd.visibility)
          {
          case GLOBAL:
          {
            MetaMetadata existingMmd = toRepository.repositoryByName.get(mmdName);
            if (existingMmd != null && existingMmd != mmd)
              throw new MetaMetadataException("meta-metadata already exists: " + mmdName + " in "
                  + fromRepository);
            toRepository.repositoryByName.put(mmdName, mmd);
            break;
          }
          case PACKAGE:
          {
            MetaMetadata existingMmd = packageMmdScope.get(mmdName);
            if (existingMmd != null && existingMmd != mmd)
              throw new MetaMetadataException("meta-metadata already exists: " + mmdName + " in "
                  + fromRepository);
            packageMmdScope.put(mmdName, mmd);
            break;
          }
          }
        }

        for (MetaMetadata mmd : fromRepository.repositoryByName.values())
        {
          MmdScope packageMmdScope = toRepository.packageMmdScopes.get(mmd.packageName());
          mmd.setMmdScope(packageMmdScope);
        }
      }

      // combine other parts
      toRepository.integrateRepositoryWithThis(fromRepository);
    }
  }

  void initializeRepository(MetaMetadataRepository result)
  {
    // initialize meta-metadata look-up maps
    // result.initializeLocationBasedMaps(); // cannot do this since it needs the metadata TScope.
    result.initializeSuffixAndMimeBasedMaps();

    // We might want to do this only if we have some policies worth enforcing.
    ParsedURL.cookieManager.setCookiePolicy(CookieProcessing.semanticsCookiePolicy);

    // FIXME -- get rid of this?!
    Metadata.setRepository(result);

    MetaMetadataRepository.baseDocumentMM = result.getMMByName(DOCUMENT_TAG);
    MetaMetadataRepository.baseImageMM = result.getMMByName(IMAGE_TAG);
  }

}
