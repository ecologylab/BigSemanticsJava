/**
 * 
 */
package ecologylab.semantics.metametadata;

/**
 * Exception to throw when lookup of a Metadata subclass by tag name fails.
 * This means that the proper Metadata object cannot be instantiated.
 * 
 * @author andruid
 */
public class NoSuchMetadataClassException extends Exception
{

	/**
	 * 
	 */
	public NoSuchMetadataClassException()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public NoSuchMetadataClassException(String message)
	{
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public NoSuchMetadataClassException(Throwable cause)
	{
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoSuchMetadataClassException(String message, Throwable cause)
	{
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
