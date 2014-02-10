/**
 * 
 */
package ecologylab.bigsemantics.documentcache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Metadata for persistent documents.
 * 
 * @author ajit
 */
public class PersistenceMetadata
{

  /**
   * The persistence ID of this document.
   */
  @simpl_scalar
  private String          docId;

  /**
   * The canonical location (after handling redirections) of the persisted document.
   */
  @simpl_scalar
  private ParsedURL       location;

  /**
   * Additional locations of the persisted document, for indexing.
   */
  @simpl_collection
  private List<ParsedURL> additionalLocations;

  /**
   * Helper object for synchronization.
   */
  final private Object    additionalLocationsLock = new Object();

  /**
   * The MIME type when accessing the document on the web.
   */
  @simpl_scalar
  private String          mimeType;

  /**
   * The time when accessing the original raw document on the web.
   */
  @simpl_scalar
  private Date            accessTime;

  /**
   * The time when persisting the document to the store. This can differ from accessTime for example
   * when the meta-metadata repository gets updated and metadata gets re-extracted.
   */
  @simpl_scalar
  private Date            persistenceTime;

  /**
   * The version number of the meta-metadata repository used to extract this document.
   */
  @simpl_scalar
  private long            repositoryVersion;

  /**
   * The hash of the meta-metadata repository used to extract this document.
   */
  @simpl_scalar
  private String          repositoryHash;

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public ParsedURL getLocation()
  {
    return location;
  }

  public void setLocation(ParsedURL location)
  {
    this.location = location;
  }

  public List<ParsedURL> getAdditionalLocations()
  {
    return additionalLocations;
  }

  public void setAdditionalLocations(List<ParsedURL> additionalLocations)
  {
    this.additionalLocations = additionalLocations;
  }

  private List<ParsedURL> additionalLocations()
  {
    if (additionalLocations == null)
    {
      synchronized (additionalLocationsLock)
      {
        if (additionalLocations == null)
        {
          additionalLocations = new ArrayList<ParsedURL>();
        }
      }
    }
    return additionalLocations;
  }

  public void addAdditionalLocation(ParsedURL additionalLocation)
  {
    this.additionalLocations().add(additionalLocation);
  }

  public boolean isOneLocation(ParsedURL location)
  {
    if (this.location == location)
    {
      return true;
    }
    if (this.additionalLocations != null && this.additionalLocations.contains(location))
    {
      return true;
    }
    return false;
  }

  public String getMimeType()
  {
    return mimeType;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  public Date getAccessTime()
  {
    return accessTime;
  }

  public void setAccessTime(Date accessTime)
  {
    this.accessTime = accessTime;
  }

  public Date getPersistenceTime()
  {
    return persistenceTime;
  }

  public void setPersistenceTime(Date persistenceTime)
  {
    this.persistenceTime = persistenceTime;
  }

  public long getRepositoryVersion()
  {
    return repositoryVersion;
  }

  public void setRepositoryVersion(long repositoryVersion)
  {
    this.repositoryVersion = repositoryVersion;
  }

  public String getRepositoryHash()
  {
    return repositoryHash;
  }

  public void setRepositoryHash(String repositoryHash)
  {
    this.repositoryHash = repositoryHash;
  }

}
