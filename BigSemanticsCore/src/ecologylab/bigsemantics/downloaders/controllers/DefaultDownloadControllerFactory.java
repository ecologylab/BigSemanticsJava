package ecologylab.bigsemantics.downloaders.controllers;

import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;

/**
 * Factory for NewDefaultDownloadControllerFactory.
 * 
 * @author quyin
 */
public class DefaultDownloadControllerFactory implements DownloadControllerFactory
{

  @Override
  public DownloadController createDownloadController(DocumentClosure closure)
  {
    return new DefaultDownloadController();
  }

}
