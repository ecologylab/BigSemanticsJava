/**
 * 
 */
package ecologylab.semantics.filestorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

/**
 * file system based implementation of FileStorageProvider
 * 
 * @author ajit
 * 
 */

public class FileSystemStorage extends Debug implements FileStorageProvider
{
	private static FileSystemStorage	fsStorageProvider = null;

	private static String				downloadDirectory;

	private static String				metaFileDirectory;

	private static SimplTypesScope		META_TSCOPE				= SimplTypesScope.get("fileMetadata",
																													FileMetadata.class);
	public static void setDownloadDirectory(String downloadDir)
	{
		downloadDirectory = downloadDir;
		metaFileDirectory = downloadDirectory + "/meta";
		
		File f = new File(downloadDirectory);
		f.mkdirs();
		f = new File(metaFileDirectory);
		f.mkdir();
	}

	private String										outFileName;

	private FileSystemStorage()
	{
	}

	@Override
	public String saveFile(ParsedURL originalPURL, InputStream input)
	{
		outFileName = SHA256FileNameGenerator.getName(originalPURL);
		File outFile = new File(downloadDirectory, outFileName);

		try
		{
			InputStream in = input;
			OutputStream out = new FileOutputStream(outFile);
			byte buf[] = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
				out.write(buf, 0, len);
			out.close();
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		debug("Saved inputstream to " + outFile.getAbsolutePath());
		return outFile.getAbsolutePath();
	}

	@Override
	public String lookupFilePath(ParsedURL originalPURL)
	{
		String fileName = SHA256FileNameGenerator.getName(originalPURL);
		File f = new File(downloadDirectory, fileName);
		debug("Checking for cached HTML file at [" + f.getAbsolutePath() + "]: exists? " + f.exists());
		if (f.exists())
			return f.getAbsolutePath();
		else
			return null;
	}

	@Override
	public void saveFileMetadata(FileMetadata fileMetadata)
	{
		if (outFileName == null)
			outFileName = SHA256FileNameGenerator.getName(fileMetadata.getLocation());
		File metaFile = new File(metaFileDirectory, (outFileName + ".meta"));
		try
		{
			SimplTypesScope.serialize(fileMetadata, metaFile, Format.XML);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public FileMetadata getFileMetadata(ParsedURL location)
	{
		if (outFileName == null)
			outFileName = SHA256FileNameGenerator.getName(location);
		File metaFile = new File(metaFileDirectory, (outFileName + ".meta"));
		try
		{
			return (FileMetadata) META_TSCOPE.deserialize(metaFile, Format.XML);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static FileStorageProvider getStorageProvider()
	{
		if (fsStorageProvider == null)
		{
			fsStorageProvider = new FileSystemStorage();
		}
		return fsStorageProvider;
	}

}
