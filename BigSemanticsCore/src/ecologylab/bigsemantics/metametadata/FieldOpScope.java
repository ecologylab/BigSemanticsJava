package ecologylab.bigsemantics.metametadata;

import ecologylab.serialization.SimplTypesScope;

public class FieldOpScope
{

  public static final String NAME    = "field_op_type_scope";

  static final Class[]       CLASSES =
                                     {
                                     RegexOp.class,
                                     };

  public static SimplTypesScope get()
  {
    return SimplTypesScope.get(NAME, CLASSES);
  }

}
