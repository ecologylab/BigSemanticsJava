package testcases;

import java.io.File;

import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.Format;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.StringFormat;
import ecologylab.serialization.TranslationScope;

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
			Image image = (Image) TS.deserialize(file, Format.XML);
			ClassDescriptor.serialize(image, System.out, StringFormat.XML);
//			TestDocument testDocumentfromXML = (TestDocument) TestDocument.translateFromXMLSAX(file, TS);
//			testDocumentfromXML.translateToXML(System.out);
		} catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
