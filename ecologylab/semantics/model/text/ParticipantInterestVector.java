package ecologylab.semantics.model.text;

import ecologylab.generic.VectorType;

public class ParticipantInterestVector extends XTermVector
{

	public ParticipantInterestVector()
	{
		// TODO Auto-generated constructor stub
	}

	public void expressInterest(VectorType<XTerm> tv, int delta)
	{
		this.add(delta, tv);
	}

}
