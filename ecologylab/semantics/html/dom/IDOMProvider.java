package ecologylab.semantics.html.dom;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface IDOMProvider
{

	void setQuiet(boolean b);

	void setShowWarnings(boolean b);

	Node parse(InputStream in, String file, OutputStream out) throws FileNotFoundException;

	Document parseDOM(InputStream inputStream, OutputStream out);

}
