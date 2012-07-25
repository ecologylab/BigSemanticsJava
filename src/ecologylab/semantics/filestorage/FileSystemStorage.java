/**
 * 
 */
package ecologylab.semantics.filestorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

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

public class FileSystemStorage implements FileStorageProvider
{
	private static FileSystemStorage	fsStorageProvider	= null;

	private static String							userhome					= System.getProperty("user.home");

	private static String							downloadDirectory	= userhome + "/Downloads/LocalDocumentCache";

	private static String							metaFileDirectory	= downloadDirectory + "/meta";

	private static SimplTypesScope		META_TSCOPE				= SimplTypesScope.get("fileMetadata",
																													FileMetadata.class);

	private String										outFileName;

	static
	{
		File f = new File(downloadDirectory);
		f.mkdirs();
		f = new File(metaFileDirectory);
		f.mkdir();
	}

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

		System.out.println("Saved inputstream to " + outFile.getAbsolutePath());
		return outFile.getAbsolutePath();
	}

	@Override
	public String lookupFilePath(ParsedURL originalPURL)
	{
		String fileName = SHA256FileNameGenerator.getName(originalPURL);
		File f = new File(downloadDirectory, fileName);
		if (f.exists())
			return f.getAbsolutePath();
		else
			return null;
	}

	@Override
	public void saveFileMetadata(ParsedURL location, ParsedURL additionalLocation,
			String localLocation, String mimeType, Date date)
	{
		FileMetadata fileMetadata = new FileMetadata(location, additionalLocation, localLocation,
				mimeType, date);
		if (outFileName == null)
			outFileName = SHA256FileNameGenerator.getName(location);
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
