package oldtonew;

//import net.sf.saxon.jdom.DocumentWrapper;
//import net.sf.saxon.xpath.XPathEvaluator;
//import net.sf.saxon.Configuration;
//import net.sf.saxon.TransformerFactoryImpl;
//import org.jdom.Document;
//import org.jdom.Element;
//import org.jdom.JDOMException;
//import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author bharat
 *
 */
public class Convertoldxml
{

	public static void main(String args[]) throws /*JDOMException,*/ IOException, 
			TransformerException, TransformerConfigurationException
	{
//		// Get a TransformerFactory
//        System.setProperty("javax.xml.transform.TransformerFactory",
//                           "net.sf.saxon.TransformerFactoryImpl");
//        TransformerFactory tfactory = TransformerFactory.newInstance();
//        Configuration config = ((TransformerFactoryImpl)tfactory).getConfiguration();
//        
//     // Build the JDOM document
//        SAXBuilder builder = new SAXBuilder();
//        Document doc = builder.build(new File("dynamicLibrary/defaultRepository.xml"));
//
//        // Give it a Saxon wrapper
//        DocumentWrapper docw =
//                new DocumentWrapper(doc, "dynamicLibrary/defaultRepository.xml", config);
//
//     // Compile the stylesheet
//
//        Templates templates = tfactory.newTemplates(new StreamSource("dynamicLibrary/generate.xsl"));
//        Transformer transformer = templates.newTransformer();
//
//        // Now do a transformation
//        
//        transformer.transform(docw, new StreamResult(System.out));
//        
//        System.out.println("Transformation over!!!");
//		
//	}
		String directoryName;  // Directory name entered by the user.
        File directory;        // File object referring to the directory.
        String[] files;        // Array of file names in the directory.
        
//        TextIO.put("Enter a directory name: ");
//        directoryName = TextIO.getln().trim();
        directoryName = "oldxmldirectory/";
        directory = new File(directoryName);
        
        if (directory.isDirectory() == false) {
        	if (directory.exists() == false)
        		System.out.println("There is no such directory!");
        	else
        		System.out.println("That file is not a directory.");
        }
        else {
        	files = directory.list();
        	System.out.println("Files in directory \"" + directory + "\":");
        	for (int i = 0; i < files.length; i++)
        	{
        		System.out.println("   " + files[i]);
        		try
        		{
        		String xmlSystemId = new File(directoryName+files[i]).toURL().toExternalForm();
        		String xsltSystemId = new File("oldtonew/copy.xsl").toURL().toExternalForm();
        		
        		
        		TransformerFactory factory = TransformerFactory.newInstance();
        		Transformer transformer = factory.newTransformer(new StreamSource(xsltSystemId));
        		
//        		transformer.transform(new StreamSource(xmlSystemId), new StreamResult(System.out));
//        		
//        		String xmlSystemIdConvert = new File(directoryName+"copy" + files[i]).toURL().toExternalForm();
        		
        		String xmlSystemIdConvert = new File(directoryName+files[i]).toURL().toExternalForm();
        		String xsltSystemIdConvert = new File("oldtonew/convert.xsl").toURL().toExternalForm();
        		TransformerFactory factoryConvert = TransformerFactory.newInstance();
        		Transformer transformerConvert = factoryConvert.newTransformer(new StreamSource(xsltSystemIdConvert));
        		
        		transformerConvert.setParameter("newFile", "Latest_"+files[i]);
        		
        		transformerConvert.transform(new StreamSource(xmlSystemIdConvert), new StreamResult(System.out));
        		
//        		transformer.transform(new StreamSource(xmlSystemId), new StreamResult(new File("xml2java/Content.txt")));

//        		transformer.transform(arg0, arg1)
//        		TransformerFactory factory = TransformerFactory.newInstance();
//        		Source stylesheet = new StreamSource("xml2java/main.xsl");
////        		Source stylesheet = factory.getAssociatedStylesheet(new StreamSource("xml2java/main.xsl"),null, null, null);
//        		Transformer transformer = factory.newTransformer(stylesheet);
//        		transformer.transform(new StreamSource(new File("xml2java/Content.xml")), new StreamResult(System.out));
        		System.out.println("Transformation over!!!!");
        		} catch(Exception e)
        		{
        			System.out.println("Exception!!!");
        			e.printStackTrace();
        		}
        	}
        }
//		try
//		{
//		String xmlSystemId = new File("oldtonew/news_new.xml").toURL().toExternalForm();
//		String xsltSystemId = new File("oldtonew/copy.xsl").toURL().toExternalForm();
//		
//		
//		TransformerFactory factory = TransformerFactory.newInstance();
//		Transformer transformer = factory.newTransformer(new StreamSource(xsltSystemId));
//		
//		transformer.transform(new StreamSource(xmlSystemId), new StreamResult(System.out));
//		
//		String xmlSystemIdConvert = new File("oldtonew/news_new_copy.xml").toURL().toExternalForm();
//		String xsltSystemIdConvert = new File("oldtonew/convert.xsl").toURL().toExternalForm();
//		TransformerFactory factoryConvert = TransformerFactory.newInstance();
//		Transformer transformerConvert = factoryConvert.newTransformer(new StreamSource(xsltSystemIdConvert));
//		
//		transformerConvert.transform(new StreamSource(xmlSystemIdConvert), new StreamResult(System.out));
//		
////		transformer.transform(new StreamSource(xmlSystemId), new StreamResult(new File("xml2java/Content.txt")));
//
////		transformer.transform(arg0, arg1)
////		TransformerFactory factory = TransformerFactory.newInstance();
////		Source stylesheet = new StreamSource("xml2java/main.xsl");
//////		Source stylesheet = factory.getAssociatedStylesheet(new StreamSource("xml2java/main.xsl"),null, null, null);
////		Transformer transformer = factory.newTransformer(stylesheet);
////		transformer.transform(new StreamSource(new File("xml2java/Content.xml")), new StreamResult(System.out));
//		System.out.println("Transformation over!!!!");
//		} catch(Exception e)
//		{
//			System.out.println("Exception!!!");
//			e.printStackTrace();
//		}
	}
}
