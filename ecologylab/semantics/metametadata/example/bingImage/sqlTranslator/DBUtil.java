package ecologylab.semantics.metametadata.example.bingImage.sqlTranslator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.scholarlyPublication.AcmProceeding;
import ecologylab.semantics.generated.library.search.SearchResult;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Entity;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

public class DBUtil extends Debug implements DBInterface
{
	private Connection	thisConnection;

	private Statement		thisStatement;

	public DBUtil(){ 
		
	}
	
	@Test
	public void testDBUtilScenario()
	{
		/*
		 * System.out.println("this is first test");
		 * 
		 * Connection isConnected = connectToDB(); if (isConnected != null) { boolean isSerialized =
		 * serialize(DocumentTest.class); Object thisDeserializedClass = deserialize();
		 * 
		 * DocumentTest thisDocumentClass = null; if (thisDeserializedClass instanceof DocumentTest) {
		 * thisDocumentClass = (DocumentTest) thisDeserializedClass;
		 * System.out.println(thisDocumentClass.getClassName());
		 * 
		 * }
		 * 
		 * } else System.out.println("db connection failed");
		 */
	}

	/**
	 * DBConnection cf. jdbc:[drivertype]:[database] //hostname:portnumber/databasename
	 * 
	 * @param dbURI
	 * @param userName
	 * @param password
	 * @return connection
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Connection connectToDB(String dbURI, String userName, String password)
			throws SQLException, ClassNotFoundException
	{
		Class.forName(POSTGRESQL_DRIVER);

		thisConnection = DriverManager.getConnection(dbURI, userName, password);
		thisConnection.setAutoCommit(POSTGRESQL_DEFAULT_COMMIT_MODE);
		if (thisConnection != null)
		{
			DatabaseMetaData thisDBMetadata = thisConnection.getMetaData();
			println("(" + thisDBMetadata.getUserName() + ") are connected to (" + thisDBMetadata.getURL()
					+ ") " + thisDBMetadata.getDatabaseProductName() + " using "
					+ thisDBMetadata.getDriverVersion());

		}
		else
		{
			println("DB connection is not created");
		}

		return thisConnection;
	}

	/**
	 * connect to default db
	 * 
	 * @return connection
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Connection connectToDB() throws SQLException, ClassNotFoundException
	{
		thisConnection = this.connectToDB(POSTGRESQL_DEFAULT_URI, POSTGRESQL_DEFAULT_USER_NAME,
				POSTGRESQL_DEFAULT_PWD);

		return thisConnection;

	}

	/**
	 * execute select Query
	 * 
	 * @param query
	 * @return resultSet
	 * @throws SQLException
	 */
	public ResultSet executeSelectQuery(String selectQuery) throws SQLException
	{
		if (thisConnection != null)
		{
			thisStatement = thisConnection.createStatement();
			ResultSet thisResultSet = thisStatement.executeQuery(selectQuery);

			return thisResultSet;
		}
		else
			println("db connection is not made : create db connection first.");

		return null;
	}

	/**
	 * execute update Query
	 * 
	 * @param updateQuery
	 * @return number of rows affected
	 * @throws SQLException
	 */
	public int executeUpdateQuery(String updateQuery)
	{
		int thisUpdatedRows = -1 ;
		boolean isCreateTable = false; 
		if (thisConnection != null)
		{
			if(updateQuery.trim().toLowerCase().startsWith("create table"))
				isCreateTable = true;  
			try {
				thisStatement = thisConnection.createStatement();
				thisUpdatedRows = thisStatement.executeUpdate(updateQuery);
			} catch (SQLException e1) {
				if(isCreateTable == true)
					println("[WARNING] table is not created : table might already exist");
				else 
					println("executeUpdateQuery() exception occurred");
			}
			
			return thisUpdatedRows;
		}
		else{
			println("db connection is not made : create db connection first.");
			return -1;
		}
	}

	/**
	 * show data
	 * 
	 * @param thisResultSet
	 * @throws SQLException
	 */
	public void showResultSetDBData(ResultSet thisResultSet) throws SQLException
	{
		StringBuilder thisColumnNameStringBuilder = new StringBuilder();
		StringBuilder thisColumnTypeStringBuilder = new StringBuilder();
		StringBuilder thisDataStringBuilder = new StringBuilder();

		Connection thisColumnCountthisResultSet;
		int thisColumnCount = thisResultSet.getMetaData().getColumnCount();

		// note that column count start from '1'
		for (int i = 1; i <= thisColumnCount; i++)
		{
			thisColumnNameStringBuilder.append(thisResultSet.getMetaData().getColumnName(i).trim()
					+ "|   ");
			thisColumnTypeStringBuilder.append(thisResultSet.getMetaData().getColumnTypeName(i).trim()
					+ "|   ");

		}
		thisColumnNameStringBuilder.append("\n").append(thisColumnTypeStringBuilder.toString().trim())
				.append("\n");

		System.out.println(thisColumnNameStringBuilder);

		// retrieve stored data
		while (thisResultSet.next())
		{
			for (int i = 1; i <= thisColumnCount; i++)
			{
				// should trim unnecessary space for pretty display
				thisDataStringBuilder.append(thisResultSet.getObject(i).toString().trim() + "  ");
			}
			thisDataStringBuilder.append("\n").trimToSize();
		}
		thisDataStringBuilder.append("\n").trimToSize();

		System.out.println(thisDataStringBuilder.toString().trim());
	}

	@Test
	public void testShowResultSetDBData() throws SQLException, ClassNotFoundException
	{
		this.connectToDB();
		ResultSet thisQueryResults = this.executeSelectQuery("select * from bookinfo;");

		this.showResultSetDBData(thisQueryResults);

	}

	@Test
	/*
	 * * Main test method
	 */
	public void testDBConnection() throws ClassNotFoundException, SQLException
	{
		this.connectToDB();
		ResultSet thisQueryResults = this.executeSelectQuery("select * from bookinfo;");

		int thisRetrievedColumnCount = thisQueryResults.getMetaData().getColumnCount();

		// note that column count start from 1
		while (thisQueryResults.next())
		{
			for (int i = 1; i <= thisRetrievedColumnCount; i++)
			{
				System.out.println("data type : " + thisQueryResults.getMetaData().getColumnClassName(i));
				System.out.println("column name : " + thisQueryResults.getMetaData().getColumnName(i));
				System.out.println("thisQueryResults: " + thisQueryResults.getObject(i));
			}
		}

		/*
		 * updateQuery
		 */
		// int thisExecuteUpdateResult =
		// thisStatement.executeUpdate("update bookinfo set sell_price = 12000 where book_name='Korean'");
		// System.out.println(thisExecuteUpdateResult + " rows has been updated");
		this.closeDBConnection();

	}


	@Test
	public void testDBSerializerThruOldJDBC() throws SQLException, ClassNotFoundException
	{
		Connection thisConnection = connectToDB();

		List<String> thisList = new ArrayList<String>();
		thisList.add("add0");
		thisList.add("add1");

		// Serialize.create(pgConnection, testClass.class);

	}

	public ResultSet ExecSQL(String sqlString) throws SQLException
	{
		return this.executeSelectQuery(sqlString);

	}

	@Test
	public void testDBDeserializer() throws SQLException, ClassNotFoundException
	{
		connectToDB();

		String thisStringForReadObject = "select object_value from java_objects where id = ?";
		PreparedStatement thisReadPreparedStatement = thisConnection
				.prepareStatement(thisStringForReadObject);

		int thisIDint = 6;
		thisReadPreparedStatement.setInt(1, thisIDint);
		ResultSet thisResultSet = thisReadPreparedStatement.executeQuery();
		thisResultSet.next();

		// TODO convert byte array into class
		byte[] thisReturnedBytesClass = thisResultSet.getBytes("object_value");
		// Object thisReturnedObject = thisResultSet.getObject(1);

		String thisString = new String(thisReturnedBytesClass);

		System.out.println(thisString);

		thisResultSet.close();
		thisReadPreparedStatement.close();

	}

	@Test
	public void testByteArray() throws UnsupportedEncodingException
	{
		String thisByteArray = "byteaArrayzZ";
		byte[] thisBytes = thisByteArray.getBytes();

		for (byte b : thisBytes)
		{
			System.out.println(b);
		}
	}

	/**
	 * object byte-array converter target object should implement Serializable
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public byte[] convertClassToByteArray(Object obj) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.flush();
		oos.close();
		bos.close();
		byte[] thisConvertedByteArray = bos.toByteArray();
		return thisConvertedByteArray;

	}

	/**
	 * TODO test this code to serialize java object to byte array and vice versa.
	 * 
	 * @throws SIMPLTranslationException
	 */
	@Test
	public void testConvertClassToByteArrayByTranslationScope() throws SIMPLTranslationException
	{
		TranslationScope ts = TranslationScope.get("test", Entity.class);

		Entity e = new Entity();
		CharBuffer buf = CharBuffer.allocate(1000);
		e.serialize(System.out);

		Charset chars = Charset.forName("UTF-8");
		ByteBuffer bytes = chars.encode(buf);

		byte[] bs = new byte[bytes.capacity()];

		buf.clear();
		// decode process
		ByteBuffer readBytes = ByteBuffer.wrap(bs);
		CharBuffer readChars = chars.decode(readBytes);

		Entity out = (Entity) ts.deserializeCharSequence(readChars);

	}

	/**
	 * TODO serialize DocumentTest, PdfTest and restore them
	 * 
	 * ? can the methods also be stored in xml format and restored after ?
	 * 
	 * cf. MonomorphicTutorial.class; PolymorphicTutorial.class; target -
	 */
	@Test
	public void testTranslationScopeSerialize() throws SIMPLTranslationException, IOException
	{
		// setting MetaMetadataRepository 
		TranslationScope ts = TranslationScope.get("ts", AcmProceeding.class, SearchResult.class, Document.class, Metadata.class);
		ts = MetaMetadataTranslationScope.get(); 
		File f = new File("D://Ecologylab5_2010_07_07//web//code//java//cf//config//semantics//metametadata//repositorySources//acmPortal.xml");
		MetaMetadataRepository mmr = MetaMetadataRepository.readRepository(f, ts); 
		
		AcmProceeding ap = new AcmProceeding();
		ap.setRepository(mmr);
		
		SearchResult sr = new SearchResult();
//		sr.setDescription("search description");
//		sr.setTitle("search title"); 
		sr.setSnippet("search snippet");
		
		SearchResult sr1 = new SearchResult();
//		sr1.setDescription("search description");
//		sr1.setTitle("search title"); 
		sr1.setSnippet("search snippet");
		
		ArrayList<SearchResult> al = new ArrayList<SearchResult>(); 
		al.add(0, sr);
		
		ArrayList<SearchResult> al1 = new ArrayList<SearchResult>();
		al1.add(0, sr1);
		
		ap.setPapers(al);
		ap.setProceedings(al1);
		
		ap.serialize(System.out);
		System.out.println();
		
		// test case 2
		Entity e = new Entity(); 
		MetadataString ms = new MetadataString("ms");
		MetadataParsedURL mpu = new MetadataParsedURL(new ParsedURL(new URL("http://ecologylab.net"))); 
		e.setGist(ms);
//		e.setLocation(mpu); 
		e.serialize(System.out);
		System.out.println();
		
		// test case 3
		AcmProceeding ap1 = new AcmProceeding(); 
		SearchResult sr2 = new SearchResult(); 
		sr2.setHeading("heading");
		sr2.setSnippet("snippet");
		sr2.setLink(new ParsedURL(new URL("http://ecologylab.net")));
		
		ArrayList<SearchResult> al2 = new ArrayList<SearchResult>(); 
		al2.add(0, sr2);
		
		ap1.setPapers(al2);
		ap1.serialize(System.out);
		
	}

	@Test
	public void testConvertClassToByteArray() throws IOException
	{
		byte[] thisByteArray = this.convertClassToByteArray(DBInterface.class);
		for (byte b : thisByteArray)
		{
			// System.out.println(b);
		}

		// convert into string
		String thisString = new String(thisByteArray);
		System.out.println(thisString);

	}

	/**
	 * close db connection
	 * 
	 * @throws SQLException
	 */
	public void closeDBConnection() throws SQLException
	{
		if (thisStatement != null && thisConnection != null)
		{
			thisStatement.close();
			thisConnection.close();

			println("db is disconnected successfully");

		}
		else
			println("DB is already disconnected");

	}


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

}
