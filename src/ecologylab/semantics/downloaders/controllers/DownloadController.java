package ecologylab.semantics.downloaders.controllers;

import java.io.IOException;

import ecologylab.semantics.metadata.builtins.DocumentClosure;

public interface DownloadController
{
	public void connect(DocumentClosure closure) throws IOException;
}
