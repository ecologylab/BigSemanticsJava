package ecologylab.semantics.model.text;

import ecologylab.model.TextChunkBase;
import ecologylab.net.ParsedURL;

public class XTermVectorFactory {
	
	
	public static XVector<XTerm> create(String string) {
		return new XTermVector();
	}
	
	//TODO TV: Move to CFTermVectorFactory
	public static XVector<XTerm> create(ParsedURL purl) {
		return new XTermVector();
	}
	
	//TODO TV: Move to CFTermVectorFactory
	public static XVector<XTerm> create(TextChunkBase textChunk) {
		return new XTermVector();
	}

}
