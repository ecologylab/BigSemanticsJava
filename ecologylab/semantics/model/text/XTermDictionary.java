package ecologylab.semantics.model.text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ecologylab.io.Assets;
import ecologylab.model.text.Term;
import ecologylab.appframework.ApplicationProperties;


public class XTermDictionary implements ApplicationProperties
{

  private static HashMap<String, Double> frequencyList = null;
  /**
   * Maintains a map of a <code>String</code> stem term to it's {@link XTerm}
   */
  private static HashMap<String, XTerm> dictionary = new HashMap<String, XTerm>();

  public static double averageIDF       = 0;
  
  public static double corpusSize = Double.MAX_VALUE; 

  static String DICTIONARY = "dictionary";
  
  public static final String[] stopWordStrings =
  { // generic i.r. stop words
  "a", "about", "above", "across", "after", "again", "against", "all", "almost",
  "alone", "along", "already", "also", "although", "always", "am", "a.m", "among", "an", "and",
  "another", "any",  "anybody", "anyone", "anything", "anywhere", "are", "area",
  "areas", "around", "as", "ask", "asked", "asking", "asks", "at", "away", "b", "back",
  "backed", "backing", "backs",  "be", "because", "become", "becomes", "became",
  "been", "before", "began", "behind", "being", "beings", "best", "better", "between",
  "big", "both", "but", "by", "c", "came",  "can", "cannot", "case", "cases", "certain",
  "certainly", "clear", "clearly", "come", "could", "d", "did", "differ", "different",
  "differently", "do", "does", "done", "down", "downed",  "downing", "downs", "during",
  "e", "each", "early", "either", "end", "ended", "ending", "ends", "enough", "et", "even",
  "evenly", "ever", "every", "everybody", "everyone", "everything", "everywhere",
  "f", "face", "faces", "fact", "facts", "far", "felt", "few", "find", "finds", "first",
  "for", "four", "from", "full", "fully", "further", "furthered", "furthering",
  "furthers",  "g", "gave", "general", "generally", "get", "gets", "give", "given",
  "gives", "go", "going", "good", "goods", "got", "great", "greater", "greatest",
  "group", "grouped", "grouping",  "groups", "h", "had", "has", "have", "having", "he",
  "help", "her", "herself", "here", "high", "higher", "highest", "him", "himself", "his", "how",
  "however", "i", "if",  "important", "in", "interest", "interested", "interesting",
  "interests", "into", "is", "it", "its", "itself", "j", "just", "k", "keep", "keeps",
  "kind", "knew", "know", "known",  "knows", "l", "large", "largely", "last", "later",
  "latest", "least", "less", "let", "lets", "like", "likely", "long", "longer",
  "longest", "m", "made", "make", "making",  "man", "many", "may", "me", "member",
  "members", "men", "might", "more", "most", "mostly", "mr", "mrs", "much", "must", "my",
  "myself", "n", "necessary", "need",  "needed", "needing", "needs", "never", "new",
  "newer", "newest", "next", "no", "non", "not", "nobody", "noone", "nothing", "now",
  "nowhere", "number", "numbered", "numbering", "numbers", "o", "of", "off", "often",
  "old", "older", "oldest", "on", "once", "one", "only", "open", "opened", "opening",
  "opens", "or", "order", "ordered", "ordering", "orders", "other", "others", "our",
  "out", "over", "p", "page", "part", "parted", "parting", "parts", "per", "perhaps", "place",
  "places", "pm", "p.m.", "point", "pointed", "pointing", "points", "possible", "present",
  "presented", "presenting", "presents", "problem", "problems", "put", "puts", "q",
  "quite", "r", "rather", "really", "right", "room", "rooms", "s", "said", "same", "saw",
  "say", "says", "second", "seconds", "see", "sees", "seem", "seemed", "seeming",
  "seems", "several", "shall", "she", "should", "show", "showed", "showing", "shows",
  "side", "sides", "since", "small", "smaller", "smallest", "so", "some", "somebody",
  "someone", "something", "somewhere", "state", "states", "stopword","still", "such", "sure", "t",
  "take", "taken", "than", "that", "the", "their", "them", "then", "there", "therefore",
  "these", "they", "thing", "things", "think", "thinks", "this", "those", "though",
  "thought", "thoughts", "three", "through", "thus", "to", "today", "together", "too",
  "took", "toward", "turn", "turned", "turning", "turns", "two", "u", "under", "until",
  "up", "upon", "us", "use", "uses", "used", "v", "very", "w", "want", "wanted", "wanting",
  "wants", "was", "way", "ways", "we", "well", "wells", "went", "were", "what", "when",
  "where", "whether", "which", "while", "who", "whole", "whose", "why", "will", "with",
  "within", "without", "work", "worked", "working", "works", "would", "x", "y", "year",
  "years", "yet", "you", "young", "younger", "youngest", "your", "yours", "z", "vs",
 
  // our web stop words
  "online","ad","adv","advertise","advertisement","click", "clicks", "contact", "contacs", "e-mail","e-mails", "email","emails", "bulletin","bulletins", "special","specials", "submit","submits", "send","sends", "reset",
  "guideline", "guidelines", "select","selects", "list","lists", "listing","listings", "form","forms", "mail","mails", "mailing","mailings", "please","pleases", "forward","forwards", "post","posts",
  "you'll", "web-site","web-sites", "website","websites", "web",
  "tel",
  "site","sites", "site", "sites", "page","pages", "home", "homepage","homepages", "home-page", "home-pages",
  "log", "try","trys","tiff","mb","gif","jpg","mov","mpeg","jpeg","image","images","javascript", "newsletter", 
  "newsletters","download","downloads","browser","browsers","browse","following",
  "format", "preferred", "field", "fields", "thumbnail", "thumbnails", 
 
  // agressive
  "past", "headline", "headlines", "announcements", "help", "info", "leading","company", 
  "monday", "tuesday", "wednesday","thursday","friday", "saturday",
  "sunday", "mon", "tues", "wed", "thurs", "fri", "sat",
  "jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec",
  "january","february","march","april","june","july","august","september","october","november","december",
  "gmt", "est", "edt", "cst", "cdt", "pst", "pdt",
  "inc", "llp", "lp", "lllp", "www", "com",

  // certain site-specific
  "picture", "pictures", "screensaver", "screensavers", "links", "bios",
  "gallery", "galleries", "archive", "archives","photo","photos",
  "photogallery"
  };
  public final static ArrayList<String> stopWordTerms = new ArrayList<String>(stopWordStrings.length);

  public static void download(float dictionaryAssetVersion)
  {
    Assets.downloadSemanticsZip(DICTIONARY, null, !USE_ASSETS_CACHE,
        dictionaryAssetVersion);
    try
    {
      createDictionary(Assets.getSemanticsFile("dictionary" + "/Dic.txt"));
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
  synchronized public static void createDictionary(File dictionary)
  throws Exception
  {
  	stemStopWords();
    readFromDictionaryFile(dictionary);
  }
  
  /**
   * Checks if the given stem is in the dictionary.  If so, returns the XTerm associated with that term.
   * If not, creates a new XTerm from the given stem, adds it to the dictionary, and returns it.
   * @param stem 
   * @return
   */
  synchronized public static XTerm getTerm(String stem) {
    if (dictionary.containsKey(stem))
      return dictionary.get(stem);
    else
      return newTerm(stem);
  }

  /**
   * Tests if the dictionary has an entry for a certain stem.
   * @param stem
   * @return
   */
  synchronized public static boolean contains(String stem)
  {
    return dictionary.containsKey(stem);
  }
  
  /**
   * Creates a new term using the given stem and assigning it an idf of averageIDF.
   * @param stem
   */
  private static XTerm newTerm(String stem) {
    if (stem.length() < 4 || stopWordTerms.contains(stem))
      return null;
    XTerm newTerm = new XTerm(stem, averageIDF);
    dictionary.put(stem, newTerm);
    return newTerm;
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
    
    HashMap<String,Double> frequencies = new HashMap<String, Double>();
    HashMap<String, XTerm> dictionary = new HashMap<String, XTerm>();
    double avgIDF = 0;
    corpusSize = Double.parseDouble(myInput.readLine());
    while ((thisTerm = myInput.readLine()) != null) 
    {              
      String[] term     = thisTerm.split("\t");
      String stem = term[0];
      if (stopWordTerms.contains(stem))
        continue;
      double freq = Double.parseDouble(term[1]);
      frequencies.put(stem, freq);    
      double idf = Math.log(corpusSize / freq);
      dictionary.put(stem, new XTerm(stem, idf));
      avgIDF += idf;
    }
    avgIDF /= frequencies.size();
    averageIDF = avgIDF;
    XTermDictionary.dictionary = dictionary;
    frequencyList = frequencies;
    myInput.close();
    myInput   = null;
  }
  
  
  private static void stemStopWords()
  {
    String s;
    PorterStemmer p = new PorterStemmer();
    for(int i=0; i<stopWordStrings.length; i++)
    {
      s = stopWordStrings[i];
      for (int j=0; j<s.length(); j++)
        p.add(s.charAt(j));
      p.stem();
      stopWordTerms.add(p.toString());
    }
  }

}