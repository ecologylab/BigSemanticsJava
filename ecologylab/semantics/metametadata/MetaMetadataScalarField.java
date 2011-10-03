package ecologylab.semantics.metametadata;

import java.io.IOException;
import java.util.regex.Pattern;

import ecologylab.semantics.documentparsers.ParserBase;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.semantics.metadata.scalar.types.MetadataStringScalarType;
import ecologylab.semantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldTypes;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.StringFormat;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_composite_as_scalar;
import ecologylab.serialization.annotations.simpl_filter;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.types.ScalarType;

@simpl_inherit
@simpl_tag("scalar")
public class MetaMetadataScalarField extends MetaMetadataField
{

	/**
	 * The type of the field -- only if it is a scalar.
	 */
	@simpl_scalar
	protected MetadataScalarType	scalarType;

	@simpl_scalar
	protected Hint								hint;

	@simpl_composite
	protected RegexFilter					filter;

	@simpl_scalar
	@simpl_tag("as_composite_scalar")
	private boolean								compositeScalar;

	/**
	 * for caching getMetaMetadataParser().
	 */
	private String								metaMetadataParser;

	/**
	 * for caching getTypeNameInJava().
	 */
	private String								typeNameInJava		= null;

	/**
	 * for caching getScalarTypeInJava().
	 */
	private String								scalarTypeInJava	= null;

	public MetaMetadataScalarField()
	{
		
	}

	@Override
	protected Object clone()
	{
		MetaMetadataScalarField cloned = new MetaMetadataScalarField();
		cloned.setCloned(true);
		cloned.inheritAttributes(this);
		cloned.copyClonedFieldsFrom(this);
		cloned.clonedFrom = this;
		return cloned;
	}

	/**
	 * @return the scalarType
	 */
	public ScalarType getScalarType()
	{
		return scalarType;
	}

	public Hint getHint()
	{
		return hint;
	}

	public void setHint(Hint hint)
	{
		this.hint = hint;
	}

	/**
	 * @return the regex pattern
	 */
	public Pattern getRegexPattern()
	{
		if (filter != null)
			return filter.getRegex();
		return null;
	}

	/**
	 * @return the replacement string
	 */
	public String getRegexReplacement()
	{
		if (filter != null)
			return filter.getReplace();
		return null;
	}
	
	public boolean isCompositeScalar()
	{
		return compositeScalar;
	}

	@Override
	protected String getTypeName()
	{
		throw new RuntimeException("no mmd name for scalar fields!");
	}

	/**
	 * get the parser attribute of the meta_metadata this field resides in.
	 * 
	 * @return
	 */
	private String getMetaMetadataParser()
	{
		if (metaMetadataParser == null)
		{
			synchronized (this)
			{
				MetaMetadataField field = this;
				while (!(field instanceof MetaMetadata))
				{
					field = (MetaMetadataField) field.parent();
				}
				MetaMetadata mmd = (MetaMetadata) field;
				metaMetadataParser = mmd.getParser();
	
				if (metaMetadataParser == null && mmd.getExtendsAttribute() != null)
				{
					// might be inherited from base class
					// TODO ... what to do ...
				}
	
				if (metaMetadataParser == null)
				{
					// if still null we just return an empty string to prevent null pointer exception
					metaMetadataParser = "";
				}
			}
		}
	
		return metaMetadataParser;
	}

	@Override
	public String getAdditionalAnnotationsInJava(MmdCompilerService compiler) throws IOException
	{
		StringBuilder appendable = StringBuilderUtils.acquire();
		
		// @simpl_composite_as_scalar
		compiler.appendAnnotation(appendable, " ", simpl_composite_as_scalar.class);
		
		// @filter
		if (filter != null && getMetaMetadataParser().equals(ParserBase.DIRECT_BINDING_PARSER))
		{
			String regex = filter.getJavaRegex();
			compiler.appendAnnotation(appendable, " ", simpl_filter.class);
			appendable.append("(regex=\"").append(regex).append("\"");
			String replace = filter.getJavaReplace();
			if (replace != null)
			{
				appendable.append(", replace=\"").append(replace).append("\"");
			}
			appendable.append(")");
		}
		
		String annotations = appendable.toString();
		StringBuilderUtils.release(appendable);
		return annotations;
	}

	@Override
	public String getTypeNameInJava()
	{
		String rst = typeNameInJava;
		if (rst == null)
		{
			rst = "Metadata" + getScalarTypeInJava();
			typeNameInJava = rst;
		}
		return typeNameInJava;
	}
	
	private String getScalarTypeInJava()
	{
		String rst = scalarTypeInJava;
		if (rst == null)
		{
			if (scalarType == null)
				throw new MetaMetadataException("Scalar type not defined: " + this);
			rst = scalarType.fieldTypeName();
			if (rst.equals("int"))
			{
				// HACK FOR METADATAINTEGER
				rst = "Integer";
			}
			if (rst.equals("float"))
			{
				rst = "Float";
			}
			scalarTypeInJava = rst;
		}
		return scalarTypeInJava;
	}

//	@Override
//	public void compileToMethods(Appendable appendable) throws IOException
//	{
//		super.compileToMethods(appendable);
//		
//		compileIsNullMethod(appendable);
//		compileHWSetter(appendable);
//		compileDirectSetter(appendable);
//		compileDirectHWSetter(appendable);
//		if (getTypeNameInJava().equals("StringBuilder"))
//		{
////			compileAppendMethod(appendable, "StringBuilder");
//			compileAppendMethod(appendable, "String");
//			compileHWAppendMethod(appendable, "StringBuilder");
//			compileHWAppendMethod(appendable, "String");
//		}
//	}
//
//	/**
//	 * generate a specialized getter for scalar field: e.g. public String getTitle()
//	 * { return title().getValue(); }
//	 */
//	@Override
//	protected void compileGetter(Appendable appendable) throws IOException
//	{
//		String fieldName = getFieldNameInJava(false);
//		String typeName = getScalarTypeInJava();
//		
//		// write java doc
//		String comment = "Gets the value of the field " + fieldName;
//		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);
//	
//		// first line
//		appendable.append("public ").append(typeName).append(" get")
//				.append(getFieldNameInJava(true)).append("()\n{\n");
//	
//		// second line
//		appendable.append("\treturn this.").append(fieldName).append("().getValue();\n}\n");
//	}
//
//	/**
//	 * generate a specialized setter for scalar field: e.g. public void setTitle(String title)
//	 * { this.title().setValue(title); }
//	 */
//	@Override
//	protected void compileSetter(Appendable appendable) throws IOException
//	{
//		String fieldName = getFieldNameInJava(false);
//		String typeName = getScalarTypeInJava();
//		
//		// write java doc
//		String comment = "Sets the value of the field " + fieldName;
//		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);
//	
//		// first line
//		appendable.append("public void set").append(getFieldNameInJava(true))
//				.append("(").append(typeName).append(" ").append(fieldName).append(")\n{\n");
//	
//		// second line
//		appendable.append("\tthis.").append(fieldName).append("().setValue(").append(fieldName).append(");\n}\n");
//	}
//
//	/**
//	 * <code>
//	 * public boolean isNullTitle() { return title == null || title.getValue() == null; }
//	 * </code>
//	 * 
//	 * @param appendable
//	 * @throws IOException
//	 */
//	private void compileIsNullMethod(Appendable appendable) throws IOException
//	{
//		String fieldName = getFieldNameInJava(false);
//		
//		// write java doc
//		String comment = "Test to see if the value of the field is null, or if the field itself is null: " + fieldName;
//		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);
//	
//		// first line
//		appendable.append("public boolean isNull").append(getFieldNameInJava(true)).append("()\n{\n");
//	
//		// second line
//		appendable.append("\treturn ").append(fieldName).append(" == null || ")
//				.append(fieldName).append(".getValue() == null;\n}\n");
//	}
//
//	/**
//	 * <code>
//	 * public void hwSetTitle(String title) { this.setTitle(title); rebuildCompositeTermVector(); }
//	 * </code>
//	 * 
//	 * @param appendable
//	 * @throws IOException
//	 */
//	private void compileHWSetter(Appendable appendable) throws IOException
//	{
//		String fieldName = getFieldNameInJava(false);
//		String typeName = getScalarTypeInJava();
//		
//		// write java doc
//		String comment = "The heavy weight setter method for field " + fieldName;
//		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);
//	
//		// first line
//		appendable.append("public void hwSet").append(getFieldNameInJava(true))
//				.append("(").append(typeName).append(" ").append(fieldName).append(")\n{\n");
//	
//		// second line
//		appendable.append("\tthis.").append(fieldName).append("().setValue(").append(fieldName).append(");\n");
//	
//		// third line
//		appendable.append("\trebuildCompositeTermVector();\n}\n");
//	}
//
//	/**
//	 * <code>
//	 * public void setQueryMetadata(MetadataString query) { this.query = query; }
//	 * </code>
//	 * 
//	 * @param appendable
//	 * @throws IOException
//	 */
//	private void compileDirectSetter(Appendable appendable) throws IOException
//	{
//		String fieldName = getFieldNameInJava(false);
//		String typeName = getTypeNameInJava();
//		
//		// write the java doc comment
//		String comment = " Sets the " + fieldName + " directly.";
//		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);
//	
//		// first line
//		appendable.append("public void set").append(getFieldNameInJava(true)).append("Metadata(")
//				.append(typeName).append(" ").append(fieldName).append(")\n{\n");
//	
//		// second line
//		appendable.append("\tthis.").append(fieldName).append(" = ").append(fieldName).append(";\n}\n");
//	}
//
//	/**
//	 * <code>
//	 * public void hwSetQueryMetadata(MetadataString query)
//	 * {
//	 * 	if (this.query != null && this.query.getValue() != null && hasTermVector())
//	 *  	termVector().remove(this.query.termVector());
//	 *  this.query = query;
//	 *  rebuildCompositeTermVector();
//	 * }
//	 * </code>
//	 * 
//	 * @param appendable
//	 * @throws IOException
//	 */
//	private void compileDirectHWSetter(Appendable appendable) throws IOException
//	{
//		String fieldName = getFieldNameInJava(false);
//		String typeName = getTypeNameInJava();
//	
//		// write Java doc
//		String comment = "Heavy Weight Direct setter method for " + fieldName;
//		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);
//	
//		// first line
//		appendable.append("public void hwSet").append(getFieldNameInJava(true)).append("Metadata(")
//				.append(typeName).append(" ").append(fieldName).append(")\n{\n");
//	
//		// second line
//		appendable.append("\tif (this.").append(fieldName).append(" != null && this.").append(fieldName)
//				.append(".getValue() != null && hasTermVector())\n");
//	
//		// third line
//		appendable.append("\t\ttermVector().remove(this.").append(fieldName).append(".termVector());\n");
//	
//		// fourth line
//		appendable.append("\tthis.").append(fieldName).append(" = ").append(fieldName).append(";\n");
//	
//		// last line
//		appendable.append("\trebuildCompositeTermVector();\n}\n");
//	}
//
//	/**
//	 * <code>
//	 * public void appendAnchorText(String anchorText) { this.anchorText().setValue(anchorText); }
//	 * </code>
//	 * 
//	 * @param appendable
//	 * @param typeName
//	 * @throws IOException
//	 */
//	private void compileAppendMethod(Appendable appendable, String typeName)
//			throws IOException
//	{
//		String fieldName = getFieldNameInJava(false);
//		
//		// write Java doc
//		String comment = "Appends the value to the field " + fieldName;
//		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);
//
//		// first line
//		appendable.append("public void append").append(getFieldNameInJava(true))
//				.append("(").append(typeName).append(" ").append(fieldName).append(")\n{\n");
//
//		// second line
//		appendable.append("\tthis.").append(fieldName).append("().setValue(").append(fieldName).append(");\n}\n");
//	}
//
//	/**
//	 * <code>
//	 * public void hwAppendAnchorText(String anchorText) { this.anchorText().setValue(anchorText); rebuildCompositeTermVector(); }
//	 * </code>
//	 * 
//	 * @param appendable
//	 * @param typeName
//	 * @throws IOException
//	 */
//	private void compileHWAppendMethod(Appendable appendable, String typeName) throws IOException
//	{
//		String fieldName = getFieldNameInJava(false);
//		
//		// write java doc
//		String comment = "The heavy weight Append method for field " + fieldName;
//		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);
//
//		// first line
//		appendable.append("public void hwAppend").append(getFieldNameInJava(true))
//				.append("(").append(typeName).append(" ").append(fieldName).append(")\n{\n");
//
//		// second line
//		appendable.append("\tthis.").append(fieldName).append("().setValue(").append(fieldName).append(");\n");
//
//		// third line
//		appendable.append("\trebuildCompositeTermVector();\n}\n");
//	}

	public static void main(String[] args) throws SIMPLTranslationException
	{
		testSerialization();
		testDeserialization();
	}

	public static void testSerialization() throws SIMPLTranslationException
	{
		MetaMetadataScalarField mmsf = new MetaMetadataScalarField();
		mmsf.scalarType = new MetadataStringScalarType();
		mmsf.hint = Hint.XML_LEAF;
		mmsf.filter = new RegexFilter("regex", "replace");
		System.out.println(ClassDescriptor.serialize(mmsf, StringFormat.XML));
	}

	public static void testDeserialization() throws SIMPLTranslationException
	{
		String xml = "<scalar name=\"example\" scalar_type=\"String\" hint=\"XML_ATTRIBUTE\"><filter regex=\".\" replace=\".\" /></scalar>";
		MetaMetadataScalarField m = (MetaMetadataScalarField) MetaMetadataTranslationScope.get()
				.deserialize(xml, StringFormat.XML);
		System.out.println(m);
	}

	@Override
	public MetadataFieldDescriptor findOrGenerateMetadataFieldDescriptor(TranslationScope tscope, MetadataClassDescriptor contextCd)
	{
		MetadataFieldDescriptor fd = this.metadataFieldDescriptor;
		if (fd == null)
		{
			String tagName = this.resolveTag();
			String fieldName = this.getFieldNameInJava(false);
			ScalarType scalarType2 = this.getScalarType();
			if (scalarType2 == null)
				error("scalar_type not specified or defined: " + this);
			else
			{
				String javaTypeName = scalarType2.getJavaTypeName();
				fd = new MetadataFieldDescriptor(
						this,
						tagName,
						this.getComment(),
						FieldTypes.SCALAR,
						null,
						contextCd,
						fieldName,
						scalarType2,
						this.getHint(),
						javaTypeName);
				this.metadataFieldDescriptor = fd;
			}
		}
		return fd;
	}

}
