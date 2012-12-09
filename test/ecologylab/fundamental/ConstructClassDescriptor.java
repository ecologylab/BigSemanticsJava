package ecologylab.fundamental;

import static org.junit.Assert.*;

import org.junit.Test;

import ecologylab.fundamental.simplescalar.*;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.types.scalar.*;

public class ConstructClassDescriptor {

	
	// Todo: Stealthily swap this with a factory method.
	private ClassDescriptor<?> ConstructClassDescriptor(Class<?> lass)
	{
		return ClassDescriptor.getClassDescriptor(lass);
	}
	
	private void testSimpleScalar(Class<?> lass, Class<?> expectedType)
	{
		ClassDescriptor<?> cd = ConstructClassDescriptor(lass);
		
		// Get the one field that is in the simple scalar class
		assertEquals(1, cd.allFieldDescriptors().size());	
		FieldDescriptor fd = cd.allFieldDescriptors().get(0);
		assertEquals(lass.getSimpleName().toLowerCase(), fd.getName());
		assertEquals(FieldType.SCALAR, fd.getType());
		// TODO: BUG. //assertEquals(1, fd.getMetaInformation().size());
		assertEquals(expectedType, fd.getScalarType().getClass());
		
		// TODO: Roundtrip the class descriptor. 
		
	}
	
	// TODO: Roundtrip some range of values
	
	@Test
	public void forSimpleBoolean() {
		testSimpleScalar(SimpleBoolean.class, ReferenceBooleanType.class);
	}

	@Test
	public void forSimpleByte() {
		testSimpleScalar(SimpleByte.class, ByteType.class);
	}
	
	@Test
	public void forSimpleChar(){
		testSimpleScalar(SimpleChar.class, CharType.class);
	}
	
	@Test
	public void forSimpleDate(){
		testSimpleScalar(SimpleDate.class, DateType.class);
	}
	
	@Test
	public void forSimpleDouble(){
		testSimpleScalar(SimpleDouble.class, ReferenceDoubleType.class);
	}
	
	@Test
	public void forSimpleFloat(){
		testSimpleScalar(SimpleFloat.class, ReferenceFloatType.class);
	}
	
	@Test
	public void forSimpleInteger()
	{
		testSimpleScalar(SimpleInteger.class, ReferenceIntegerType.class);
	}
	
	@Test
	public void forSimpleJavaURL(){
		testSimpleScalar(SimpleJavaURL.class, URLType.class);
	}
	
	@Test
	public void forSimpleLong(){
		testSimpleScalar(SimpleLong.class, ReferenceLongType.class);
	}
	
	@Test
	public void forSimpleParsedURL()
	{
		testSimpleScalar(SimpleParsedURL.class, ParsedURLType.class);
	}

	@Test
	public void forSimplePattern()
	{
		testSimpleScalar(SimplePattern.class, PatternType.class);
	}
	
	@Test
	public void forSimplePrimBoolean()
	{
		testSimpleScalar(Simpleprimboolean.class, BooleanType.class);
	}
	
	@Test
	public void forSimplePrimByte()
	{
		testSimpleScalar(Simpleprimbyte.class, ByteType.class);
	}
	
	@Test
	public void forSimplePrimChar()
	{
		testSimpleScalar(Simpleprimchar.class, CharType.class);
	}
	
	@Test
	public void forSimplePrimDouble()
	 {
		 testSimpleScalar(Simpleprimdouble.class, DoubleType.class);
	 }
	
	@Test
	public void forSimplePrimFloat()
	{
		testSimpleScalar(Simpleprimfloat.class, FloatType.class);
	}
	
	@Test
	public void forSimplePrimInt()
	{
		testSimpleScalar(Simpleprimint.class, IntType.class);
	}
	
	@Test
	public void forSimplePrimLong()
	{
		testSimpleScalar(Simpleprimlong.class, LongType.class);
	}
	
	@Test
	public void forSimplePrimShort()
	{
		testSimpleScalar(Simpleprimshort.class, ShortType.class);
	}
	
	// TODO.
	@Test
	public void forSimpleShort()
	{
		testSimpleScalar(SimpleShort.class, null);
	}
	
	@Test
	public void forSimpleString()
	{
		testSimpleScalar(SimpleString.class, StringType.class);
	}
	
	@Test
	public void forSimpleStringBuilder()
	{
		testSimpleScalar(SimpleStringBuilder.class, StringBuilderType.class);
	}
	
	@Test
	public void forSimpleUUID()
	{
		testSimpleScalar(SimpleUUID.class, UUIDType.class);
	}
	
}
