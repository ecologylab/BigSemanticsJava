package ecologylab.semantics.metametadata;

import java.io.IOException;

import ecologylab.semantics.documentparsers.ParserBase;
import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.serialization.Hint;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.types.scalar.ScalarType;
import ecologylab.serialization.types.scalar.StringType;

@simpl_inherit
@xml_tag("scalar")
public class MetaMetadataScalarField extends MetaMetadataField
{

	/**
	 * The type of the field -- only if it is a scalar.
	 */
	@simpl_scalar
	protected ScalarType	scalarType;

	@simpl_scalar
	protected Hint				hint;

	@simpl_composite
	protected RegexFilter	filter;

	private String				metaMetadataParser;

	public MetaMetadataScalarField()
	{
	}

	public MetaMetadataScalarField(MetaMetadataField mmf)
	{
		this.name = mmf.name;
		this.extendsAttribute = mmf.extendsAttribute;
		this.hide = mmf.hide;
		this.alwaysShow = mmf.alwaysShow;
		this.style = mmf.style;
		this.layer = mmf.layer;
		this.xpath = mmf.xpath;
		this.navigatesTo = mmf.navigatesTo;
		this.shadows = mmf.shadows;
		this.isFacet = mmf.isFacet;
		this.ignoreInTermVector = mmf.ignoreInTermVector;
		this.comment = mmf.comment;
		this.contextNode = mmf.contextNode;
		this.tag = mmf.tag;
		this.kids = mmf.kids;
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
	public String getRegexPattern()
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

	/**
	 * Writes a Scalar Nested attribute to the class file.It is of the form
	 * 
	 * @xml_tag(tagName) @simpl_composite scalarType name;
	 * @param appendable
	 *          The appendable in which this declaration has to be appended.
	 * @throws IOException
	 */
	@Override
	protected void doAppending(Appendable appendable, int pass) throws IOException
	{
		if (scalarType != null)
		{
			String fieldName = getFieldName();
			// fieldName = MetadataCompilerConstants.handleJavaKeyWord(fieldName);
			String fieldTypeName = scalarType.fieldTypeName();
			if (fieldTypeName.equals("int"))
			{
				// HACK FOR METADATAINTEGER
				fieldTypeName = "Integer";
			}

			if (pass == MetadataCompilerUtils.GENERATE_FIELDS_PASS)
			{
				// write the java doc comment for this field
				MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

				// append the Nested field.
				appendLeaf(appendable, "private Metadata", scalarType.fieldTypeName(), fieldName);
			}
			else if (pass == MetadataCompilerUtils.GENERATE_METHODS_PASS)
			{
				// append the getter and setter methods
				appendLazyEvaluationMethod(appendable, fieldName, "Metadata" + fieldTypeName);
				appendGetter(appendable, fieldName, fieldTypeName);
				appendSetter(appendable, fieldName, fieldTypeName);
				appendHWSetter(appendable, fieldName, fieldTypeName);
				appendDirectSetMethod(appendable, fieldName, "Metadata" + fieldTypeName);
				appendDirectHWSetMethod(appendable, fieldName, "Metadata" + fieldTypeName);
				if (fieldTypeName.equals("StringBuilder"))
				{
					// appendAppendMethod(appendable,fieldName,"StringBuilder");
					appendAppendMethod(appendable, fieldName, "String");
					appendHWAppendMethod(appendable, fieldName, "StringBuilder");
					appendHWAppendMethod(appendable, fieldName, "String");
				}
			}
		}
	}

	/**
	 * This method will generate the getter for the field. public String getTitle() { return
	 * title().getValue(); }
	 */
	protected void appendGetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Gets the value of the field " + fieldName;
		// write java doc
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public " + fieldType + " get"
				+ XMLTools.javaNameFromElementName(fieldName, true) + "(){\n");

		// second line
		appendable.append("return " + fieldName + "().getValue();\n}\n");

	}

	/**
	 * This method will generate setter for the field. public void setTitle(String title) {
	 * this.title().setValue(title); }
	 */
	protected void appendSetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Sets the value of the field " + fieldName;
		// write java doc
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void set" + XMLTools.javaNameFromElementName(fieldName, true) + "( "
				+ fieldType + " " + fieldName + " )\n{\n");

		// second line
		appendable.append("this." + fieldName + "().setValue(" + fieldName + ");\n}\n");
	}

	/**
	 * public void hwSetTitle(String title) { this.setTitle(title); rebuildCompositeTermVector(); }
	 */
	protected void appendHWSetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "The heavy weight setter method for field " + fieldName;
		// write java doc
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void hwSet" + XMLTools.javaNameFromElementName(fieldName, true)
				+ "( " + fieldType + " " + fieldName + " )\n{\n");

		// second line
		appendable.append("this." + fieldName + "().setValue(" + fieldName + ");\n");

		// third line
		appendable.append("rebuildCompositeTermVector();\n }");
	}

	/**
	 * Appends scalar field with @simpl_scalar @simpl_hints(Hint.XML_LEAF) annotation
	 * 
	 * @param appendable
	 * @param classNamePrefix
	 * @param className
	 * @param fieldName
	 * @throws IOException
	 */
	protected void appendLeaf(Appendable appendable, String classNamePrefix, String className,
			String fieldName) throws IOException
	{
		if ("int".equals(className))
		{
			// HACK FOR METADATAINTEGER
			className = "Integer";
		}

		StringBuilder annotations = new StringBuilder("@simpl_scalar");
		
		if (getHint() != null)
		{
			annotations.append(String.format(" @simpl_hints(Hint.%s)", getHint()));
		}

		if (filter != null && getMetaMetadataParser().equals(ParserBase.DIRECT_BINDING_PARSER))
		{
			String regex = filter.getJavaRegex();
			String replace = filter.getJavaReplace();
			annotations.append(String.format(" @simpl_filter(regex=\"%s\", replace=\"%s\")", regex,
					replace));
		}

		appendMetalanguageDecl(appendable, getTagDecl() + annotations, classNamePrefix, className,
				fieldName);
	}

	/**
	 * Does this declaration declare a new field, rather than referring to a previously declared
	 * field?
	 * 
	 * @return true if there is a scalar_type attribute declared.
	 */
	protected boolean isNewDeclaration()
	{
		return scalarType != null;
	}

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

	/**
	 * This method will always return false since scalar fields never generate classes.
	 */
	@Override
	public boolean isNewClass()
	{
		return false;
	}

	public static void main(String[] args) throws SIMPLTranslationException
	{
		testSerialization();
		testDeserialization();
	}

	public static void testSerialization() throws SIMPLTranslationException
	{
		MetaMetadataScalarField mmsf = new MetaMetadataScalarField();
		mmsf.scalarType = new StringType();
		mmsf.hint = Hint.XML_LEAF;
		mmsf.filter = new RegexFilter("regex", "replace");
		System.out.println(mmsf.serialize());
	}

	public static void testDeserialization() throws SIMPLTranslationException
	{
		String xml = "<scalar name=\"example\" scalar_type=\"String\" hint=\"XML_ATTRIBUTE\"><filter regex=\".\" replace=\".\" /></scalar>";
		MetaMetadataScalarField m = (MetaMetadataScalarField) MetaMetadataTranslationScope.get()
				.deserializeCharSequence(xml);
		System.out.println(m);
	}

	@Override
	protected boolean checkForErrors()
	{
		if (name == null)
			return false;
		return true;
	}

	@Override
	protected String getTypeName()
	{
		return scalarType.fieldTypeName();
	}

	@Override
	protected String getSuperTypeName()
	{
		return null;
	}

}
