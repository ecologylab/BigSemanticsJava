package ecologylab.bigsemantics.metadata.builtins;

import java.util.ArrayList;
import java.util.List;

import ecologylab.bigsemantics.metadata.builtins.declarations.PersistenceMetaInfoDeclaration;
import ecologylab.bigsemantics.metadata.scalar.MetadataParsedURL;
import ecologylab.net.ParsedURL;

public class PersistenceMetaInfo extends PersistenceMetaInfoDeclaration {

	List<ParsedURL> getRawAdditionalLocations()
	{
	  List<MetadataParsedURL> additionalLocations = getAdditionalLocations();
	  if (additionalLocations != null)
	  {
		  List<ParsedURL> result = new ArrayList<ParsedURL>();
		  for (MetadataParsedURL purl : additionalLocations)
		  {
			  result.add(purl.getValue());
		  }
		  return result;
	  }
	  return null;
	}
	
}
