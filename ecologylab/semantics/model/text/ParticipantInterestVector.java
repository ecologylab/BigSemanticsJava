package ecologylab.semantics.model.text;

public class ParticipantInterestVector extends XTermVector {

	public ParticipantInterestVector() {
		// TODO Auto-generated constructor stub
	}

	public void expressInterest(XTermVector tv, int delta) {
		this.add(delta, tv);
	}

}
