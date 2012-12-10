package ecologylab.semantics.testcases;

import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class TestTranslationScopeDeSerialization extends TestTranslationScope
{

	static DeserializationHookStrategy<ElementState, FieldDescriptor>	emptyStrategy;

	static
	{
		emptyStrategy = new DeserializationHookStrategy<ElementState, FieldDescriptor>()
		{
			@Override
			public void deserializationPreHook(
					ElementState e, FieldDescriptor fd)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void deserializationInHook(ElementState o, FieldDescriptor fd)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void deserializationPostHook(
					ElementState e, FieldDescriptor fd)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public ElementState changeObjectIfNecessary(ElementState o, FieldDescriptor fd)
			{
				return o;
			}
		};
	}

	/**
	 * @param args
	 * @throws SIMPLTranslationException
	 */
	public static void main(String[] args) throws SIMPLTranslationException
	{
		SimplTypesScope ts = get();

		StringBuilder sb = new StringBuilder();
		SimplTypesScope.serialize(ts, sb, StringFormat.XML);

		String xml = sb.toString();
		System.out.println();
		System.out.println(xml);
		System.out.println();
		
		xml = xml.replaceAll("TestDocument", "NewTestDocument");

		SimplTypesScope tsts = SimplTypesScope.get(
				"tscope_bootstrap",
				SimplTypesScope.class,
				ClassDescriptor.class,
				FieldDescriptor.class);
		SimplTypesScope newTs = (SimplTypesScope) tsts.deserialize(xml, emptyStrategy, StringFormat.XML);
		for (Class clazz : newTs.getAllClasses())
		{
			System.out.println(clazz);
		}
	}

}
