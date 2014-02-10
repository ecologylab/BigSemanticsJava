package ecologylab.bigsemantics.documentparsers;

import java.io.IOException;

import ecologylab.bigsemantics.metadata.builtins.Image;

/**
 * TODO
 */
public abstract class ImageParser extends DocumentParser<Image>
{

  public ImageParser()
  {
    super();
  }

  @Override
  public void parse() throws IOException
  {
    // To be overridden
  }

}
