package ecologylab.bigsemantics.metametadata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

public class TestFieldParserForNsfPubList
{

  final static String TEST_HTML = "/ecologylab/bigsemantics/metametadata/testRemovingHtmlTag.html";

  @Test
  public void testRemovingHtmlTag()
  {
    FieldParserForNsfPubList fp = new FieldParserForNsfPubList();

    BufferedReader br = null;
    try
    {
      InputStream stream = getClass().getResourceAsStream(TEST_HTML);
      br = new BufferedReader(new InputStreamReader(stream));
      StringBuilder sb = new StringBuilder();
      while (true)
      {
        String line = br.readLine();
        if (line == null)
          break;
        sb.append(line).append("\n");
      }
      String input = sb.toString();

      fp.getCollectionResult(null, input);
    }
    catch (FileNotFoundException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    finally
    {
      if (br != null)
      {
        try
        {
          br.close();
        }
        catch (IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

}
