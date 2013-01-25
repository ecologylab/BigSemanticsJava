package ecologylab.semantics.collecting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ecologylab.appframework.EnvironmentGeneric;
import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.generic.Debug;
import ecologylab.io.Assets;
import ecologylab.io.AssetsRoot;
import ecologylab.io.Files;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metametadata.MetaMetadataRepositoryFileFormats;
import ecologylab.semantics.namesandnums.SemanticsAssetVersions;
import ecologylab.serialization.formatenums.Format;

/**
 * Locate meta-metadata wrapper repository and open input streams to them.
 * 
 * @author quyin
 * 
 */
public class MetaMetadataRepositoryLocator extends Debug
{

  private static final String   SEMANTICS;

  private static final String[] DEFAULT_REPOSITORY_LOCATIONS;

  static
  {
    SEMANTICS = "semantics/";
    DEFAULT_REPOSITORY_LOCATIONS = new String[]
    {
      "../../bigSemanticsWrapperRepository/bigSemanticsWrappers/MmdRepository/mmdrepository",
      "../../MetaMetadataRepository/MmdRepository/mmdrepository",
      "../ecologylabSemantics/repository",
    };
  }

  private boolean               useAssetsCache;

  /**
   * 
   * @param useAssetsCache
   *          (Used by loading from assets only)
   */
  MetaMetadataRepositoryLocator(boolean useAssetsCache)
  {
    this.useAssetsCache = useAssetsCache;
  }

  /**
   * Locate meta-metadata repository files and open input streams to them. It will try the following
   * places in order: 1) the assets, if app framework is used; 2) default locations; 3) java
   * resources.
   * 
   * @param repositoryLocation
   * @param repositoryFormat
   * @return
   */
  public List<InputStream> locateRepositoryAndOpenStreams(File repositoryLocation,
                                                          Format repositoryFormat)
  {
    List<InputStream> result = new ArrayList<InputStream>();

    if (repositoryLocation == null)
    {
      // first, try assets
      if (SingletonApplicationEnvironment.isInUse()
          && !SingletonApplicationEnvironment.runningInEclipse())
      {
        ParsedURL semanticsRelativeUrl = EnvironmentGeneric.configDir().getRelative(SEMANTICS);
        File repositoryDir = Files.newFile(PropertiesAndDirectories.thisApplicationDir(),
                                           SEMANTICS + "/repository");
        AssetsRoot mmAssetsRoot = new AssetsRoot(semanticsRelativeUrl, repositoryDir);
        repositoryLocation = Assets.getAsset(mmAssetsRoot,
                                             null,
                                             "repository",
                                             null,
                                             !useAssetsCache,
                                             SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
      }

      // then, try default locations
      if (repositoryLocation == null)
      {
        repositoryLocation = locateRepositoryByDefaultLocations();
        debug("meta-metadata repository located by default location at " + repositoryLocation);
      }
    }

    int n = 0;
    if (repositoryLocation != null)
    {
      debug("tentative meta-metadata repository location: " + repositoryLocation);
      n = openStreams(result, repositoryLocation, repositoryFormat);
    }
    else
    {
      // at last, try java resources
      debug("trying to locate meta-metadata repository as java resources...");
      n = locateRepositoryAsJavaResourcesAndOpenStreams(result);
    }
    debug(n + "repository file(s) or resource(s) found.");

    return result;
  }

  public static File locateRepositoryByDefaultLocations()
  {
    if (PropertiesAndDirectories.os() == PropertiesAndDirectories.ANDROID)
    {
      return locateRepositoryForAndroid();
    }
    else
    {
      for (String defaultLocation : DEFAULT_REPOSITORY_LOCATIONS)
      {
        File possibleRepositoryDir = new File(defaultLocation);
        if (possibleRepositoryDir.exists() && possibleRepositoryDir.isDirectory())
          return possibleRepositoryDir;
      }
    }
    return null;
  }

  static File locateRepositoryForAndroid()
  {
    Class environmentClass;
    try
    {
      environmentClass = Class.forName("android.os.Environment");
      if (environmentClass != null)
      {
        Method m = environmentClass.getMethod("getExternalStorageDirectory");
        File sdCard = (File) m.invoke(null, null);
        File ecologylabDir = new File(sdCard.getAbsolutePath()
            + "/Android/data/com.ecologyAndroid.ecoDroidTest/files/");
        File mmdrepositoryDir = new File(ecologylabDir + "/mmdrepository/");
        return mmdrepositoryDir;
      }
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (SecurityException e)
    {
      e.printStackTrace();
    }
    catch (NoSuchMethodException e)
    {
      e.printStackTrace();
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
    catch (InvocationTargetException e)
    {
      e.printStackTrace();
    }

    return null;
  }

  int locateRepositoryAsJavaResourcesAndOpenStreams(List<InputStream> result)
  {
    InputStream repositoryListStream =
        this.getClass().getResourceAsStream("/mmdrepository/repositoryFiles.lst");
    BufferedReader br = new BufferedReader(new InputStreamReader(repositoryListStream));

    int n = 0;
    while (true)
    {
      String line = null;
      try
      {
        line = br.readLine();
      }
      catch (IOException e)
      {
        error("Cannot read repositoryFiles.lst");
        e.printStackTrace();
      }
      if (line == null)
        break;
      line = line.trim();
      if (line.length() > 0)
      {
        String repositoryFileResourceName = "/mmdrepository/" + line;
        result.add(this.getClass().getResourceAsStream(repositoryFileResourceName));
        n++;
      }
    }
    return n;
  }

  int openStreams(List<InputStream> result, File repositoryDir, Format repositoryFormat)
  {
    FileFilter fileFilter = MetaMetadataRepositoryFileFormats.getFileFilter(repositoryFormat);

    int n = 0;
    n += openStreamsHelper(result, repositoryDir, fileFilter);
    n += openStreamsHelper(result, new File(repositoryDir, "repositorySources"), fileFilter);
    n += openStreamsHelper(result, new File(repositoryDir, "powerUser"), fileFilter);
    return n;
  }

  int openStreamsHelper(List<InputStream> result, File dir, FileFilter fileFilter)
  {
    int n = 0;
    if (dir != null && dir.exists())
    {
      for (File f : dir.listFiles(fileFilter))
      {
        try
        {
          result.add(new FileInputStream(f));
          n++;
        }
        catch (FileNotFoundException e)
        {
          error("Cannot open input stream for " + f);
        }
      }
    }
    return n;
  }

}
