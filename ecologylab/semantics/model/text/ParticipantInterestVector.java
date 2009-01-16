package ecologylab.semantics.model.text;

import ecologylab.generic.VectorType;

public class ParticipantInterestVector extends TermVector
{

	public ParticipantInterestVector()
	{
		// TODO Auto-generated constructor stub
	}

	public void expressInterest(VectorType<Term> tv, int delta)
	{
		this.add(delta, tv);
	}

}
