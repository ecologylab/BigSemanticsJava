package ecologylab.semantics.documentparsers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Pattern;

import ecologylab.net.ParsedURL;

/**
 * 
 * @author quyin
 * 
 */
public class SpecialImageUrlHandler
{

  /**
   * For Google Image, we need to change the source of the image since we don't support HTTPS.
   * 
   * @param imgSrcAttr
   *          The HTTPS image source.
   * @return The HTTP image source that points to the same image.
   */
  public String changeImageUrlIfNeeded(String imgSrcAttr)
  {
    if (imgSrcAttr != null)
    {
      if (Pattern.matches("https://encrypted-tbn\\d+.google.com/images?.*", imgSrcAttr)
          || Pattern.matches("https://encrypted-tbn\\d+.gstatic.com/images?.*", imgSrcAttr))
      {
        imgSrcAttr = imgSrcAttr.replace("https://", "http://");
        imgSrcAttr = imgSrcAttr.replace("//encrypted-tbn", "//tbn");
        imgSrcAttr = imgSrcAttr.replace("gstatic.com", "google.com");
      }
    }
    return imgSrcAttr;
  }

  /**
   * For some other cases with Google Image, we need to parse the image URL from URL parameters.
   * 
   * @param hrefString
   * @return The true image URL if contained in the URL parameters, or null if not contained.
   */
  public String getImageUrlFromParameters(String hrefString)
  {
    if (hrefString != null)
    {
      if (hrefString.startsWith("http://www.google.com/imgres?")
          || hrefString.startsWith("http://images.google.com/imgres?"))
      {
        ParsedURL hrefPURL = ParsedURL.getAbsolute(hrefString);
        Map<String, String> params = hrefPURL.extractParams(false);
        if (params != null && params.containsKey("imgurl"))
          return params.get("imgurl");
      }
    }
    return null;
  }

  /**
   * Image ref URL is the URL of the referring page where the image appears in. In some cases, like
   * Google Image, this ref URL is encoded as a URL parameter, and we need to extract it.
   * 
   * @param imgHref
   *          The original image ref URL.
   * @param outNewImgHref
   *          Buffer to hold the real image ref URL. By default it is the same as the imgHref, but
   *          in cases needed it will be different.
   * @return If we should change the image's source_doc to outNewImgHref.
   * @throws UnsupportedEncodingException
   */
  public boolean changeImageRefUrlAndSourceDocIfNeeded(String imgHref,
                                                       StringBuilder outNewImgHref)
      throws UnsupportedEncodingException
  {
    if (imgHref != null)
    {
      if (imgHref.startsWith("http://www.google.com/imgres?")
          || imgHref.startsWith("http://images.google.com/imgres?"))
      {
        ParsedURL hrefPURL = ParsedURL.getAbsolute(imgHref);
        Map<String, String> params = hrefPURL.extractParams(false);
        if (params != null)
        {
          if (params.containsKey("imgrefurl"))
          {
            String newImgHref = params.get("imgrefurl");
            newImgHref = URLDecoder.decode(newImgHref, "utf-8");
            if (outNewImgHref != null)
              outNewImgHref.append(newImgHref);
            return true;
          }
        }
      }
      else
      {
        if (outNewImgHref != null)
          outNewImgHref.append(imgHref);
      }
    }
    return false;
  }

}
