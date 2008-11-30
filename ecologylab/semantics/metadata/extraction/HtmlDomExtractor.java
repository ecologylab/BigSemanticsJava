package ecologylab.semantics.metadata.extraction;



import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.types.scalar.ParsedURLType;

/**
 * Extracts Metadata from the AcmPortal pages using the xPath strings in the MetaMetadata.
 * @author bharat
 *
 * @param <M>
 */
public class HtmlDomExtractor<M extends MetadataBase> extends Debug
{
	
	private	String 						domainString;
	
	private	MetaMetadataRepository 	metaMetaDataRepository;
	
	static 	Tidy 							tidy;
	static 	XPath 						xpath;
	

	public HtmlDomExtractor(MetaMetadataRepository metaMetaDataRepository)
	{
		tidy 									= new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		xpath 								= XPathFactory.newInstance().newXPath();
		
		this.metaMetaDataRepository	= metaMetaDataRepository;
	}
	/**
	 * Checks to see which HtmlDomExtractionPattern can be used and calls
	 * <code>extractMetadataWithPattern</code> using the appropriate patter
	 * @param purl
	 * @return
	 * @throws XmlTranslationException
	 */
	public M populateMetadata(M  metadata, ParsedURL purl, String domainString,TranslationScope translationScope)
	{
		MetaMetadata metaMetadata = metadata.getMetaMetadata();
		this.domainString = domainString;
		if(metaMetadata.isSupported(purl))
		{
			PURLConnection purlConnection 	= purl.connect(metaMetadata.getUserAgentString());
			Document tidyDOM 				= tidy.parseDOM(purlConnection.inputStream(), null /*System.out*/);
			M populatedMetadata 			= recursiveExtraction(translationScope, metaMetadata, metadata, tidyDOM, purl);

			return populatedMetadata;
		} else
		{
			println("Pattern supports only URLs starting with: "
					+ metaMetadata.getUrlBase());
		}

		return null;
		
	}
	
	/**
	 * Extracts metadata using the tidyDOM from the purl.
	 * @param translationScope TODO
	 * @param mmdField
	 * @param metadata
	 * @param tidyDOM
	 * @param purl
	 * @return
	 */
	private M recursiveExtraction(TranslationScope translationScope, MetaMetadataField mmdField, M metadata, Document tidyDOM, ParsedURL purl)
	{
		//Gets the child metadata of the mmdField.
		HashMapArrayList<String, MetaMetadataField> mmdFieldSet = mmdField.getSet();
		
		/********************debug********************/
		for (MetaMetadataField mmdElement : mmdFieldSet)
		{
			println(mmdElement.getName());
		}
		/*********************************************/
		
		//Traverses through the child metadata to populate.
		for (MetaMetadataField mmdElement : mmdFieldSet)
		{
			String xpathString 				= mmdElement.getXpath(); //Used to get the field value from the web page.
			String mmdElementName 			= mmdElement.getName();
			if(mmdElement.getSet() == null) 
			{
				if (xpathString != null)
				{
					try
					{
						if (mmdElement.isList())
						{	// linear collection of scalar values
							NodeList nodes = (NodeList) xpath.evaluate(xpathString, tidyDOM, XPathConstants.NODESET);
							//FIXME -- what about other types???!!! -- not always String!
							ArrayList<String> values = new ArrayList<String>();
							for (int j = 0; j < nodes.getLength(); j++)
							{
								String nodeValue = nodes.item(j).getNodeValue();
								//Removing the unwanted html chars.
								nodeValue = XMLTools.unescapeXML(nodeValue);
								nodeValue = nodeValue.trim();
								values.add(nodeValue);
								// println(mdElement.getName() + ":\t" + nodeValue);
							}
							//FIXME -- we are totally hosed here.
							// how are we supposed to set an arraylist of scalar values?!
//							metadata.set(mmdElementName, values);
						} 
						else
						{	// single scalar value
							String evaluation 	= xpath.evaluate(xpathString, tidyDOM);
							String stringPrefix 	= mmdElement.getStringPrefix();
							if (stringPrefix != null)
							{
								if (evaluation.startsWith(stringPrefix))
								{
									evaluation = evaluation.substring(stringPrefix.length());
								} 
								else
									evaluation = null;
							}
							if (evaluation != null)
							{
								evaluation = XMLTools.unescapeXML(evaluation);
								evaluation = evaluation.trim();
							}

							
							/***************************clean up****************************/
							//Adding the URL prefix for proper formation of the URL.
//							if((mmdElementName.equals("full_text") /*|| mmdElementName.equals("img_purl") */|| 
//									mmdElementName.equals("table_of_contents") || mmdElementName.equals("archive") ||
//									mmdElementName.equals("results_page")) && evaluation != null &&
//									evaluation != "null" && (evaluation.length() != 0))
//							{
//								if(ParsedURL.isMalformedURL(evaluation))
//								{
//									evaluation = DOMAIN_STRING + evaluation;
//								}
//								//the url base is different form the domain so creating a constant.
////								evaluation = ACMPORTAL_DOMAIN + evaluation;
//							}
//							if(mmdElement.getScalarType() instanceof ParsedURLType)
//							{
//								if(evaluation != null &&
//										evaluation != "null" && (evaluation.length() != 0) &&
//										ParsedURL.isMalformedURL(evaluation))
//								{
//									evaluation = this.domainString + evaluation;
//								}
//							}
							/****************************************************************/
							//FIXME -- horrible kludge to avoid dealing with semantics design issues of
							// how domain should be propagated to the ParsedURL ScalarType
							evaluation = parsedURLTypeKludge(mmdElement, evaluation);
							
							/**************debug************/
							if(mmdElementName.equals("year_of_publication"))
							{
								println("debug");
//								evaluation = XMLTools.unescapeXML(evaluation);
//								evaluation = evaluation.trim();
//								evaluation = "2006";
							}
//							if(metadata == null)
//							{
//								println("debug");
//							}
							/********************************/
							metadata.set(mmdElementName, evaluation);
						}
					} catch (XPathExpressionException e)
					{
						println("Xpath Evaluation Error for expression:"
								+ xpathString + "\n\t On purl:" + purl.toString());
						e.printStackTrace();
					}
				}
			}
			else // If the meta_metadatafield is nested	or mapped		
			{
				//Gets the corresponding Metadata Field which is nested.
				//Call recursively on the nested or mapped Metadata Field.
				
				M nestedMetadata = null;
				
				//If the field is nested
				if(mmdElement.isNested())
				{
					//Have to return the nested object for the field.
					FieldAccessor fieldAccessor = metadata.getMetadataFieldAccessor(mmdElementName);
					nestedMetadata				= (M) fieldAccessor.getAndPerhapsCreateNested(metadata);
					
					//FIXME use current MetaMetadataField and repository to find child MetaMetadata
					// and set here
					
					recursiveExtraction(translationScope, mmdElement, nestedMetadata, tidyDOM, purl);
				}
				//If the field is mapped.
				//TODO -- support all collection types here
				else if(mmdElement.isMap())
				{
					FieldAccessor fieldAccessor = metadata.getMetadataFieldAccessor(mmdElementName);
					Field field = fieldAccessor.getField();
					HashMapArrayList<Object, Metadata> metadataMap	=
						(HashMapArrayList<Object, Metadata>) ReflectionTools.getFieldValue(metadata , field);
					
					if(metadataMap == null)
					{
						metadataMap = (HashMapArrayList<Object, Metadata>) ReflectionTools.getInstance(field.getType());
						ReflectionTools.setFieldValue(metadata, field, metadataMap);
					}					
					HashMapArrayList<String, MetaMetadataField> mmdChildFieldSet = mmdElement.getSet();//author
					
					//Populating the key data.
					String key = mmdElement.getKey();
					MetaMetadataField mmdChildElement = mmdChildFieldSet.get(key);
					
					String childXPathString 			= mmdChildElement.getXpath();
					String childElementName 			= mmdChildElement.getName();	
					try
					{
						NodeList nodes = (NodeList) xpath.evaluate(childXPathString, tidyDOM, XPathConstants.NODESET);
						int numRows 		= nodes.getLength();
						
						for (int j = 0; j < numRows; j++)
						{
							Node keyNode = nodes.item(j);
							String keyValue	= keyNode.getNodeValue();
							keyValue 			= parsedURLTypeKludge(mmdChildElement, keyValue);
							
							//Generic access of Element Type
							String collectionChildTag	= mmdElement.getCollectionChildType();
							Class collectionChildClass 	= translationScope.getClassByTag(collectionChildTag);
							Metadata collectionChildInstance = (Metadata) ReflectionTools.getInstance(collectionChildClass);
							//FIXME use current MetaMetadataField and repository to find child MetaMetadata
							// and set here
							

							
							metadataMap.put(keyValue, collectionChildInstance);
							if(collectionChildInstance == null)
								println("debug");
							collectionChildInstance.set(key, keyValue);
							
							/***************clean up*****************/
							//Populating one author at a time into the HashMapArrayList.
//							if(mmdElementName.equals("authors"))
//							{	
//								
//								mappedMetadata.put(nodeValue, new Author());
//								Metadata mapVElement = mappedMetadata.get(nodeValue);
//								mapVElement.set(key, nodeValue);
//							}
//							//Populating one reference/citaiton at a time into the HashMapArrayList.
//							if(mmdElementName.equals("references") || mmdElementName.equals("citations"))
//							{
//								mappedMetadata.put(nodeValue, new Reference());
//								Metadata mapVElement = mappedMetadata.get(nodeValue);
//								mapVElement.set(key, nodeValue);
//							}
							/******************************************/
						}
						
						//Populating the other attributes in the map
						for (MetaMetadataField mappedChildElement : mmdChildFieldSet)
						{
							childXPathString = mappedChildElement.getXpath();
							childElementName = mappedChildElement.getName();
							if(!key.equals(childElementName))
							{
								try
								{
									nodes = (NodeList) xpath.evaluate(childXPathString, tidyDOM, XPathConstants.NODESET);
								} catch (XPathExpressionException e)
								{
									e.printStackTrace();
								}
								for (int l = 0; l < nodes.getLength() && l < numRows && l < metadataMap.size(); l++)
								{
									String nodeValue = nodes.item(l).getNodeValue();
									
									//debug
//									println("Attribute Name: "+mmdElementNameChild + "Attribute value: "+nodeValue);
//									println("No of Keys: "+keys + "index:  "+l);
									
									Metadata mapVElement = metadataMap.get(l);
									nodeValue = parsedURLTypeKludge(mappedChildElement, nodeValue);
									if(mapVElement != null)
									{
										nodeValue = XMLTools.unescapeXML(nodeValue);
										nodeValue = nodeValue.trim();
										mapVElement.set(childElementName, nodeValue);
									}
								}
							}
						}//for

					} catch (XPathExpressionException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return metadata;
	}
	private String parsedURLTypeKludge(MetaMetadataField mmdElement,
			String evaluation)
	{
		if(mmdElement.getScalarType() instanceof ParsedURLType)
		{
			ParsedURL parsedURL = null;
			try
			{
				if(evaluation != "null" && (evaluation.length() != 0))
				{
					parsedURL = ParsedURL.getRelative(new URL(domainString),evaluation, "");
					evaluation = parsedURL.toString();
				}
			} catch (MalformedURLException e)
			{
				//Should not come here b'coz the domainString has to be properly formed all the time.
				e.printStackTrace();
			}
		}
		return evaluation;
	}
}
