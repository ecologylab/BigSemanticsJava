package ecologylab.bigsemantics.documentparsers;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import ecologylab.generic.ResourcePool;

/**
 * A pool for reusing XML Transformers.
 * 
 * @author quyin
 *
 */
public class XmlTransformerPool extends ResourcePool<Transformer>
{

	public static final int						INIT_POOL_SIZE	= 4;

	private static TransformerFactory	factory					= TransformerFactory.newInstance();

	protected XmlTransformerPool()
	{
		super(INIT_POOL_SIZE, INIT_POOL_SIZE);
	}

	@Override
	protected Transformer generateNewResource()
	{
		try
		{
			return factory.newTransformer();
		}
		catch (TransformerConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException("Cannot create new XML transformers!");
	}

	@Override
	protected void clean(Transformer objectToClean)
	{
		objectToClean.reset();
	}

	private static XmlTransformerPool	thePool;

	static
	{
		thePool = new XmlTransformerPool();
	}

	public static XmlTransformerPool get()
	{
		return thePool;
	}

}
