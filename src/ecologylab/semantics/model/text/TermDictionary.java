package ecologylab.semantics.model.text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ecologylab.appframework.ApplicationProperties;
import ecologylab.collections.CollectionTools;
import ecologylab.io.Assets;
import ecologylab.semantics.namesandnums.SemanticsAssetVersions;

public class TermDictionary implements ApplicationProperties
{

	private static PorterStemmer				stemmer						= new PorterStemmer();

	private static HashMap<String, Double>		frequencyList				= null;

	/**
	 * Maintains a map of a <code>String</code> stem term to it's {@link Term}
	 */
	private static HashMap<String, Term>		dictionary					= new HashMap<String, Term>();

	public static double						averageIDF					= 0;

	public static double						corpusSize					= Double.MAX_VALUE;

	public static StopWordTerm					STOP_WORD					= new StopWordTerm();

	static String								DICTIONARY					= "dictionary";
	
	static Pattern				NO_PUNC_REGEX				= Pattern.compile("[^A-Z^a-z]");

	public static final String[]				mostObviousStopWordStrings	=
																			{ // most generic i.r.
																			// stop words
			"a", "an", "and", "but", "can", "like", "o", "of", "on", "should", "the", "with" };

	public static final String[]				moreStopWordStrings			=
																			{ // more aggressive
																				// i.r.
			// stop words
			"about", "img", "above", "across", "after", "again", "against", "all", "almost", "alone",
			"along", "already", "also", "although", "always", "am", "a.m", "among", "another",
			"any", "anybody", "anyone", "anything", "anywhere", "are", "area", "areas", "around",
			"as", "ask", "asked", "asking", "asks", "at", "away", "b", "back", "backed", "backing",
			"backs", "be", "because", "become", "becomes", "became", "been", "before", "began",
			"behind", "being", "beings", "best", "better", "between", "big", "both", "by", "c",
			"came", "cannot", "case", "cases", "certain", "certainly", "clear", "clearly", "come",
			"could", "d", "did", "differ", "different", "differently", "do", "does", "done",
			"down", "downed", "downing", "downs", "during", "e", "each", "early", "either", "end",
			"ended", "ending", "ends", "enough", "et", "even", "evenly", "ever", "every",
			"everybody", "everyone", "everything", "everywhere", "f", "face", "faces", "fact",
			"facts", "far", "felt", "few", "find", "finds", "first", "for", "four", "from", "full",
			"fully", "further", "furthered", "furthering", "furthers", "g", "gave", "general",
			"generally", "get", "gets", "give", "given", "gives", "go", "going", "good", "goods",
			"got", "great", "greater", "greatest", "group", "grouped", "grouping", "groups", "h",
			"had", "has", "have", "having", "he", "help", "her", "herself", "here", "high",
			"higher", "highest", "him", "himself", "his", "how", "however", "i", "if", "important",
			"in", "interest", "interested", "interesting", "interests", "into", "is", "it", "its",
			"itself", "j", "just", "k", "keep", "keeps", "kind", "knew", "know", "known", "knows",
			"l", "large", "largely", "last", "later", "latest", "least", "less", "let", "lets",
			"likely", "long", "longer", "longest", "m", "made", "make", "making", "man", "many",
			"may", "me", "member", "members", "men", "might", "more", "most", "mostly", "mr",
			"mrs", "much", "must", "my", "myself", "n", "necessary", "need", "needed", "needing",
			"needs", "never", "new", "newer", "newest", "next", "no", "non", "not", "nobody",
			"noone", "nothing", "now", "nowhere", "number", "numbered", "numbering", "numbers",
			"off", "often", "old", "older", "oldest", "once", "one", "only", "open", "opened",
			"opening", "opens", "or", "order", "ordered", "ordering", "orders", "other", "others",
			"our", "out", "over", "p", "page", "part", "parted", "parting", "parts", "per",
			"perhaps", "place", "places", "pm", "p.m.", "point", "pointed", "pointing", "points",
			"possible", "present", "presented", "presenting", "presents", "problem", "problems",
			"put", "puts", "q", "quite", "r", "rather", "really", "right", "room", "rooms", "s",
			"said", "same", "saw", "say", "says", "second", "seconds", "see", "sees", "seem",
			"seemed", "seeming", "seems", "several", "shall", "she", "show", "showed", "showing",
			"shows", "side", "sides", "since", "small", "smaller", "smallest", "so", "some",
			"somebody", "someone", "something", "somewhere", "state", "states", "stopword",
			"still", "such", "sure", "t", "take", "taken", "than", "that", "their", "them", "then",
			"there", "therefore", "these", "they", "thing", "things", "think", "thinks", "this",
			"those", "though", "thought", "thoughts", "three", "through", "thus", "to", "today",
			"together", "too", "took", "toward", "turn", "turned", "turning", "turns", "two", "u",
			"under", "until", "up", "upon", "us", "use", "uses", "used", "v", "very", "w", "want",
			"wanted", "wanting", "wants", "was", "way", "ways", "we", "well", "wells", "went",
			"were", "what", "when", "where", "whether", "which", "while", "who", "whole", "whose",
			"why", "will", "within", "without", "work",
			"worked",
			"working",
			"works",
			"would",
			"x",
			"y",
			"year",
			"years",
			"yet",
			"you",
			"young",
			"younger",
			"youngest",
			"your",
			"yours",
			"z",
			"vs",

			// our web stop words
			"online", "ad", "adv", "advertise", "advertisement", "click", "clicks", "contact",
			"contacs", "e-mail", "e-mails", "email", "emails", "bulletin", "bulletins", "special",
			"specials", "submit", "submits", "send", "sends", "reset", "guideline", "guidelines",
			"select", "selects", "list", "lists", "listing", "listings", "form", "forms", "mail",
			"mails", "mailing", "mailings", "please", "pleases", "forward", "forwards", "post",
			"posts", "you'll", "web-site", "web-sites", "website", "websites", "web", "tel",
			"site", "sites", "site", "sites", "page", "pages", "home", "homepage", "homepages",
			"home-page", "home-pages", "log", "try", "trys", "tiff", "mb", "gif", "jpg", "mov",
			"mpeg", "jpeg", "image", "images", "javascript", "newsletter", "newsletters",
			"download", "downloads", "browser", "browsers", "browse", "following",
			"format",
			"preferred",
			"field",
			"fields",
			"thumbnail",
			"thumbnails",
			"doi","proceedings",//acm portal...

			// agressive
			"past", "headline", "headlines", "announcements", "help", "info", "leading", "company",
			"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "mon",
			"tues", "wed", "thurs", "fri", "sat", "jan", "feb", "mar", "apr", "may", "jun", "jul",
			"aug", "sep", "oct", "nov", "dec", "january", "february", "march", "april", "june",
			"july", "august", "september", "october", "november", "december", "gmt", "est", "edt",
			"cst", "cdt", "pst", "pdt", "inc", "llp", "lp", "lllp", "www", "com",

			// certain site-specific
			"picture", "pictures", "screensaver", "screensavers", "links", "bios", "gallery",
			"galleries", "archive", "archives", "photo", "photos", "photogallery", "bbc", "news" };

	public final static HashMap<String, String>	mostObviousStopWordTerms	= CollectionTools
																					.buildHashMapFromStrings(mostObviousStopWordStrings);

	public final static HashMap<String, String>	stopWordTerms				= CollectionTools
																					.buildHashMapFromStrings(moreStopWordStrings);

	static
	{
		for (String key : mostObviousStopWordStrings)
			stopWordTerms.put(key, key);
	}

	public static void download ( float dictionaryAssetVersion )
	{
		try
		{
			final String ASSET_NAME = "dictionary";
			createDictionary(Assets.getAsset(SemanticsAssetVersions.SEMANTICS_ASSETS_ROOT, ASSET_NAME + "/Dictionary.txt", ASSET_NAME, null, !USE_ASSETS_CACHE, dictionaryAssetVersion));
		}
		catch (Exception e)
		{
			System.err.println("Error: cannot open dictionary file.");
		}
	}

	/**
	 * Loads the dictionary term/frequency map from a YAML file, then generates the IDF Mapping.
	 * 
	 * @param yamlTermFrequency
	 *            file from which to load the term/frequency map
	 * @throws FileNotFoundException
	 */
	synchronized public static void createDictionary ( File dictionary ) throws Exception
	{
		stemStopWords();
		readFromDictionaryFile(dictionary);
	}

	private static final Object	LOCK	= new Object();

	/**
	 * Checks if the given stem is in the dictionary. If so, returns the XTerm associated with that
	 * term. If not, creates a new XTerm from the given stem, adds it to the dictionary, and returns
	 * it.
	 * 
	 * @param stem
	 * @return
	 */
	public synchronized static Term getTerm ( String word, String stem )
	{
		if (dictionary.containsKey(stem))
		{
			Term term = dictionary.get(stem);
			if (!term.hasWord())
				term.setWord(word);
			return term;
		}
		else
			return newTerm(word, stem);
	}

	/**
	 * Tests if the dictionary has an entry for a certain stem.
	 * 
	 * @param stem
	 * @return
	 */
	synchronized public static boolean contains ( Term term )
	{
		return dictionary.containsKey(term.getStem());
	}

	/**
	 * Creates a new term using the given stem and assigning it an idf of averageIDF.
	 * 
	 * @param stem
	 */
	private static Term newTerm ( String word, String stem )
	{
		if (stem.length() < 4 || stopWordTerms.containsKey(stem))
			return STOP_WORD;
		Term newTerm = new Term(stem, /* averageIDF */STOP_WORD.idf());
		newTerm.setWord(word);
		dictionary.put(newTerm.getStem(), newTerm);
		return newTerm;
	}

	private static void readFromDictionaryFile ( File inputDictionaryFile ) throws Exception
	{
		String thisTerm;
		InputStream in = null;
		String fileName = inputDictionaryFile.getName();
		char fileType = fileName.charAt(fileName.length() - 3);
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
			byte[] uncompressedBuf = new byte[(int) decompressedSize];
			int readLength = 0;
			int chunk = 0;
			while (((int) decompressedSize - readLength) > 0)
			{
				chunk = source.read(uncompressedBuf, readLength, (int) decompressedSize
						- readLength);
				if (chunk == -1)
				{
					break;
				}
				readLength += chunk;
			}
			in = new ByteArrayInputStream(uncompressedBuf);
			break;
		}
		BufferedReader myInput = new BufferedReader(new InputStreamReader(in));

		HashMap<String, Double> frequencies = new HashMap<String, Double>();
		HashMap<String, Term> dictionary = new HashMap<String, Term>();
		double avgIDF = 0;

		corpusSize = Double.parseDouble(myInput.readLine());
		while ((thisTerm = myInput.readLine()) != null)
		{
			// Regexes for the dictionary are very costly.
			// substring methods are way cheaper
			// String[] term = thisTerm.split("\t");

			int indexOfTab = thisTerm.indexOf('\t');
			String stem = Term.getUniqueStem(thisTerm.substring(0, indexOfTab));

			if (stopWordTerms.containsKey(stem))
				continue;
			double freq = Double.parseDouble(thisTerm.substring(indexOfTab, thisTerm.length()));
			frequencies.put(stem, freq);
			double idf = Math.log(corpusSize / freq);
			dictionary.put(stem, new Term(stem, idf));
			avgIDF += idf;
		}
		avgIDF /= frequencies.size();
		averageIDF = avgIDF;
		TermDictionary.dictionary = dictionary;
		frequencyList = frequencies;
		myInput.close();
		myInput = null;
	}

	private static void stemStopWords ( )
	{
		String s;
		PorterStemmer p = new PorterStemmer();
		for (int i = 0; i < moreStopWordStrings.length; i++)
		{
			s = moreStopWordStrings[i];
			for (int j = 0; j < s.length(); j++)
				p.add(s.charAt(j));
			p.stem();
			String pString = p.toString();
			stopWordTerms.put(pString, pString);
		}
	}

	/**
	 * Stems and returns a term for a given string.  
	 * Use getTermForUnsafeWord if the string contains 
	 * punctuation or spaces to ensure correct use
	 * of the term model. 
	 * @param s shouldn't have punctuation or spaces
	 * @return Term representing that string
	 */
	public static Term getTermForWord ( CharSequence s )
	{
		PorterStemmer p = stemmer;
		synchronized (p)
		{
			for (int i = 0; i < s.length(); i++)
			{
				char inputChar = s.charAt(i);
				if (!Character.isLetter(inputChar) && (inputChar != '-'))
					System.out.println("AWFUL! bad char =" + inputChar);
				else
					p.add(inputChar);
			}
			p.stem();
			return getTerm(s.toString(), p.toString());
		}

	}

	/**
	 * calls <code>getTerm(string,stem)</code> after stripping the punctuation from the word and
	 * stemming it.
	 * 
	 * @param s
	 *            the word, perhaps with punctuation and spaces in it.
	 * @return
	 */
	public static Term getTermForUnsafeWord ( String s )
	{
		//FIXME -- use a StringBuilder as input or output to avoid repeated replaces!
		Matcher matcher = NO_PUNC_REGEX.matcher(s.toLowerCase());
		return getTermForWord(matcher.replaceAll(""));
	}

}