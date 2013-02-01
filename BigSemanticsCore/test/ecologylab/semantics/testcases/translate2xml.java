package ecologylab.semantics.testcases;

import java.io.File;

import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;

public class translate2xml
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SimplTypesScope TS = TestTranslationScope.get();
//		TestDocument testDocument = new TestDocument();
//		testDocument.setContext("Sample Context");
//		testDocument.getTitle().setValue("Sample Title");
//		testDocument.setValue("Sample scalarText Value");
//	
		File file = new File("./testcases/file3.xml");
		try
		{
			Image image = (Image) TS.deserialize(file, Format.XML);
			SimplTypesScope.serialize(image, System.out, StringFormat.XML);
//			TestDocument testDocumentfromXML = (TestDocument) TestDocument.translateFromXMLSAX(file, TS);
//			testDocumentfromXML.translateToXML(System.out);
		} catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
