package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.sensor.location.EarthData;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * An object for representing a set of 3d coordinates on the earth's surface: latitude, longitude,
 * and altitude.
 * 
 * @author Zachary O. Toups (zach@ecologylab.net)
 */
@simpl_inherit
public class GeoLocation extends Metadata implements EarthData
{
	/** The latitude, expressed in degrees in double-precision degrees. */
	@simpl_scalar
	double										latitude;

	/** The longitude, expressed in degrees in double-precision degrees. */
	@simpl_scalar
	double										longitude;

	/** The altitude, expressed in meters. */
	@simpl_scalar
	double										altitude;


	/**
	 * 
	 */
	public GeoLocation()
	{
	}

	public GeoLocation(double lat, double lon, double alt)
	{
		this.latitude = lat;
		this.longitude = lon;
		this.altitude = alt;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double lat)
	{
		this.latitude = lat;

		kMLCommaDelimited = null;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double lon)
	{
		this.longitude = lon;

		kMLCommaDelimited = null;
	}

	public double getAltitude()
	{
		return altitude;
	}

	public void setAltitude(double alt)
	{
		this.altitude = alt;

		kMLCommaDelimited = null;
	}

	/**
	 * A String reprsenting the current data for KML; cached for re-use and computed only when needed.
	 */
	String	kMLCommaDelimited	= null;

	/**
	 * Get the set of coordinates, serialized for use in KML / Google Earth.
	 * 
	 * @return
	 */
	public String getKMLCommaDelimitedString()
	{
		if (kMLCommaDelimited == null)
		{
			kMLCommaDelimited = this.longitude + "," + this.latitude + "," + this.altitude;
		}

		return kMLCommaDelimited;
	}

	/**
	 * @param that
	 * @return positive if this is farther north than that, negative if that is more north; 0 if they
	 *         lie on exactly the same parallel.
	 */
	public double compareNS(GeoLocation that)
	{
		return this.getLatitude() - that.getLatitude();
	}

	/**
	 * @param that
	 * @return compares two GPSDatum's based on the acute angle between their longitudes. Returns 1 if
	 *         this is farther east than that, -1 if this is farther west, 0 if the two points lie on
	 *         the same arc, 180/-180 if they are opposite.
	 */
	public double compareEW(GeoLocation that)
	{
		double diff = getLongitude() - that.getLongitude();

		if (diff > 180)
		{
			return diff - 360;
		}
		else if (diff < -180)
		{
			return diff + 360;
		}
		else
		{
			return diff;
		}
	}

	/**
	 * Uses the haversine formula to compute the great-circle direct distance from this to the other
	 * point. Does not take into account altitude.
	 * 
	 * Result is given in meters.
	 * 
	 * Formula used from http://www.movable-type.co.uk/scripts/latlong.html.
	 * 
	 * @param other
	 * @return great-circle distance between this and other, in meters.
	 */
	public double distanceTo(GeoLocation other)
	{
		return this.distanceTo(other.getLatitude(), other.getLongitude());
	}

	/**
	 * Uses the haversine formula to compute the great-circle direct distance from this to the other
	 * point. Does not take into account altitude.
	 * 
	 * Result is given in meters.
	 * 
	 * Formula used from http://www.movable-type.co.uk/scripts/latlong.html.
	 * 
	 * @param otherLat
	 * @param otherLon
	 * @return great-circle distance between this and other, in meters.
	 */
	public double distanceTo(double otherLat, double otherLon)
	{
		double deltaLat = Math.toRadians(otherLat - this.getLatitude());
		double deltaLon = Math.toRadians(otherLon - this.getLongitude());

		double a = (Math.sin(deltaLat / 2.0) * Math.sin(deltaLat / 2.0))
				+ (Math.cos(Math.toRadians(this.getLatitude())) * Math.cos(Math.toRadians(otherLat))
						* Math.sin(deltaLon / 2.0) * Math.sin(deltaLon / 2.0));
		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

		return c * RADIUS_EARTH_METERS;
	}
}
