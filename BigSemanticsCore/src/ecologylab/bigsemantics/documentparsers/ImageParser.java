package ecologylab.bigsemantics.documentparsers;

import java.io.IOException;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.metadata.builtins.Image;

//TODO: 

public abstract class ImageParser extends DocumentParser<Image>
{
	public ImageParser()
	{
		
	}

	public ImageParser(SemanticsGlobalScope infoCollector)
	{
		super(infoCollector);
	}	
	
	@Override
	public void parse() throws IOException 
	{
		// TODO Auto-generated method stub
		
	}

}
