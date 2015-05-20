package ecologylab.bigsemantics.metametadata.fieldparsers;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.metametadata.fieldparsers.FieldParserForAcmReferences;

public class TestFieldParserForAcmReferences
{

  FieldParserForAcmReferences parser;
  
  @Before
  public void setup()
  {
    parser = new FieldParserForAcmReferences();
  }
  
  public void runTest(String ref, String expectedAuthorList, String expectedTitle)
  {
    Map<String, String> result = parser.getKeyValuePairResult(null, ref);
    String authors = result.get(FieldParserForAcmReferences.AUTHOR_LIST);
    String title = result.get(FieldParserForAcmReferences.TITLE);
    Assert.assertEquals(expectedAuthorList, authors);
    Assert.assertEquals(expectedTitle, title);
  }

  @Test
  public void testCase1()
  {
    runTest(
				"George W. Furnas , Samuel J. Rauch, Considerations for information environments and the NaviQue workspace, Proceedings of the third ACM conference on Digital libraries, p.79-88, June 23-26, 1998, Pittsburgh, Pennsylvania, United States  [doi>10.1145/276675.276684]",
        "George W. Furnas , Samuel J. Rauch",
        "Considerations for information environments and the NaviQue workspace");
  }

  @Test
  public void testCase2()
  {
    runTest(
				"Miller, G.A., The Magical number seven, plus or minus two: some limits on our capacity for processing information, Psychology Review, 63, 81--97, 1956. ",
				"Miller, G.A.",
				"The Magical number seven, plus or minus two: some limits on our capacity for processing information");
  }

  @Test
  public void testCase3()
  {
    runTest(
				"Hamming, R. The Art of Doing Science and Engineering: Learning to Learn. CRC Press, 1997, 35. {The original maxim is, of course, \"The purpose of computing is insight, not numbers.\"} ",
				"Hamming, R.",
				"The Art of Doing Science and Engineering: Learning to Learn");
  }

  @Test
  public void testCase4()
  {
    runTest(
				"Karlson, A., Piatko, C., and Gersh, J. Semantic navigation in complex graphs. Interactive poster and demonstration. Abstract published in IEEE Symposium on Information Visualization Poster Compendium (Seattle, WA), 2003, 84--85. ",
				"Karlson, A. , Piatko, C. , Gersh, J.",
				"Semantic navigation in complex graphs");
  }

  @Test
  public void testCase5()
  {
    runTest(
				"Smith, S. M., Getting Into and Out of Mental Ruts: A theory of Fixation, Incubation, and Insight in Sternberg, R J. and Davidson, J., The Nature of Insight, Cambridge, MA, MIT Press, 1994, 121--149. ",
				"Smith, S. M.",
				"Getting Into and Out of Mental Ruts: A theory of Fixation");
  }

  @Test
  public void testCase6()
  {
    runTest(
				"Smith, S. M., Dodds, R. A., Incubation. in Runco, M.A., Pritzker, S. R., eds., Encyclopedia of Creativity, Volume 2. San Diego: Assoc Press, 1999, 39--44. ",
				"Smith, S. M. , Dodds, R. A.",
				"Incubation");
  }

  @Test
  public void testCase7()
  {
    runTest(
				"Smith, S.M., Blankenship, S.E., Incubation and the Persistence of Fixation in Problem Solving, Am Journ Psychology, 104, 1991, 61--87. ",
				"Smith, S.M. , Blankenship, S.E.",
				"Incubation and the Persistence of Fixation in Problem Solving");
  }

  @Test
  public void testCase8()
  {
    runTest(
				"Shah, J.J., Smith, S.M., Vargas-Hernandez, N. Metrics for Measuring Ideation Effectiveness. Design Studies, 24, 2003, 111--134.",
				"Shah, J.J. , Smith, S.M. , Vargas-Hernandez, N.",
				"Metrics for Measuring Ideation Effectiveness");
  }

  @Test
  public void testCase9()
  {
    runTest(
				"Sperling, G. The information available in brief visual presentations. Psychological Monographs, 74:48.",
				"Sperling, G.",
				"The information available in brief visual presentations");
  }

  @Test
  public void testCase10()
  {
    runTest(
				"Newell, A., Shaw, J. C., Simon, H. A. The process of creative thinking. In Gruber, H. E., Terrell, G., Wertheimer, M., eds., Contemporary approaches to creative thinking, New York: Atherton Press, 1962.",
				"Newell, A. , Shaw, J. C. , Simon, H. A.",
				"The process of creative thinking");
  }

//  @Test
//  public void testCase11()
//  {
//    runTest(
//				"Oxford English Dictionary on Compact Disk, 2nd Edition. Oxford: Oxford University Press, 1992.",
//				"",
//				"");
//  }

  @Test
  public void testCase12()
  {
    runTest(
        "Antoniou, G., and van Harmelen, F. A Semantic Web Primer. The MIT Press, 2004.",
        "Antoniou, G. , van Harmelen, F.",
        "A Semantic Web Primer");
  }

  @Test
  public void testCase13()
  {
    runTest(
        "Huynh, D., et al. Exhibit: lightweight structured data publishing. In Proc. of WWW (2007).",
        "Huynh, D., et al.",
        "Exhibit: lightweight structured data publishing");
  }

  @Test
  public void testCase14()
  {
    runTest(
        "28. Kerne, A., et al. Meta-metadata: a metadata semantics language for collection representation applications. In Proc. of CIKM (2010).",
        "Kerne, A., et al.",
        "Meta-metadata: a metadata semantics language for collection representation applications");
  }

  @Test
  public void testCase15()
  {
    runTest(
        "Foss, C. L. Detecting lost users: Empirical studies on browsing hypertext. Tech. rep., 1989.",
        "Foss, C. L.",
        "Detecting lost users: Empirical studies on browsing hypertext");
  }

  @Test
  public void testCase16()
  {
    runTest(
        "Huynh, D. F., Mazzocchi, S., and Karger, D. Piggy bank: Experience the semantic web inside your web browser. In Proc. of ISWC (2005).",
        "Huynh, D. F. , Mazzocchi, S. , Karger, D.",
        "Piggy bank: Experience the semantic web inside your web browser");
  }

}
