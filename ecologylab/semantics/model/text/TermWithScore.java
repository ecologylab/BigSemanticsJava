package ecologylab.semantics.model.text;

import ecologylab.serialization.annotations.simpl_inherit;

/***
 * 
 * @author rhema
 *  Extends Term allowing one to detach the term from a vector
 *  and change it's score arbitrarily.  This is useful
 *  in the context of normalizing terms.
 */
@simpl_inherit
public class TermWithScore extends Term
{
	private double score;
	public double getScore()
	{
		return score;
	}

	public void setScore(double score)
	{
		this.score = score;
	}

	public TermWithScore()
	{
		super();
	}
	
	public TermWithScore(Term t, double score)
	{
		super();
		this.setWord(t.getWord());
		this.score = score;
	}
	
	@Override
	public String toString()
	{
		return this.getWord() + ":" + score;
	}
	
	@Override
	public int compareTo(Term o) 
	{
		TermWithScore convertedTerm = (TermWithScore) o;
		double difference = this.getScore() - convertedTerm.getScore();
			
		if (difference > 0)
			return -1;
		else
			return (difference == 0) ? this.getWord().compareTo(convertedTerm.getWord()) : 1;
	}

	
}
