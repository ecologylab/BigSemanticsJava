package ecologylab.semantics.filestorage;

import java.util.Date;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_scalar;

public class FileMetadata
{
	@simpl_scalar
	private ParsedURL				location;
	
	@simpl_scalar
	private ParsedURL				redirectedLocation;

	@simpl_scalar
	private String					localLocation;
	
	@simpl_scalar
	private String					mimeType;
	
	@simpl_scalar
	private String					downloadTime;
	
	public FileMetadata() {}
	
	public FileMetadata(ParsedURL location, ParsedURL redirectedLocation,
			String localLocation, String mimeType, Date date)
	{
		this.location = location;
		this.redirectedLocation = redirectedLocation;
		this.localLocation = localLocation;
		this.mimeType = mimeType;
		this.downloadTime = date.toString();
	}

	public ParsedURL getLocation()
	{
		return location;
	}

	public ParsedURL getRedirectedLocation()
	{
		return redirectedLocation;
	}

	public String getLocalLocation()
	{
		return localLocation;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public String getDownloadTime()
	{
		return downloadTime;
	}
	
}
