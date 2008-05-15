package testcases;

import ecologylab.xml.XMLTranslationException;

public class translate2xml
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		TestDocument testDocument = new TestDocument();
		testDocument.setContext("Sample Context");
		testDocument.getTitle().setValue("Sample Title");
		testDocument.setValue("Sample scalarText Value");
		try
		{
			System.out.println(testDocument.translateToXML().toString());
		} catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
