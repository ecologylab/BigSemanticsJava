package testcases;

import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.StringFormat;
import ecologylab.serialization.TranslationScope;

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
			public void deserializationPostHook(
					ElementState e, FieldDescriptor fd)
			{
				// TODO Auto-generated method stub

			}
		};
	}

	/**
	 * @param args
	 * @throws SIMPLTranslationException
	 */
	public static void main(String[] args) throws SIMPLTranslationException
	{
		TranslationScope ts = get();

		StringBuilder sb = new StringBuilder();
		ClassDescriptor.serialize(ts, sb, StringFormat.XML);

		String xml = sb.toString();
		System.out.println();
		System.out.println(xml);
		System.out.println();
		
		xml = xml.replaceAll("TestDocument", "NewTestDocument");

		TranslationScope tsts = TranslationScope.get(
				"tscope_bootstrap",
				TranslationScope.class,
				ClassDescriptor.class,
				FieldDescriptor.class);
		TranslationScope newTs = (TranslationScope) tsts.deserialize(xml, emptyStrategy, StringFormat.XML);
		for (Class clazz : newTs.getAllClasses())
		{
			System.out.println(clazz);
		}
	}

}
