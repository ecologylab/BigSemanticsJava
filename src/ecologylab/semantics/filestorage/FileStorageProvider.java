/**
 * 
 */
package ecologylab.semantics.filestorage;

/**
 * An interface for saving and looking up files and file metadata
 * 
 * @author ajit
 */

import java.io.InputStream;

import ecologylab.net.ParsedURL;

public interface FileStorageProvider
{
	/**
	 * URL is used for generating unique name of the file
	 * 
	 * @param originalPURL	url of input stream
	 * @param in						input stream
	 * @return							path of the saved file
	 */
	public String saveFile(ParsedURL originalPURL, InputStream in);

	/**
	 * Looks up path corresponding to URL
	 * 
	 * @param originalPURL
	 * @return	path of the stored file
	 */
	public String lookupFilePath(ParsedURL originalPURL);

	/**
	 * Save file metadata
	 * 
	 * @param fileMetadata
	 * @see		{@link FileMetadata FileMetadata}
	 */
	public void saveFileMetadata(FileMetadata fileMetadata);
	
	/**
	 * 
	 * @param location
	 * @return
	 */
	public FileMetadata getFileMetadata(ParsedURL location);
}
