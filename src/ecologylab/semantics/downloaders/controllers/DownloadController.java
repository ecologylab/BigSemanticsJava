package ecologylab.semantics.downloaders.controllers;

import java.io.IOException;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.DocumentClosure;

public interface DownloadController
{
  
	public void connect(DocumentClosure closure) throws IOException;

	/**
	 * Test if a document with the given PURL has been cached already. Note that even this returns
	 * false, we might actually have cached the document using another URL, since URLs can do
	 * redirections.
	 * 
	 * @param purl
	 * @return
	 */
  public boolean isCached(ParsedURL purl);
  
}
