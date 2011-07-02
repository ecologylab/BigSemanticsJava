/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.generic.Debug;
import ecologylab.semantics.metadata.builtins.Clipping;
import ecologylab.semantics.metadata.builtins.CompoundDocument;

/**
 * All state related to the crawler, and associated with a single CompoundDocument.
 * 
 * @author andruid
 */
public class CompoundDocumentParserCrawlerResult extends Debug implements ParserResult
{

	/**
	 * 
	 */
	public CompoundDocumentParserCrawlerResult()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void recycle()
	{
		// TODO Auto-generated method stub

	}

	public void derive(CompoundDocument compoundDocument)
	{
		for (Clipping clipping: compoundDocument.getClippings())
		{
			if (clipping.isImage())
			{
				
			}
			else
			{	// text clipping
				
			}
		}
//		if (outlink != null)
//			infoCollector.addClosureToPool(imageClipping.getOutlinkClosure());
		
	}
}
