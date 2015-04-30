package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.metametadata.fieldops.FieldOp;
import ecologylab.bigsemantics.metametadata.fieldops.FieldOpScope;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_other_tags;
import ecologylab.serialization.annotations.simpl_scope;

/**
 * Semantics that transforms locations, with operations such as set_param, strip_param inside it,
 * for managing variability in Document location ParsedURL arguments.
 * 
 * @author andruid
 * @author quyin
 */
@simpl_other_tags("location_op")
public class FilterLocation
{

  private static Logger logger = LoggerFactory.getLogger(FilterLocation.class);

  @simpl_scope(FieldOpScope.NAME)
  @simpl_nowrap
  @simpl_collection
  ArrayList<FieldOp>    ops;

  @simpl_nowrap
  @simpl_collection("alternative_host")
  ArrayList<String>     alternativeHosts;

  public ArrayList<FieldOp> getOps()
  {
    return ops;
  }

  public void setOps(ArrayList<FieldOp> ops)
  {
    this.ops = ops;
  }

  public void addOp(FieldOp op)
  {
    if (ops == null)
    {
      ops = new ArrayList<FieldOp>();
    }
    ops.add(op);
  }

  public ArrayList<String> getAlternativeHosts()
  {
    return alternativeHosts;
  }

  public void setAlternativeHosts(ArrayList<String> alternativeHosts)
  {
    this.alternativeHosts = alternativeHosts;
  }

  public void addAlternativeHost(String alternativeHost)
  {
    if (alternativeHosts == null)
    {
      alternativeHosts = new ArrayList<String>();
    }
    alternativeHosts.add(alternativeHost);
  }

  /**
   * Filter the original location. If the original location is changed, return the new location, and
   * put the original one in alternativeLocations.
   * 
   * @param location
   *          The original location.
   * @param alternativeLocations
   *          Locations with alternative domains.
   * @return The filtered location.
   * @throws Exception
   */
  public ParsedURL filter(ParsedURL location, List<ParsedURL> alternativeLocations)
      throws Exception
  {
    ParsedURL result = location;

    if (location.isFile())
    {
      logger.warn("Not doing <filter_location> because this is a file: " + location);
    }
    else
    {
      Object opsResult = location;
      if (ops != null && ops.size() > 0)
      {
        for (FieldOp op : ops)
        {
          opsResult = op.operateOn(opsResult);
        }
      }
      result = (opsResult instanceof ParsedURL)
          ? ((ParsedURL) opsResult)
          : ParsedURL.getAbsolute(opsResult.toString());

      if (alternativeLocations == null)
      {
        // although we don't return the new list, this prevents from NPEs.
        alternativeLocations = new ArrayList<ParsedURL>();
      }

      if (alternativeHosts != null)
      {
        final String origHost = result.host();
        for (String alternativeHost : alternativeHosts)
        {
          if (!origHost.equals(alternativeHost))
          {
            ParsedURL newLocation = result.changeHost(alternativeHost);
            if (!alternativeLocations.contains(newLocation))
            {
              alternativeLocations.add(newLocation);
            }
          }
        }
      }

      if (result != location)
      {
        if (!alternativeLocations.contains(location))
        {
          alternativeLocations.add(location);
        }
      }
      return result;
    }

    return location;
  }

  /**
   * Filter the input Purl if needed, i.e. specified in the meta-metadata repository. It will first
   * match the input Purl with a meta-metadata, and then use filtering rules for that meta-metadata
   * to do filtering.
   * 
   * @param location
   * @param alternativeLocations
   * @param semanticsScope
   * @return
   * @throws Exception
   */
  public static ParsedURL filterIfNeeded(ParsedURL location,
                                         List<ParsedURL> alternativeLocations,
                                         SemanticsGlobalScope semanticsScope) throws Exception
  {
    MetaMetadata mm = semanticsScope.getMetaMetadataRepository().getDocumentMM(location);
    FilterLocation filter = mm == null ? null : mm.getRewriteLocation();
    if (filter != null)
    {
      ParsedURL filteredPurl = filter.filter(location, alternativeLocations);
      return filteredPurl;
    }
    return location;
  }

}
