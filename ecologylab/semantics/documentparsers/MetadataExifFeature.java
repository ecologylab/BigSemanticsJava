/**
 * 
 */
package ecologylab.semantics.documentparsers;

import java.util.Date;

import com.drew.metadata.MetadataException;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metadata.scalar.MetadataDate;
import ecologylab.semantics.metadata.scalar.MetadataScalarBase;
import ecologylab.semantics.metadata.scalar.MetadataString;

/**
 * Maps an exif tag with a S.IM.PL Metadata field in a S.IM.PL Metadata object.
 * 
 * @author andruid
 */
public class MetadataExifFeature
{
	String metadataTag;
	
	int	exifTag;
	
	public MetadataExifFeature(String metadataName, int	exifTag)
	{
		this.metadataTag	= metadataName;
		this.exifTag			= exifTag;
	}

	/**
	 * @return the metadataName
	 */
	public String getMetadataName()
	{
		return metadataTag;
	}

	/**
	 * @return the exifTag
	 */
	public int getExifTag()
	{
		return exifTag;
	}
	
	//TODO -- define a general extract object that used the scalar_type in the MetadataFieldDescriptor
	// to figure out what kind of value to get, construct, and set.

	public String extractString(Metadata metadata, com.drew.metadata.Directory dir)
	{
		String value	= getStringValue(dir);
		if (value != null)
		{
			MetadataString metaString		= new MetadataString(value);
			setMetadataField(metadata, metaString);
		}
		return value;
	}
	public String getStringValue(com.drew.metadata.Directory dir)
	{
		String result	= null;
		if (dir.containsTag(exifTag))
			result			= dir.getString(exifTag);
		return result;
	}
	public Date extractDate(Metadata metadata, com.drew.metadata.Directory dir)
	{
		Date value	= getDateValue(dir);
		if (value != null)
		{
			MetadataDate metaDate				= new MetadataDate(value);
			setMetadataField(metadata, metaDate);
		}
		return value;
	}

	/**
	 * @param metadata
	 * @param metaDate
	 */
	public void setMetadataField(Metadata metadata, MetadataScalarBase metaDate)
	{
		MetadataFieldDescriptor mfd	= metadata.getFieldDescriptorByTagName(metadataTag);
		if (mfd != null)
			mfd.setField(metadata, metaDate);
	}
	public Date getDateValue(com.drew.metadata.Directory dir)
	{
		Date result	= null;
		if (dir.containsTag(exifTag))
			try
			{
				result			= dir.getDate(exifTag);
			}
			catch (MetadataException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return result;
	}
}
