package testcases;

import java.io.File;
import java.io.IOException;

import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;

public class translate2xml
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		TranslationScope TS = TestTranslationScope.get();
//		TestDocument testDocument = new TestDocument();
//		testDocument.setContext("Sample Context");
//		testDocument.getTitle().setValue("Sample Title");
//		testDocument.setValue("Sample scalarText Value");
//	
		File file = new File("./testcases/file3.xml");
		try
		{
			Image image = (Image) ElementState.translateFromXML(file, TS);
			image.serialize(System.out);
//			TestDocument testDocumentfromXML = (TestDocument) TestDocument.translateFromXMLSAX(file, TS);
//			testDocumentfromXML.translateToXML(System.out);
		} catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
