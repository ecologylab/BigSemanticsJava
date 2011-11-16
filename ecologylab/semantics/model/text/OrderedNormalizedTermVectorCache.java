package ecologylab.semantics.model.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import ecologylab.generic.Debug;
/***
 * 
 * @author rhema
 *
 *  Stores terms ordered by tfidf score and will cache
 *  normalized sets of arbitrary maximum size.
 */

public class OrderedNormalizedTermVectorCache extends Debug
{
	protected Vector<TermWithScore> orderedTerms;
	protected HashMap<Integer, Vector<TermWithScore>> cachedNormalizedVectors; 
	
	public OrderedNormalizedTermVectorCache()
	{
		orderedTerms = new Vector<TermWithScore>();
		cachedNormalizedVectors = new HashMap<Integer, Vector<TermWithScore>>();
	}
	
	public OrderedNormalizedTermVectorCache(TermVector termVector)
	{
		this();
		//debug("A metadata has been created!");
		ArrayList<Term> blah = termVector.tfIdfTrim(.00001, new TermVector());
		for(Term t: blah)
		{
			if(t != null)
			{
			   //debug(t.getWord() + "  "+termVector.tfIdf(t));
			   orderedTerms.add(new TermWithScore(t, termVector.tfIdf(t)));
			}
			else
			{
				//debug("Why is this null?");
			}
		}
	}
	

  public Vector<TermWithScore> getOrderedTerms()
  {
  	return orderedTerms;
  }
  
  public static int NO_MAX = -1;
  public Vector<TermWithScore> getNormalizedOrderedTerms(int max)
  {
  	if(cachedNormalizedVectors.containsKey(max))
  		return cachedNormalizedVectors.get(max);
  	Vector<TermWithScore> returnVector = new Vector<TermWithScore>();
  	if(max == NO_MAX)
  		max = orderedTerms.size();
  	double totalScore = 0;
  	//I'm not super confident about how we are normalizing this here...
  	//We are not going to take this and multiply because we want to save
  	//sparse terms and dot product is ruthless for our context.
  	int foundSoFar = 0;
  	for(TermWithScore nt : orderedTerms)
  	{
  		foundSoFar += 1;
  		totalScore += nt.getScore();
  		if(foundSoFar >= max)
  			break;
  	}
  	
  	foundSoFar = 0;
  	for(TermWithScore nt : orderedTerms)
  	{
  		foundSoFar += 1;
  		returnVector.add(new TermWithScore(nt,nt.getScore()/totalScore));
  		if(foundSoFar >= max)
  			break;
  	}
  	cachedNormalizedVectors.put(max, returnVector);
  	return returnVector;
  }
}
