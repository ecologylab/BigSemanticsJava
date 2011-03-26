package ecologylab.semantics.metametadata.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import ecologylab.generic.Debug;

public class BibTeXToMMD extends Debug
{
	public static void main(String[] args)
	{
		for (int i=0; i <args.length; i++)
		{
			String fileName = args[i];
			try
			{
				FileInputStream input = new FileInputStream(fileName);
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


}
