package ecologylab.bigsemantics.metametadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tools for file / path operations.
 * 
 * @author quyin
 * 
 */
public class FileTools
{

  /**
   * Returns the relative path of file with respect to ancestor. Convenient method so that the
   * caller does not need to specify the separator.
   * 
   * @param ancestor
   * @param path
   * @return
   */
  public static String getRelativePath(String ancestor, String path)
  {
    return getRelativePath(ancestor, path, File.separatorChar);
  }

  /**
   * Returns the relative path of file with respect to ancestor. Requires that the two arguments are
   * either both absolute paths or both relative paths.
   * 
   * @param ancestor
   * @param path
   * @param separator
   * @return
   */
  public static String getRelativePath(String ancestor, String path, char separator)
  {
    List<String> p1 = decompositePath(ancestor, separator);
    List<String> p2 = decompositePath(path, separator);

    // under windows, if the two paths are for different disk partitions, we cannot find a relative
    // path.
    if (separator == '\\'
        && p1.size() > 0 && p1.get(0).endsWith(":")
        && p2.size() > 0 && p2.get(0).endsWith(":")
        && !p1.get(0).equals(p2.get(0)))
      return path;

    StringBuilder sb = new StringBuilder();
    while (!compatible(p1, p2))
    {
      p1.remove(p1.size() - 1);
      sb.append("..").append(separator);
    }
    for (int i = p1.size(); i < p2.size(); ++i)
    {
      sb.append(p2.get(i)).append(i == p2.size() - 1 ? "" : separator);
    }
    return sb.toString();
  }

  static boolean compatible(List<String> ancestor, List<String> path)
  {
    if (ancestor.size() == 0)
      return true;
    for (int i = 0; i < ancestor.size(); ++i)
    {
      if (i >= path.size() || !ancestor.get(i).equals(path.get(i)))
        return false;
    }
    return true;
  }

  static List<String> decompositePath(String path, char separator)
  {
    List<String> components = new ArrayList<String>();

    StringBuilder sb = new StringBuilder();
    char c = 0;
    for (int i = 0; i < path.length(); ++i)
    {
      c = path.charAt(i);
      if (c != separator)
      {
        sb.append(c);
      }
      else if (sb.length() > 0)
      {
        components.add(sb.toString());
        sb.delete(0, sb.length());
      }
    }
    // last component:
    if (sb.length() > 0)
      components.add(sb.toString());

    return components;
  }

}
