package ecologylab.bigsemantics.model.text;

public class StopWordTerm extends Term
{

	public StopWordTerm() {
		super("STOP_WORD", 0);
	}
	
	@Override
	public boolean isStopword()
	{
		return true;
	}
	
}
