package ecologylab.bigsemantics.downloaders.controllers;

/**
 * Factory for NewDefaultDownloadControllerFactory.
 * 
 * @author quyin
 */
public class NewDefaultDownloadControllerFactory implements DownloadControllerFactory
{

  @Override
  public NewDownloadController createDownloadController()
  {
    return new NewDefaultDownloadController();
  }

}
