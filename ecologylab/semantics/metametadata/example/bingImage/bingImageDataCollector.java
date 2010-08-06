package ecologylab.semantics.metametadata.example.bingImage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.metametadata.example.MyInfoCollector;
import ecologylab.semantics.metametadata.example.SaveReportSemanticAction;
import ecologylab.semantics.metametadata.example.bingImage.generated.BingImage;
import ecologylab.semantics.metametadata.example.bingImage.generated.BingImageType;
import ecologylab.semantics.metametadata.example.bingImage.generated.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metametadata.example.bingImage.sqlTranslator.DBInterface;
import ecologylab.semantics.metametadata.example.bingImage.sqlTranslator.DBUtil;
import ecologylab.semantics.metametadata.example.bingImage.sqlTranslator.SqlTranslator;
import ecologylab.semantics.metametadata.example.bingImage.sqlTranslator.SqlTranslatorUtil;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

public class bingImageDataCollector{
	public static List<BingImageType> metadataCollected = new ArrayList<BingImageType>(); 
	
	public static void main(String[] args) throws Exception
	{ 
		SemanticAction.register(SaveReportSemanticAction.class);
		SemanticAction.register(bingImageSemanticAction.class);
		
		MyInfoCollector mic = new MyInfoCollector("repo", GeneratedMetadataTranslationScope.get());
		
		/* Test cases
		ParsedURL flickr_search = ParsedURL.getAbsolute("http://www.flickr.com/search/?q=lion");
		ParsedURL flickr_search_detailed = ParsedURL.getAbsolute("http://www.flickr.com/search/?q=texas&z=m");
		ParsedURL google_search_focused = ParsedURL.getAbsolute("http://www.google.com/search?q=texas+site%3Awww.nytimes.com");
		ParsedURL google_search_focused_ny = ParsedURL.getAbsolute("http://www.google.com/search?q=texas+site%3Awww.wunderground.com");
		ParsedURL google_search = ParsedURL.getAbsolute("http://www.google.com/search?q=texas");
		ParsedURL google_image_search = ParsedURL.getAbsolute("http://www.google.com/images?q=texas&sout=1");
		ParsedURL yahoo_web_search_xml = ParsedURL.getAbsolute("http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=yahoosearchwebrss&query=texas");
		ParsedURL yahoo_img_search_xml = ParsedURL.getAbsolute("http://api.search.yahoo.com/ImageSearchService/V1/imageSearch?appid=yahoosearchwebrss&query=texas");
		ParsedURL wikipedia_search = ParsedURL.getAbsolute("http://en.wikipedia.org/w/index.php?title=Special%3ASearch&redirs=1&search=korea&fulltext=Search&ns0=1");
		ParsedURL bing_web_search_xml = ParsedURL.getAbsolute("http://api.bing.net/xml.aspx?Sources=web+spell&AppId=828DA72AA6D172560F256E7B3784FA2295CB7D99&Version=2.2&Market=en-US&Query=texas&Web.Count=20");
		ParsedURL bing_web_search = ParsedURL.getAbsolute("http://www.bing.com/search?q=seoul&go=&form=QBLH&qs=n&sk=&sc=8-5");		
		*/ 
		
		ParsedURL bing_image_search_detailed = ParsedURL.getAbsolute("http://www.bing.com/images/search?q=seoul#lod4");
		System.out.println("************" + bing_image_search_detailed);
		
		mic.getContainerDownloadIfNeeded(null, bing_image_search_detailed, null, false, false, false);
		Thread.sleep(5000); 
		mic.getDownloadMonitor().stop(); 
		
//		printoutToFile("bingImage.csv", metadataCollected);
//		printoutToDB(GeneratedMetadataTranslationScope.get(), metadataCollected);
	}
	
	/**
	 * print out to file 
	 * @param filename
	 * @param metadataCollected
	 * @throws FileNotFoundException
	 */
	private static void printoutToFile(String filename, List metadataCollected) throws FileNotFoundException{
		PrintWriter pw = new PrintWriter(new FileOutputStream(filename)); 
		pw.printf("#format:caption, img_url, img_ref, img_property\n");
		
		if(metadataCollected.size() > 0){
			for (Object obj : metadataCollected)
			{
				if(obj instanceof BingImageType){
					BingImageType bing = (BingImageType)obj;
					ArrayList<BingImage> bingImages = bing.getBingImages();
					for (BingImage bingImage : bingImages)
					{
						String collected = bingImage.getCaption() + " " + bingImage.getImgUrl() + " " +   
								bingImage.getImgProperty() + " " + bingImage.getImgRef();
						System.out.println(collected + "\n");

						pw.printf("%s, %s, %s, %s\n",
								bingImage.getCaption(),
								bingImage.getImgUrl(),
								bingImage.getImgRef(),
								bingImage.getImgProperty());
					}					
				}
			}  
			pw.flush(); 
			pw.close();
			
		}else{
			Debug.println("no results retrieved");
		}

	}
	
	/**
	 * print out to DB 
	 * @param targetTranslationScope
	 * @param metadataCollected
	 * @throws SIMPLTranslationException 
	 * @throws IOException 
	 */
	private static void printoutToDB(TranslationScope ts, List<BingImageType> mc) throws Exception{
		SqlTranslator t = new SqlTranslator(); 
		
		SqlTranslatorUtil.setDEFAULT_SQL_OUTPUT_DIRECTORY("ecologylab//semantics//metametadata//example//bingImage"); 
		t.createSQLTableSchema(ts, SqlTranslatorUtil.DEFAULT_CREATE_TABLE_MODE);
		
		/*part of result table script generated above
		--MetadataClassDescriptor[ecologylab.semantics.metametadata.example.bingImage.generated.BingImage]
	      CREATE TABLE BingImage (
	      imgUrl varchar(64) ,	 
	      imgProperty text ,	 
	      imgRef varchar(64) , 
	      caption text 	 
	      )INHERITS (Metadata);*/ 
		String table = "CREATE TABLE bingimage ("  
								      + "imgUrl text ," 	 
								      + "imgProperty text ," 	 
								      + "imgRef text ," 
								      + "caption text" 
								      +  ");"; 

		//db connection & declare psmt & execute insert using metadataCollected 
		DBUtil u = new DBUtil();
		
		Connection conn;
		conn = u.connectToDB(); 

		if(conn != null)
			System.out.println("db connected");
		
		// should commit_mode = true to be effective   
		// TODO: check if table already exists 
		u.executeUpdateQuery(table);
	 
		String insertString = "INSERT INTO bingimage("
											+ "imgUrl, ImgProperty, imgRef, caption) "
											+ "VALUES (?, ?, ?, ?)";
		
		PreparedStatement sqlInsertPsmt = conn.prepareStatement(insertString);
		
		for (BingImageType bit : mc)
		{
			ArrayList<BingImage> images = bit.getBingImages(); 
			for (BingImage bingImage : images)
			{
				sqlInsertPsmt.setString(1,bingImage.getImgUrl().toString());
				sqlInsertPsmt.setString(2,bingImage.getImgProperty());
				sqlInsertPsmt.setString(3,bingImage.getImgRef().toString());
				sqlInsertPsmt.setString(4,bingImage.getCaption());
		
				sqlInsertPsmt.executeUpdate();
			}							
		}
		sqlInsertPsmt.close(); 
		u.closeDBConnection(); 
		
	}
	
	//TODO : test remote access 
	@Test
	public void testDBconnection() throws SQLException, ClassNotFoundException{
		Connection conn = (new DBUtil()).connectToDB("jdbc:postgresql://227.0.0.1:5432/", 
				"comma78", "postgresql");
		if(conn != null)
			System.out.println("db connected");
	}
	
	@Test
	public void testPrintoutToDB() throws Exception{
		BingImage b = new BingImage(); 
		b.setImgUrl(new ParsedURL(new URL("http://www.ecologylab.net")));
		b.setImgRef(new ParsedURL(new URL("http://www.ecologylab.net/metadata")));
		b.setImgProperty("img property");
		b.setCaption("img caption");
		
		BingImageType bi = new BingImageType(); 
		bi.bingImages().add(b);
		
		List<BingImageType> li = new ArrayList<BingImageType>();
		li.add(bi);
		
		TranslationScope ts = GeneratedMetadataTranslationScope.get();
		SqlTranslator t = new SqlTranslator(); 
		SqlTranslatorUtil.setDEFAULT_SQL_OUTPUT_DIRECTORY("ecologylab//semantics//metametadata//example//bingImage"); 
		t.createSQLTableSchema(ts, SqlTranslatorUtil.DEFAULT_CREATE_TABLE_MODE);
		
		String table = "CREATE TABLE bingimage ("  
								      + "imgUrl text ," 	 
								      + "imgProperty text ," 	 
								      + "imgRef text ," 
								      + "caption text" 
								      +  ");";
		DBUtil DB = new DBUtil(); 
		Connection conn = DB.connectToDB("jdbc:postgresql://127.0.0.1:5432/", 
				"comma78", "postgresql");
		
		if(conn != null){
			// should set commit_mode = true to be effective   
			DB.executeUpdateQuery(table);
			String insertString = "INSERT INTO bingimage("
									+ "imgUrl, ImgProperty, imgRef, caption) "
									+ "VALUES (?, ?, ?, ?)";
			PreparedStatement p = conn.prepareStatement(insertString);
			
			for (BingImageType bit : li)
			{
				ArrayList<BingImage> images = bit.getBingImages(); 
				for (BingImage bi2 : images)
				{
					p.setString(1,bi2.getImgUrl().toString());
					p.setString(2,bi2.getImgProperty());
					p.setString(3,bi2.getImgRef().toString());
					p.setString(4,bi2.getCaption());
			
					p.executeUpdate();
				}
			}
			p.close(); 
			DB.closeDBConnection(); 
		}
	}
}
 