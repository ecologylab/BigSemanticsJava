package testcases;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularExpressionTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String regularExpression="/photos/(\\w+)/s*";
		//String regularExpression="Pages ";
	//	StringBuilder evaluation= new StringBuilder("Pages 33-37");
		StringBuilder evaluation= new StringBuilder("/photos/dcdead/tags/wheat/");
	// create a pattern based on regular expression
		Pattern pattern = Pattern.compile(regularExpression);

		// create a matcher based on input string
		Matcher matcher = pattern.matcher(evaluation);

		// TODO right now we r using regular expressions just to replace the
		// matching string we might use them for more better uses.
		// get the replacement thing.
		String replacementString = "/photos/";
		if (replacementString != null)
		{
			// create string buffer
			StringBuffer stringBuffer = new StringBuffer();
			boolean result = matcher.find();
			if (result)
			{
				evaluation.replace(matcher.start(), matcher.end(), replacementString);
				//matcher.appendReplacement(stringBuffer, replacementString);
//				result = matcher.find();
			}
//			matcher.appendTail(stringBuffer);
//			evaluation = stringBuffer.toString();
			System.out.println(evaluation);
		}
	}

}
