package ecologylab.bigsemantics.logging;

import ecologylab.generic.Debug;
import ecologylab.serialization.SimplTypesScope;

/**
 * 
 * @author quyin
 */
public class DocumentLogRecordScope extends Debug
{

  public static final String   NAME      = "document_log_record_scope";

  protected static final Class CLASSES[] = {
                                         DocumentLogRecord.class,
                                         Phase.class,
                                         DocumentErrorRecord.class,
                                         };

  public static SimplTypesScope get()
  {
    return SimplTypesScope.get(NAME, CLASSES);
  }

  public static void addType(Class<? extends DocumentLogRecord> type)
  {
    get().addTranslation(type);
  }

}
