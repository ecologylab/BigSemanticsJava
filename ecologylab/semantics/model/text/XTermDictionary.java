package ecologylab.semantics.model.text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ecologylab.io.Assets;
import ecologylab.appframework.ApplicationProperties;


public class XTermDictionary implements ApplicationProperties
{

  private static Hashtable<String, Double> frequencyList = null;
  private static Hashtable<String, XTerm> dictionary = null;

  public static double lowestFrequency 	= Double.MAX_VALUE;
  public static double highestFrequency	= Double.MIN_VALUE;
  public static double lowestIDF  			= Double.MAX_VALUE;
  public static double highestIDF   		= Double.MIN_VALUE;
  public static double averageIDF       = 0;
  
  public static double corpusSize = -1; 

  static String DICTIONARY = "dictionary";

  public static void download(float dictionaryAssetVersion)
  {
    Assets.downloadSemanticsZip(DICTIONARY, null, !USE_ASSETS_CACHE,
        dictionaryAssetVersion);
    try
    {
      createDictionary(Assets.getSemanticsFile(DICTIONARY + "/dictionary.yaml"));
    } catch (Exception e)
    {
      System.err.println("Error: cannot open dictionary file.");
    }
  }
  
  
  /**
   * Loads the dictionary term/frequency map from a YAML file, then generates
   * the IDF Mapping.
   * 
   * @param yamlTermFrequency
   *            file from which to load the term/frequency map
   * @throws FileNotFoundException
   */
  public static void createDictionary(File dictionary)
  throws Exception
  {
    readFromDictionaryFile(Assets.getSemanticsFile("dictionary" + "/Dic.txt"));
    generateDictionary();
  }
  
  
  public static XTerm getTerm(String stem) {
    if (contains(stem))
      return dictionary.get(stem);
    else
      return newTerm(stem);
  }

  /**
   * Tests if the dictionary has an entry for a certain stem.
   * @param stem
   * @return
   */
  public static boolean contains(String stem)
  {
    return dictionary.containsKey(stem);
  }
  
  /**
   * Creates a new term using the given stem and assigning it an idf of averageIDF.
   * @param stem
   */
  public static XTerm newTerm(String stem) {
    if (stem.length() < 4)
      return null;
    XTerm newTerm = new XTerm(stem, averageIDF);
    dictionary.put(stem, newTerm);
    return newTerm;
  }
  
  public static int numTerms() {
    return frequencyList.size();
  }

  private static void readFromDictionaryFile(File inputDictionaryFile) throws Exception
  {
    String thisTerm; 
    InputStream in    = null;
    String fileName   = inputDictionaryFile.getName();
    char fileType   = fileName.charAt(fileName.length()-3);
    switch (fileType)
    {
    case 't':
      in = new FileInputStream(inputDictionaryFile);
      break;
    case 'z':
      in = new FileInputStream(inputDictionaryFile);
      ZipInputStream source = new ZipInputStream(in);
      ZipEntry anEntry = source.getNextEntry(); 
      long decompressedSize = anEntry.getSize();
      byte[]  uncompressedBuf = new byte[(int)decompressedSize];  
      int   readLength=0;
      int   chunk=0;
      while (((int)decompressedSize - readLength) > 0) 
      {
        chunk=source.read(uncompressedBuf,readLength,(int)decompressedSize - readLength);
        if (chunk==-1) {
          break;
        }
        readLength+=chunk;
      }
      in  = new ByteArrayInputStream (uncompressedBuf);            
      break;
    }
    BufferedReader myInput = new BufferedReader(new InputStreamReader(in));
    Hashtable<String,Double> frequencies = new Hashtable<String, Double>();
    while ((thisTerm = myInput.readLine()) != null) 
    {              
      String[] term     = thisTerm.split("\t");
      String stem = term[0];
      double freq = Double.parseDouble(term[1]);
      if (freq < lowestFrequency)
        lowestFrequency = freq;
      else if (freq > highestFrequency)
        highestFrequency = freq;
      frequencies.put(stem, freq);      
    }
    frequencyList = frequencies;
    myInput.close();
    myInput   = null;
  }


  private static void generateDictionary()
  {
    dictionary = new Hashtable<String, XTerm>(frequencyList.size());
    corpusSize = highestFrequency;
    double avgIDF = 0;
    for (String stem : frequencyList.keySet())
    {
      double idf = Math.log(corpusSize / frequencyList.get(stem));
      dictionary.put(stem, new XTerm(stem, idf));
      if (idf < lowestIDF)
        lowestIDF = idf;
      else if (idf > highestIDF)
        highestIDF = idf;
      avgIDF += idf;
    }
    avgIDF /= frequencyList.size();
  }

}