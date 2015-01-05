package ecologylab.bigsemantics.documentcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.net.ParsedURL;

/**
 * This class uses EhCache to provide a configuration cache for documents.
 * 
 * @author quyin
 */
public class EhCacheDocumentCache implements DocumentCache<ParsedURL, Document>
{

  /**
   * The name of the underlying cache in the Ehcache system.
   */
  static final String EHCACHE_NAME = "bigsemantics-document-cache";

  private Ehcache     cache;

  public EhCacheDocumentCache()
  {
    CacheManager cacheManager = CacheManager.getInstance();
    if (cacheManager.cacheExists(EHCACHE_NAME))
    {
      cacheManager.removeCache(EHCACHE_NAME);
    }
    cacheManager.addCache(EHCACHE_NAME);
    cache = cacheManager.getCache(EHCACHE_NAME);
  }

  @Override
  public boolean containsKey(ParsedURL key)
  {
    return cache.isKeyInCache(key);
  }

  @Override
  public Document get(ParsedURL key)
  {
    Element element = cache.get(key);
    return element == null ? null : (Document) element.getObjectValue();
  }

  @Override
  public Document get(ParsedURL key, String revision)
  {
    throw new UnsupportedOperationException("EhCacheDocumentCache does not support revisions.");
  }

  @Override
  public void put(ParsedURL key, Document document)
  {
    cache.put(new Element(key, document));
  }

  @Override
  public Document putIfAbsent(ParsedURL key, Document document)
  {
    Element prev = cache.putIfAbsent(new Element(key, document));
    return prev == null ? null : (Document) prev.getObjectValue();
  }

  @Override
  public boolean replace(ParsedURL key, Document oldDocument, Document newDocument)
  {
    return cache.replace(new Element(key, oldDocument), new Element(key, newDocument));
  }

  @Override
  public Document replace(ParsedURL key, Document newDocument)
  {
    Element prev = cache.replace(new Element(key, newDocument));
    return prev == null ? null : (Document) prev.getObjectValue();
  }

  @Override
  public void remove(ParsedURL key)
  {
    cache.remove(key);
  }

  @Override
  public boolean remove(ParsedURL key, Document oldDocument)
  {
    return cache.removeElement(new Element(key, oldDocument));
  }

}
