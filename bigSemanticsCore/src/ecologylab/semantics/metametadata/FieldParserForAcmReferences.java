package ecologylab.semantics.metametadata;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldParserForAcmReferences extends FieldParser
{

	public static enum Flavor
	{
		UNKNOWN,
		ACM_STANDARD,
		BRIEF,
	}

	public static final String	OTHER									= "$other";

	public static final String	TITLE									= "$title";

	public static final String	AUTHOR_LIST						= "$author_list";

	private static final int		MINIMAL_TITLE_LENGTH	= 5;

	private Pattern							pAuthors							= Pattern.compile("\\s*(?:and\\s*)?([A-Z][a-z]+(-[A-Z][a-z]+)?, [A-Z]\\.(?:\\s*[A-Z]\\.)?)(?:\\s*,)?");

	@Override
	public Map<String, String> getKeyValuePairResult(FieldParserElement parserElement, String input)
	{
		Map<String, String> result = new HashMap<String, String>();
		if (input == null || input.length() == 0)
			return result;

		Flavor flavor = Flavor.UNKNOWN;
		if (input.contains(" , ") || input.contains("doi>") || !input.contains("."))
			flavor = Flavor.ACM_STANDARD;
		else if (input.matches("^[A-Z][a-z]+, [A-Z]\\..*"))
			flavor = Flavor.BRIEF;

		switch (flavor)
		{
		case ACM_STANDARD:
			String[] authorListAndOther = input.split("(?<=\\S),\\s", 2);
			if (authorListAndOther.length == 2)
			{
				String authorList = authorListAndOther[0];
				if (authorList != null)
					result.put(AUTHOR_LIST, authorList.trim());
				String other = authorListAndOther[1];
				if (other != null)
				{
					String[] titleAndOther = other.split(",\\s(?=[A-Z])", 2);
					if (titleAndOther.length == 2)
					{
						String title = titleAndOther[0];
						String other0 = titleAndOther[1];
						if (title != null)
							result.put(TITLE, trimUntilLetter(title).trim());
						if (other0 != null)
							result.put(OTHER, trimUntilLetter(other0).trim());
					}
				}
			}
			break;
		case BRIEF:
			Matcher m = pAuthors.matcher(input);
			int nextPos = 0;
			StringBuilder authors = new StringBuilder();
			while (m.find())
			{
				if (nextPos != m.start()) // assume that author names must be adjacent
					break;
				if (nextPos > 0)
					authors.append(" , ");
				authors.append(m.group(1));
				nextPos = m.end();
			}
			result.put(AUTHOR_LIST, authors.toString());

			int beginTitle = nextPos;
			nextPos = nextPos + MINIMAL_TITLE_LENGTH;
			if (nextPos > input.length())
				nextPos = input.length();
			nextPos = skipCharsUntil(input, nextPos, ",.");
			int endTitle = nextPos;

			nextPos = skipChars(input, nextPos, ", .");
			if (Character.isLowerCase(input.charAt(nextPos)) && !input.startsWith("in ", nextPos))
			{
				endTitle = skipCharsUntil(input, nextPos, ",.");
				nextPos = skipChars(input, endTitle, ", .");
			}
			String title = input.substring(beginTitle, endTitle);
			result.put(TITLE, trimUntilLetter(title).trim());
			String other = input.substring(nextPos);
			result.put(OTHER, trimUntilLetter(other).trim());
			break;
		default:
			result.put(TITLE, input);
		}

		return result;
	}

	private static int skipChars(String s, int start, String chars)
	{
		while (start < s.length() && chars.indexOf(s.charAt(start)) >= 0)
			start++;
		return start;
	}

	private static int skipCharsUntil(String s, int start, String chars)
	{
		while (start < s.length() && chars.indexOf(s.charAt(start)) < 0)
			start++;
		return start;
	}
	
	private static String trimUntilLetter(String s)
	{
		int p = 0;
		while (p < s.length())
		{
			if (Character.isLetter(s.charAt(p)))
				break;
			p++;
		}
		return s.substring(p);
	}

	public static void main(String[] args)
	{
		String[] tests = {
				"George W. Furnas , Samuel J. Rauch, Considerations for information environments and the NaviQue workspace, Proceedings of the third ACM conference on Digital libraries, p.79-88, June 23-26, 1998, Pittsburgh, Pennsylvania, United States  [doi>10.1145/276675.276684]",
				"Miller, G.A., The Magical number seven, plus or minus two: some limits on our capacity for processing information, Psychology Review, 63, 81--97, 1956. ",
				"Hamming, R. The Art of Doing Science and Engineering: Learning to Learn. CRC Press, 1997, 35. {The original maxim is, of course, \"The purpose of computing is insight, not numbers.\"} ",
				"Karlson, A., Piatko, C., and Gersh, J. Semantic navigation in complex graphs. Interactive poster and demonstration. Abstract published in IEEE Symposium on Information Visualization Poster Compendium (Seattle, WA), 2003, 84--85. ",
				"Oxford English Dictionary on Compact Disk, 2nd Edition. Oxford: Oxford University Press, 1992.",
				"Smith, S. M., Getting Into and Out of Mental Ruts: A theory of Fixation, Incubation, and Insight in Sternberg, R J. and Davidson, J., The Nature of Insight, Cambridge, MA, MIT Press, 1994, 121--149. ",
				"Smith, S. M., Dodds, R. A., Incubation. in Runco, M.A., Pritzker, S. R., eds., Encyclopedia of Creativity, Volume 2. San Diego: Assoc Press, 1999, 39--44. ",
				"Smith, S.M., Blankenship, S.E., Incubation and the Persistence of Fixation in Problem Solving, Am Journ Psychology, 104, 1991, 61--87. ",
				"Shah, J.J., Smith, S.M., Vargas-Hernandez, N. Metrics for Measuring Ideation Effectiveness. Design Studies, 24, 2003, 111--134.",
				"Sperling, G. The information available in brief visual presentations. Psychological Monographs, 74:48.",
				"Newell, A., Shaw, J. C., Simon, H. A. The process of creative thinking. In Gruber, H. E., Terrell, G., Wertheimer, M., eds., Contemporary approaches to creative thinking, New York: Atherton Press, 1962.",
		};
		FieldParserForAcmReferences f = new FieldParserForAcmReferences();

		String s = "Smith, S. M., Dodds, R. A., Incubation.";
		Matcher m = f.pAuthors.matcher(s);
		while (m.find())
		{
			for (int g = 0; g <= m.groupCount(); ++g)
				System.out.println("Group " + g + ":  " + m.group(g));
			System.out.println();
		}
		
		for (String test : tests)
		{
			Map<String, String> kv = f.getKeyValuePairResult(null, test);
			System.out.println("authors:  " + kv.get(AUTHOR_LIST));
			System.out.println("title:    " + kv.get(TITLE));
			System.out.println("other:    " + kv.get(OTHER));
			System.out.println();
		}
	}

}
