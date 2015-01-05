package ecologylab.bigsemantics.metametadata;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import ecologylab.serialization.formatenums.Format;

/**
 * 
 * @author quyin
 * 
 */
public class MetaMetadataRepositoryFileFormats
{

  /**
   * registry of formats to file name extensions.
   */
  static final Map<Format, String> fileNameExts = new HashMap<Format, String>();

  static
  {
    fileNameExts.put(Format.XML, ".xml");
    fileNameExts.put(Format.JSON, ".json");
  }

  /**
   * 
   * @param format
   * @return
   */
  public static String getFileExt(Format format)
  {
    return fileNameExts.get(format);
  }

  /**
   * 
   * @param format
   * @return
   */
  public static FileFilter getFileFilter(Format format)
  {
    String fileExt = getFileExt(format);
    final String fileExt0 = fileExt.startsWith(".") ? fileExt : "." + fileExt;
    return new FileFilter()
    {
      @Override
      public boolean accept(File pathname)
      {
        return pathname.getName().endsWith(fileExt0);
      }
    };
  }

}
