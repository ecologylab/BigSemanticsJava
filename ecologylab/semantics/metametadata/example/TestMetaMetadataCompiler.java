package ecologylab.semantics.metametadata.example;

import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.xml.XMLTranslationException;

/**
 * 
 * @author quyin
 *
 */
public class TestMetaMetadataCompiler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			// use the default repository location
			new MetadataCompiler(args);
		} catch (XMLTranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
