package ecologylab.bigsemantics.downloaders.controllers;

import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;

/**
 * Factory for NewDefaultDownloadControllerFactory.
 * 
 * @author quyin
 */
public class NewDefaultDownloadControllerFactory implements DownloadControllerFactory
{

  @Override
  public NewDownloadController createDownloadController(DocumentClosure closure)
  {
    return new NewDefaultDownloadController();
  }

}
