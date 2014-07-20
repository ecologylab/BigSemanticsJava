package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ecologylab.bigsemantics.documentparsers.ParserBase;
import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataStringScalarType;
import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.MetaInformation;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite_as_scalar;
import ecologylab.serialization.annotations.simpl_filter;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.formatenums.StringFormat;
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

	@simpl_collection("value")
	protected List<MetaMetadataValueField> concatenateValues;

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
	
	
	public List<MetaMetadataValueField> getConcatenateValues()
	{
		return concatenateValues;
	}
	
	public void setConcatenateValues(List<MetaMetadataValueField> list)
	{
		this.concatenateValues = list;
	}
	
	public Boolean hasConcatenateValues()
	{
		return (this.concatenateValues != null  && this.concatenateValues.size() > 0);
	}
	
	public Boolean hasValueDependencies()
	{
		Boolean hasDependencies = (this.getValueDependencies().size() > 0);
		return hasDependencies;
	}
	
	private List<MetaMetadataValueField> cachedValueDependencies = null;
	
	public List<MetaMetadataValueField> getValueDependencies()
	{
		if(cachedValueDependencies == null)
		{
			List<MetaMetadataValueField> ourDependencies = new ArrayList<MetaMetadataValueField>();
			
			//Todo: refactor to Google Guava predicates
			if(hasConcatenateValues())
			{
				for(MetaMetadataValueField field : getConcatenateValues())
				{
					if(field.fromScalar != null)
					{
						ourDependencies.add(field);
					}
				}
			}
			
			// More semantics that generate dep's should be filtered here... 
			
			this.cachedValueDependencies =ourDependencies;
			return ourDependencies;
		}else{
			return cachedValueDependencies;
		}
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
	public void addAdditionalMetaInformation(List<MetaInformation> metaInfoBuf, MmdCompilerService compiler)
	{
		// @simpl_composite_as_scalar
		if (this.compositeScalar)
			metaInfoBuf.add(new MetaInformation(simpl_composite_as_scalar.class));

    if (fieldOps != null && fieldOps.size() > 0)
    {
      // TODO currently we can have only one @simpl_filter for a field.
      FieldOp op = fieldOps.get(0);
      if (op instanceof RegexOp)
      {
        // @simpl_filter
        RegexOp filter = (RegexOp) op;
        if (filter != null && filter.getRegex() != null && filter.getRegex().pattern().length() > 0
            && getMetaMetadataParser().equals(ParserBase.DIRECT_BINDING_PARSER))
        {
          List<String> argNames = new ArrayList<String>();
          List<Object> argValues = new ArrayList<Object>();
          argNames.add("regex");
          argValues.add(filter.getJavaRegex());
          if (filter.getGroup() > 0)
          {
            argNames.add("group");
            argValues.add(filter.getGroup());
          }
          if (filter.getReplace() != null)
          {
            argNames.add("replace");
            argValues.add(filter.getReplace());
          }
          metaInfoBuf.add(new MetaInformation(simpl_filter.class,
                                              argNames.toArray(new String[] {}),
                                              argValues.toArray()));
        }
      }
    }
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
		mmsf.fieldOps = new ArrayList<FieldOp>();
		mmsf.fieldOps.add(new RegexOp(Pattern.compile("regex"), "replace"));
		System.out.println(SimplTypesScope.serialize(mmsf, StringFormat.XML));
	}

	public static void testDeserialization() throws SIMPLTranslationException
	{
		String xml = "<scalar name=\"example\" scalar_type=\"String\" hint=\"XML_ATTRIBUTE\"><filter regex=\".\" replace=\".\" /></scalar>";
		MetaMetadataScalarField m = (MetaMetadataScalarField) MetaMetadataTranslationScope.get()
				.deserialize(xml, StringFormat.XML);
		System.out.println(m);
	}

	@Override
	public MetadataFieldDescriptor findOrGenerateMetadataFieldDescriptor(SimplTypesScope tscope, MetadataClassDescriptor contextCd)
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
						FieldType.SCALAR,
						null,
						contextCd,
						fieldName,
						scalarType2,
						this.getHint(),
						javaTypeName);
//				if (filter != null && filter.getRegex() != null && filter.getRegex().pattern().length() > 0)
//					fd.setRegexFilter(filter.getRegex(), filter.getGroup(), filter.getReplace());
				this.metadataFieldDescriptor = fd;
			}
		}
		return fd;
	}

	@Override
  protected String getFingerprintString()
	{
	  StringBuilder sb = StringBuilderUtils.acquire();
	  sb.append(super.getFingerprintString());
	  addToFp(sb, scalarType);
	  addToFp(sb, compositeScalar);
	  addCollectionToFp(sb, concatenateValues);
	  String fp = sb.toString();
	  StringBuilderUtils.release(sb);
	  return fp;
	}

}
