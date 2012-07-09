package ecologylab.semantics.filestorage;

import java.io.InputStream;
import java.util.Date;

import ecologylab.net.ParsedURL;

public interface FileStorageProvider
{
	public String saveFile(ParsedURL originalPURL, InputStream in);

	public String lookupFilePath(ParsedURL originalPURL);

	public void saveFileMetadata(ParsedURL location, ParsedURL additionalLocation,
			String localLocation, String mimeType, Date date);
	
	public FileMetadata getFileMetadata(ParsedURL location);
}
