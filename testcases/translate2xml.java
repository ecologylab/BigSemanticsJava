package testcases;

import java.io.File;
import java.io.IOException;

import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;

public class translate2xml
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		TranslationScope TS = TestTranslationScope.get();
		TestDocument testDocument = new TestDocument();
		TestDocument testDocumentfromXML = new TestDocument();
		testDocument.setContext("Sample Context");
		testDocument.getTitle().setValue("Sample Title");
		testDocument.setValue("Sample scalarText Value");
		File file1 = new File("./testcases/file1.xml");
		try
		{
//			try
//			{
//				testDocument.translateToXML(file1);
				testDocumentfromXML = (TestDocument) TestDocument.translateFromXML(file1, TS);
//			} 
//			catch (IOException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		} catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
