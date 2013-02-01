package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ecologylab.semantics.collecting.DownloadStatus;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.tools.MmTest;
import ecologylab.serialization.SIMPLTranslationException;

public class TestMmdSpeed extends MmTest
{
	PrintStream	print;

	public TestMmdSpeed(String appName) throws SIMPLTranslationException
	{
		super(appName);
		try
		{
			File outfile = new File("TestMmdSpeed.csv");
			print = new PrintStream(outfile);
			System.out.println("Output csv file at:"+outfile.getAbsolutePath());
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		TestMmdSpeed speedTest;
		try
		{
			speedTest = new TestMmdSpeed("TestMmSpeed");
			speedTest.collect(args);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}
	
	boolean shownHeaderYet = false;
	@Override protected void output(DocumentClosure incomingClosure)
	{
		Document document	= incomingClosure.getDocument();
		if (document != null)
		{
			List<Metadata>	allMetadata	= informationComposition.getMetadata();
			if (allMetadata == null)
			{
				allMetadata	= new ArrayList<Metadata>();
				informationComposition.setMetadata(allMetadata);
			}
			allMetadata.add(document);
			String outline = ""+document.getLocation().toString();
			HashMap<DownloadStatus, Long> statusChanges = document.getTransitionTimeToDownloadStatus();
			
			if(shownHeaderYet == false)
			{
				String line = "URL,";
				for(DownloadStatus downloadStatus : DownloadStatus.values())
					line += downloadStatus+",";
				print.append(line+"\n");
				System.out.println(line);
				shownHeaderYet = true;
			}
			for(DownloadStatus downloadStatus : DownloadStatus.values())
			{
			  if(statusChanges.containsKey(downloadStatus))
			  {
			  	outline += ","+statusChanges.get(downloadStatus);
			  }
			  else
			  {
			  	outline += ","+"null";
			  }
			}
			System.out.println(outline);
			print.append(outline+"\n");
			System.out.println(outline);		
		}
	}
}
