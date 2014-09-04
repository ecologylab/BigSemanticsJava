package ecologylab.bigsemantics;

import java.io.File;

/**
 * Utilities with regarding to file operations.
 * 
 * @author quyin
 */
public class FileUtils
{

	/**
	 * Delete a directory recursively.
	 * 
	 * @param dir
	 * @return true if the directory exists and was deleted completely and successfully; otherwise
	 *         false.
	 */
	public static boolean deleteDir(File dir)
	{
		if (dir.exists() && dir.isDirectory())
		{
			for (File file : dir.listFiles())
			{
				if (file.isDirectory())
				{
					deleteDir(file);
				}
				else if (file.isFile())
				{
					file.delete();
				}
			}
			return dir.delete();
		}
		return false;
	}

}
