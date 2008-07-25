package ecologylab.semantics.model.text;

import ecologylab.model.TextChunkBase;
import ecologylab.net.ParsedURL;

public class XTermVectorFactory {
	
	
	public static XTermVector create(String string) {
		return new XTermVector();
	}
	
	//TODO TV: Move to CFTermVectorFactory
	public static XTermVector create(ParsedURL purl) {
		return new XTermVector();
	}
	
	//TODO TV: Move to CFTermVectorFactory
	public static XTermVector create(TextChunkBase textChunk) {
		return new XTermVector();
	}

}
