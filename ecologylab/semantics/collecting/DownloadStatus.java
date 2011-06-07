/**
 * 
 */
package ecologylab.semantics.collecting;

/**
 * Status of a Document vis-a-vis download and parse.
 * 
 * @author andruid
 */
public enum DownloadStatus
{
	UNPROCESSED, QUEUED, PARSING, DOWNLOAD_DONE, IOERROR, RECYCLED
}
