/**
 * 
 */
package ecologylab.semantics.connectors;

/**
 * Status of a Document vis-a-vis download and parse.
 * 
 * @author andruid
 */
public enum DownloadStatus
{
	UNPROCESSED, QUEUED, PARSING, DONE, IOERROR, RECYCLED
}
