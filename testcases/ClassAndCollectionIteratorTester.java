package testcases;

import ecologylab.generic.ClassAndCollectionIterator;
import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.OneLevelNestingIterator;
import ecologylab.semantics.library.scholarlyPublication.AcmPortal;
import ecologylab.semantics.library.scholarlyPublication.Author;
import ecologylab.semantics.library.scholarlyPublication.Reference;
import ecologylab.semantics.library.scholarlyPublication.Source;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.xml.FieldAccessor;

public class ClassAndCollectionIteratorTester
	extends Debug
{

	static AcmPortal acmPortal = new AcmPortal();
	HashMapArrayList<String, Author> authors = new HashMapArrayList<String, Author> ();
	Source source = new Source();
	Reference ref = new Reference();
	public ClassAndCollectionIteratorTester()
	{
		Author author1 = new Author();
		author1.setName("Sashikanth");
		author1.setAffiliation("Texas A&M");

		Author author2 = new Author();
		author2.setName("Damaraju");
		author2.setAffiliation("UT Austin");
		
		authors.put("Sashikanth", author1);
		authors.put("Damaraju", author2);

		acmPortal.setAuthors(authors);
	}
	
	public static void main(String args[])
	{
		new ClassAndCollectionIteratorTester();
		AcmPortal temp = acmPortal;
		ClassAndCollectionIterator<FieldAccessor, MetadataBase> iterator = 
			new ClassAndCollectionIterator<FieldAccessor, MetadataBase>(temp);
		OneLevelNestingIterator<FieldAccessor, Metadata> oneLevelIterator = 
			new OneLevelNestingIterator<FieldAccessor, Metadata>(temp, null);
		int i = 0;
		while(iterator.hasNext())
		{
			MetadataBase metadata = iterator.next();
			println("Metadata (" + i++ + "): " + metadata);
		}
		i = 0;
		while(oneLevelIterator.hasNext())
		{
			FieldAccessor fa = oneLevelIterator.next();
			println("One Level Metadata(" + i++ + "): " + fa.getFieldName());
		}
	}
}
