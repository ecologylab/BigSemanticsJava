package ecologylab.semantics.documentparsers;

import java.io.IOException;

import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.metadata.builtins.Image;

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
