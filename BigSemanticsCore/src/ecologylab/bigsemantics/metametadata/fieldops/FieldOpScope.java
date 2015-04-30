package ecologylab.bigsemantics.metametadata.fieldops;

import ecologylab.serialization.SimplTypesScope;

/**
 * 
 * @author quyin
 */
public class FieldOpScope
{

  public static final String NAME    = "field_op_type_scope";

  static final Class<?>[]    CLASSES =
                                     {
                                     Prepend.class,
                                     Append.class,
                                     Strip.class,
                                     Substring.class,
                                     DecodeUrl.class,
                                     Match.class,
                                     Replace.class,
                                     GetParam.class,
                                     SetParam.class,
                                     OverrideParams.class,
                                     StripParam.class,
                                     StripParamsBut.class,
                                     };

  public static SimplTypesScope get()
  {
    return SimplTypesScope.get(NAME, CLASSES);
  }

}
