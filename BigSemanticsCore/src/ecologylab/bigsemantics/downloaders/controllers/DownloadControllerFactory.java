package ecologylab.bigsemantics.downloaders.controllers;

import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;

/**
 * A factory that creates DownloadController objects.
 * 
 * @author quyin
 */
public interface DownloadControllerFactory
{
  
  /**
   * @return A new DownloadController object.
   */
  DownloadController createDownloadController(DocumentClosure closure);

}
