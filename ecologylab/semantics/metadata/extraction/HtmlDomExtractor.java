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
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scholarlyPublication.Author;
import ecologylab.semantics.library.scholarlyPublication.Reference;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.xml.FieldAccessor;
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
	
	private String domainString;
	static Tidy tidy;
	static XPath xpath;
	

	public HtmlDomExtractor()
	{
		tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		xpath = XPathFactory.newInstance().newXPath();
	}
	/**
	 * Checks to see which HtmlDomExtractionPattern can be used and calls
	 * <code>extractMetadataWithPattern</code> using the appropriate patter
	 * 
	 * @param purl
	 * @return
	 * @throws XmlTranslationException
	 */
	public M populateMetadata(ParsedURL purl, M  metadata, String domainString)
	{
		MetaMetadata metaMetadata = metadata.getMetaMetadata();
		this.domainString = domainString;
		if(metaMetadata.isSupported(purl))
		{
			PURLConnection purlConnection 	= purl.connect(metaMetadata.getUserAgentString());
			Document tidyDOM 				= tidy.parseDOM(purlConnection.inputStream(), null /*System.out*/);
			
			M populatedMetadata 			= recursiveExtraction(metadata, metaMetadata, tidyDOM, purl);

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
	 * @param metadata
	 * @param mmdField
	 * @param tidyDOM
	 * @param purl
	 * @return
	 */
	private M recursiveExtraction(M metadata, MetaMetadataField mmdField, Document tidyDOM, ParsedURL purl)
	{
		//Gets the child metadata of the mmdField.
		HashMapArrayList<String, MetaMetadataField> mmdFieldSet = mmdField.getSet();
		
		/********************debug********************/
		for (int i = 0; i < mmdFieldSet.size(); i++)
		{
			MetaMetadataField mmdElement 	= mmdFieldSet.get(i);
			String mmdElementName 			= mmdElement.getName();
			println(mmdElementName);
		}
		/*********************************************/
		
		//Traverses through the child metadata to populate.
		for (int i = 0; i < mmdFieldSet.size(); i++)
		{
			MetaMetadataField mmdElement 	= mmdFieldSet.get(i);

			String xpathString 				= mmdElement.getXpath(); //Used to get the field value from the web page.
			String mmdElementName 			= mmdElement.getName();
			if(mmdElement.getSet() == null) 
			{
				if (xpathString != null)
				{
					try
					{
						if (mmdElement.isList())
						{
							NodeList nodes = (NodeList) xpath.evaluate(xpathString, tidyDOM, XPathConstants.NODESET);
							ArrayList<String> values = new ArrayList<String>();
							for (int j = 0; j < nodes.getLength(); j++)
							{
								String nodeValue = nodes.item(j).getNodeValue();
								values.add(nodeValue);
								//Removing the unwanted html chars.
								nodeValue = XMLTools.unescapeXML(nodeValue);
								nodeValue = nodeValue.trim();
								metadata.set(mmdElementName, nodeValue);
								// println(mdElement.getName() + ":\t" + nodeValue);
							}
						} 
						else
						{
							String evaluation = xpath.evaluate(xpathString, tidyDOM);
							String stringPrefix = mmdElement.getStringPrefix();
							//
							if (stringPrefix != null)
							{
								if (evaluation.startsWith(stringPrefix))
								{
									evaluation = evaluation.substring(stringPrefix.length());
									evaluation = XMLTools.unescapeXML(evaluation);
									evaluation = evaluation.trim();
								} else
									evaluation = null;
							}
							// println(mdElement.getName() + ":\t" + evaluation);
							
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
					Field field 				= fieldAccessor.getField();
					
					try
					{
						nestedMetadata 				= (M) field.get(metadata);
					} catch (IllegalArgumentException e)
					{
						e.printStackTrace();
					} catch (IllegalAccessException e)
					{
						e.printStackTrace();
					}
					
					if(nestedMetadata == null)
					{
						nestedMetadata = (M) ReflectionTools.getInstance(field.getType());
						ReflectionTools.setFieldValue(metadata, field, nestedMetadata);
					}
					recursiveExtraction(nestedMetadata, mmdElement, tidyDOM, purl);
				}
				//If the field is mapped.
				else if(mmdElement.isMap())
				{
					FieldAccessor fieldAccessor = metadata.getMetadataFieldAccessor(mmdElementName);
					Field field = fieldAccessor.getField();
					HashMapArrayList<Object, Metadata> mappedMetadata;
					
					mappedMetadata = (HashMapArrayList<Object, Metadata>) ReflectionTools.getFieldValue(metadata , field);
					if(mappedMetadata == null)
					{
						//mappedMetadata is not initialized.
						mappedMetadata = (HashMapArrayList<Object, Metadata>) ReflectionTools.getInstance(field.getType());
						ReflectionTools.setFieldValue(metadata, field, mappedMetadata);
					}					

					HashMapArrayList<String, MetaMetadataField> mmdFieldSetChild = mmdElement.getSet();//author
					
					//Populating the key data.
					String key = mmdElement.getKey();
					MetaMetadataField mmdChildElement = mmdFieldSetChild.get(key);
					
					String xpathStringChild = mmdChildElement.getXpath();
					String mmdElementNameChild = mmdChildElement.getName();
					NodeList nodes = null;
					
					try
					{
						nodes = (NodeList) xpath.evaluate(xpathStringChild, tidyDOM, XPathConstants.NODESET);
					} catch (XPathExpressionException e)
					{
						e.printStackTrace();
					}
					ArrayList<String> values = new ArrayList<String>();
					int keys = nodes.getLength();
					
					for (int j = 0; j < keys; j++)
					{
						String nodeValue = nodes.item(j).getNodeValue();
						//Forming proper ParsedURL from the relative Path.
						if(mmdChildElement.getScalarType() instanceof ParsedURLType)
						{
							ParsedURL parsedURL = null;
							try
							{
								if(nodeValue != "null" && (nodeValue.length() != 0))
								{
									parsedURL = ParsedURL.getRelative(new URL(domainString),nodeValue, "");
									nodeValue = parsedURL.toString();
								}
							} catch (MalformedURLException e)
							{
								//Should not come here b'coz the domainString has to be properly formed all the time.
								e.printStackTrace();
							}
						}
						
						//Generic access of Element Type
						Type[] typeArgs = ReflectionTools.getParameterizedTypeTokens(field);
						Type mapVElementType = typeArgs[1];
						Class mapVElementClass = (Class) mapVElementType;
						mappedMetadata.put(nodeValue, (Metadata) ReflectionTools.getInstance(mapVElementClass));
						Metadata mapVElement = mappedMetadata.get(nodeValue);
						mapVElement.set(key, nodeValue);
						
						
						/***************clean up*****************/
						//Populating one author at a time into the HashMapArrayList.
//						if(mmdElementName.equals("authors"))
//						{	
//							
//							mappedMetadata.put(nodeValue, new Author());
//							Metadata mapVElement = mappedMetadata.get(nodeValue);
//							mapVElement.set(key, nodeValue);
//						}
//						//Populating one reference/citaiton at a time into the HashMapArrayList.
//						if(mmdElementName.equals("references") || mmdElementName.equals("citations"))
//						{
//							mappedMetadata.put(nodeValue, new Reference());
//							Metadata mapVElement = mappedMetadata.get(nodeValue);
//							mapVElement.set(key, nodeValue);
//						}
						/******************************************/
					}
					
					//Populating the other attributes in the map
					for (int k = 0; k < mmdFieldSetChild.size(); k++)
					{
						mmdChildElement = mmdFieldSetChild.get(k);
						xpathStringChild = mmdChildElement.getXpath();
						mmdElementNameChild = mmdChildElement.getName();
						if(!key.equals(mmdElementNameChild))
						{
							try
							{
								nodes = (NodeList) xpath.evaluate(xpathStringChild, tidyDOM, XPathConstants.NODESET);
							} catch (XPathExpressionException e)
							{
								e.printStackTrace();
							}
							values = new ArrayList<String>();
							//debug
//							int attributes = nodes.getLength();
//							int size2 = mappedMetadata.size();
							
							for (int l = 0; l < nodes.getLength() && l < keys && l < mappedMetadata.size(); l++)
							{
								String nodeValue = nodes.item(l).getNodeValue();
								
								//debug
//								println("Attribute Name: "+mmdElementNameChild + "Attribute value: "+nodeValue);
//								println("No of Keys: "+keys + "index:  "+l);
								
								Metadata mapVElement = mappedMetadata.get(l);
								//Forming proper ParsedURL from the relative Path.
								if(mmdChildElement.getScalarType() instanceof ParsedURLType)
								{
									ParsedURL parsedURL = null;
									try
									{
										if(nodeValue != "null" && (nodeValue.length() != 0))
										{
											parsedURL = ParsedURL.getRelative(new URL(domainString),nodeValue, "");
											nodeValue = parsedURL.toString();
										}
									} catch (MalformedURLException e)
									{
										//Should not come here b'coz the domainString has to be properly formed all the time.
										e.printStackTrace();
									}
								}
								if(mapVElement != null)
								{
									nodeValue = XMLTools.unescapeXML(nodeValue);
									nodeValue = nodeValue.trim();
									mapVElement.set(mmdElementNameChild, nodeValue);
								}
							}
						}
					}//for
				}
			}
		}
		return metadata;
	}
}
