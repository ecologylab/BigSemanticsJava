package ecologylab.semantics.metametadata;

import java.io.IOException;

import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.xml.XMLTools;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;
import ecologylab.xml.types.scalar.ScalarType;

@xml_inherit
@xml_tag("mm_scalar_field")
public class MetaMetadataScalarField extends MetaMetadataField
{

	/**
	 * The type of the field -- only if it is a scalar.
	 */
	@xml_attribute
	protected ScalarType	scalarType;

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
		this.stringPrefix = mmf.stringPrefix;
		this.isFacet = mmf.isFacet;
		this.ignoreInTermVector = mmf.ignoreInTermVector;
		this.comment = mmf.comment;
		this.dontCompile = mmf.dontCompile;
		this.key = mmf.key;
		this.textRegex = mmf.textRegex;
		this.matchReplacement = mmf.matchReplacement;
		this.contextNode = mmf.contextNode;
		this.tag = mmf.tag;
		this.ignoreExtractionError = mmf.ignoreExtractionError;
		this.kids = mmf.kids;
	}

	/**
	 * @return the scalarType
	 */
	public ScalarType getScalarType()
	{
		return scalarType;
	}
	
	@Override
	protected void doAppending(Appendable appendable, int pass) throws IOException
	{
		appendScalarLeaf(appendable, pass);
	}

	/**
	 * Writes a Scalar Nested attribute to the class file.It is of the form
	 * 
	 * @xml_tag(tagName) @xml_nested scalarType name;
	 * @param appendable
	 *          The appendable in which this declaration has to be appended.
	 * @throws IOException
	 */
	protected void appendScalarLeaf(Appendable appendable, int pass) throws IOException
	{
		String fieldName = XMLTools.fieldNameFromElementName(getName());
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
