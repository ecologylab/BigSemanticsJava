package ecologylab.bigsemantics.metadata.extraction;

import junit.framework.Assert;

import org.junit.Test;

import ecologylab.bigsemantics.generated.library.product.*;
import ecologylab.bigsemantics.metadata.MetadataTestHelper;
import ecologylab.net.ParsedURL;

public class NeweggProductPrice
{
	private Object	lockDoc	= new Object();
	
	@Test
	public void testNeweggMetadataExtraction() throws InterruptedException
	{
		ParsedURL purl = ParsedURL.getAbsolute("http://www.newegg.com/Product/Product.aspx?Item=N82E16813128532");
		
		MetadataTestHelper m = new MetadataTestHelper();
		Product p = (Product)m.getMetadata(purl);
		
		Assert.assertNotNull(p.getPrice());
		Assert.assertTrue(p.getPrice().length() > 0);
	}
}
