package ecologylab.semantics.documentparsers;

import java.io.IOException;

import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.metadata.builtins.Image;

//TODO: 

public abstract class ImageParser extends DocumentParser<Image>
{
	public ImageParser()
	{
		
	}

	public ImageParser(SemanticsSessionScope infoCollector)
	{
		super(infoCollector);
	}	
	
	@Override
	public void parse() throws IOException 
	{
		// TODO Auto-generated method stub
		
	}

}
