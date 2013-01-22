/**
 * 
 */
package ecologylab.semantics.model.text.concept;

import ecologylab.semantics.model.text.Term;

/**
 * @author quyin
 *
 */
public class TermEx extends Term
{
	private String posTag;
	public String getPosTag()
	{
		return posTag;
	}

	/**
	 * @param stem
	 * @param idf
	 */
	public TermEx(String stem, double idf)
	{
		super(stem, idf);
		// TODO Auto-generated constructor stub
	}

}
