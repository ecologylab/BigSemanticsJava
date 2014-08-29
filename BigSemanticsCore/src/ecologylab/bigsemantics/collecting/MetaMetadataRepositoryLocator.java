package ecologylab.bigsemantics.collecting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.appframework.EnvironmentGeneric;
import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepositoryFileFormats;
import ecologylab.bigsemantics.namesandnums.SemanticsAssetVersions;
import ecologylab.generic.Debug;
import ecologylab.io.Assets;
import ecologylab.io.AssetsRoot;
import ecologylab.io.Files;
import ecologylab.io.NamedInputStream;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.formatenums.Format;

/**
 * Locate meta-metadata wrapper repository and open input streams to them.
 * 
 * @author quyin
 * 
 */
public class MetaMetadataRepositoryLocator extends Debug
{

  static final Logger           logger;

  private static final String   SEMANTICS;

  private static final String[] DEFAULT_REPOSITORY_LOCATIONS;

  static
  {
    logger = LoggerFactory.getLogger(MetaMetadataRepositoryLocator.class);
    SEMANTICS = "semantics/";
    DEFAULT_REPOSITORY_LOCATIONS = new String[]
    {
      "../../BigSemanticsWrapperRepository/BigSemanticsWrappers/repository",
      "../../MetaMetadataRepository/repository",
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
   *          the location of the repository. this can be null, in which case the locator will try
   *          to locate the repository automatically.
   * @param repositoryFormat
   * @return
   */
  public List<NamedInputStream> locateRepositoryAndOpenStreams(File repositoryLocation,
                                                          Format repositoryFormat)
  {
    List<NamedInputStream> result = new ArrayList<NamedInputStream>();

    if (repositoryLocation == null)
    {
      // try default locations
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
    debug(n + " repository file(s) or resource(s) found.");

    return result;
  }

  public static File locateRepositoryByDefaultLocations()
  {
    for (String defaultLocation : DEFAULT_REPOSITORY_LOCATIONS)
    {
      File possibleRepositoryDir = new File(defaultLocation);
      if (possibleRepositoryDir.exists() && possibleRepositoryDir.isDirectory())
        return possibleRepositoryDir;
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

  int locateRepositoryAsJavaResourcesAndOpenStreams(List<NamedInputStream> result)
  {
    InputStream repositoryListStream =
        this.getClass().getResourceAsStream("/repositoryFiles.lst");
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
        String repositoryFileResourceName = "/" + line;
        result.add(new NamedInputStream(repositoryFileResourceName,
        																this.getClass().getResourceAsStream(repositoryFileResourceName)));
        n++;
      }
    }

    return n;
  }

  public static List<File> listRepositoryFiles(File repositoryDir, Format repositoryFormat)
  {
    
    FileFilter fileFilter = MetaMetadataRepositoryFileFormats.getFileFilter(repositoryFormat);
    assert fileFilter != null;

    List<File> allFiles = new ArrayList<File>();
    
    List<File> allDirectories = getSubDirectories(repositoryDir);
    allDirectories.add(repositoryDir);
    for(int i = 0; i < allDirectories.size(); i++){
    	  addFilesInDirToList(allDirectories.get(i), fileFilter, allFiles);
    }

    return allFiles;
  }

	private static List<File> getSubDirectories(File rootDirectory){
		  List<File> subdirectories = new ArrayList<File>();
		  String[] directories = rootDirectory.list(new FilenameFilter() {
		      @Override
		      public boolean accept(File current, String name) {
		        return new File(current, name).isDirectory();
		      }
		    });
		  for(int i = 0; i < directories.length; i++){
			  File directory  = new File(rootDirectory, directories[i]);
			  subdirectories.add(directory);
			  subdirectories.addAll(getSubDirectories(directory));
		  }
		  return subdirectories;
	  }
  
  private static void addFilesInDirToList(File dir, FileFilter filter, List<File> buf)
  {
    if (dir == null || !dir.exists())
      return;
    for (File f : dir.listFiles(filter))
      buf.add(f);
  }

  public int openStreams(List<NamedInputStream> result,
                         File repositoryDir,
                         Format repositoryFormat)
  {
    List<File> repoFiles = listRepositoryFiles(repositoryDir, repositoryFormat);
    for (File file : repoFiles)
    {
      try
      {
        result.add(new NamedInputStream(file));
      }
      catch (FileNotFoundException e)
      {
        Debug.error(MetaMetadataRepositoryLocator.class, "Cannot open input stream for " + file);
      }
    }
    return repoFiles.size();
  }

}
