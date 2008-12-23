/**
 * 
 */
package ecologylab.documenttypes;

import java.io.File;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.services.messages.cf.Seed;

/**
 * @author andruid
 *
 */
public interface InfoProcessor<AC extends AbstractContainer>
{

	void displayStatus(String message);
	
	AC lookupAbstractContainer(ParsedURL connectionPURL);

	Object globalCollectionContainersLock();
	
	void mapContainerToPURL(ParsedURL purl, AC container);

	boolean accept(ParsedURL connectionPURL);

	MetaMetadataRepository metaMetaDataRepository();

	DocumentType newFileDirectoryType(File file);
	
	Class<? extends InfoProcessor>[] getInfoProcessorClassArg();
}
