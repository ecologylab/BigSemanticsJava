package ecologylab.bigsemantics.downloaders.controllers;

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
  NewDownloadController createDownloadController();

}
