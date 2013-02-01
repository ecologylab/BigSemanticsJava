package ecologylab.semantics.model.text;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
	protected HashMap<String, Vector<TermWithScore>> cachedNormalizedVectors; 
	
	public OrderedNormalizedTermVectorCache()
	{
		orderedTerms = new Vector<TermWithScore>();
		cachedNormalizedVectors = new HashMap<String, Vector<TermWithScore>>();
	}
	
	public OrderedNormalizedTermVectorCache(CompositeTermVector compositeTermVector)
	{
		this();
		//debug("A metadata has been created!");
		Map<Term, Double> blah = compositeTermVector.map();//.tfIdfTrim(.00001, new TermVector());
		for(Term t: blah.keySet())
		{
			if(t != null)
			{
			   //debug(t.getWord() + "  "+termVector.tfIdf(t));
			   orderedTerms.add(new TermWithScore(t, blah.get(t)));//compositeTermVector.tfIdf(t)));
			}
			else
			{
				//debug("Why is this null?");
			}
		}
		Collections.sort(orderedTerms);
	}
	

  public Vector<TermWithScore> getOrderedTerms()
  {
  	return orderedTerms;
  }
  
  public static int NO_MAX = -1;
  public static double TF_ONLY = .5;
  
  public Vector<TermWithScore> getNormalizedOrderedTerms(int max)
  {
    return getNormalizedOrderedTerms(max, TF_ONLY);
  }
  
  	
  
  public Vector<TermWithScore> getNormalizedOrderedTerms(int max, double dfBonus)
  {
  	
  	DecimalFormat twoDForm = new DecimalFormat("#.#");
  	dfBonus = Double.valueOf(twoDForm.format(dfBonus));
  	String key = max+":"+String.format("%.2g%n", dfBonus);
  	
  	if(cachedNormalizedVectors.containsKey(key))
  		return cachedNormalizedVectors.get(key);
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
  		returnVector.add(new TermWithScore(nt,nt.getScore()/totalScore, dfBonus));
  		if(foundSoFar >= max)
  			break;
  	}
  	cachedNormalizedVectors.put(key, returnVector);
  	return returnVector;
  }
}
