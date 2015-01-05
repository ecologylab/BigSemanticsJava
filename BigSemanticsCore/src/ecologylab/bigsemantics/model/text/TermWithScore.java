package ecologylab.bigsemantics.model.text;

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
	private double tf;
	private double idf;
	
	public double getIdf()
	{
		return idf;
	}
	
	public double getTf()
	{
		return tf;
	}
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
	
	
	public static int TF_SCORE_TYPE = 0;
	public static int IDF_SCORE_TYPE = 1;
	public static int TF_IDF_SCORE_TYPE = 2;
	public static int TF_IDF_DIST_SCORE_TYPE = 3;
	public static int IF_IDF_CLOSENESS_SCORE_TYPE = 4;
	
	
  double getScore(int scoreType, Term t, double tf, double farness)//dist is between 0 and 1 with 0 as close and 1 as far
  {
  	double idf = TermDictionary.getTermForWord(t.getWord()).idf();
  	if(idf == 0)
  		idf = TermDictionary.averageIDF;

  	double returnValue = tf*Math.pow(idf, -20*farness + 10);
  	if(returnValue == Double.NaN)
  	{
  		debug(" Nan value for idf "+idf+" and tf "+tf);
  		return 0;
 		}
  	return returnValue;
  }
	
  public static int SCORE_TYPE = 0;
	
	public TermWithScore(Term t, double score)
	{
		this(t,score,OrderedNormalizedTermVectorCache.TF_ONLY);
	}
	
	public TermWithScore(Term t, double score, double dfBonus)
	{
		this.setWord(t.getWord());
		tf = score;
		idf = TermDictionary.getTermForWord(t.getWord()).idf();
		this.score = getScore(SCORE_TYPE, t, score, dfBonus);
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
