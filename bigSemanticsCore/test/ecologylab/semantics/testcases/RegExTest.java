package ecologylab.semantics.testcases;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
	// get the regular expression
	//	String regularExpression ="((\\w[.-]?){1,3}?\\s?,)+";
		
		String regularExpression ="^(\\s?[a-zA-Z\\-\\s\\.]+[\\s]?,){1,50}";
		String evaluation="John Domingue , Enrico Motta, PlanetOnto: From News Publishing to Integrated Knowledge Management Support, IEEE Intelligent Systems, v.15 n.3, p.26-32, May 2000   ";
		String replacementString="";
		/*
		 * System.out.println("DEBUG:: mmdElementName=\t" + mmdElement.getName());
		 * System.out.println("DEBUG:: regularExpression=\t" + regularExpression);
		 * System.out.println("DEBUG:: evaluation=\t" + evaluation);
		 */

		if (regularExpression != null)
		{
			// create a pattern based on regular expression
			Pattern pattern = Pattern.compile(regularExpression);

			// create a matcher based on input string
			Matcher matcher = pattern.matcher(evaluation);

			// TODO right now we r using regular expressions just to replace the
			// matching string we might use them for more better uses.
			// get the replacement thing.
		
		
			//System.out.println(matcher.groupCount());
			if(matcher.find())
			{
				String temp=matcher.group().trim();
				String[] arr=temp.split(",");
			//	Matcher mat = pat.matcher(temp);
				
			 for(int i=0;i<arr.length;i++)
						System.out.println(arr[i]);
			}
			
		}

	

	}

}
