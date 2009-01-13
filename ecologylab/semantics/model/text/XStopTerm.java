package ecologylab.semantics.model.text;

public class XStopTerm extends XTerm
{

	public XStopTerm() {
		super("STOP_WORD", 0);
	}
	
	@Override
	public boolean isStopword()
	{
		return true;
	}
	
}
