package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.metadata.Metadata;

public class AuthorsMap extends Metadata
{
	private	HashMapArrayList<String, Author>		authorMap 		= new HashMapArrayList<String, Author>();

	public Author get(String key)
	{
		return authorMap.get(key);
	}
	
	public Author get(int index)
	{
		if(index < authorMap.size())
		{
			return authorMap.get(index);
		}
		return null;
	}
	
	public void add(String key)
	{
		Author author = new Author(key);
		this.addAuthor(author);
	}
	public void addAuthor(Author author)
	{
		 authorMap.put(author.name, author);
	}
	/**
	 * @return the author
	 */
	public HashMapArrayList<String, Author> getAuthor()
	{
		return authorMap;
	}

	/**
	 * @param authorMap the author to set
	 */
	public void setAuthor(HashMapArrayList<String, Author> authorMap)
	{
		this.authorMap = authorMap;
	}
	
}
