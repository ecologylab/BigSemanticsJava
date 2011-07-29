package ecologylab.semantics.html.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;

/**
 * Provides an interface for DOM providers that is used by DOM parsers
 * @author agh8154
 *
 */
public interface IDOMProvider
{

	/**
	 * If quiet is set to true, certain tags are not parsed
	 * @param quiet
	 */
	void setQuiet(boolean quiet);

	/**
	 * Enables showing warnings while parsing
	 * @param showWarnings
	 */
	void setShowWarnings(boolean showWarnings);

	/**
	 * Creates an org.w3c.dom.Document containing the DOM
	 * @param in
	 * @param out
	 * @return
	 */
	Document parseDOM(InputStream in, OutputStream out) throws IOException;

}
