package ecologylab.bigsemantics.documentcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import ecologylab.bigsemantics.collecting.DocumentCache;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.net.ParsedURL;

public class EhCacheDocumentCache implements DocumentCache<ParsedURL, Document>
{

  private Cache cache;
  
  public EhCacheDocumentCache()
  {
    cache = CacheManager.getInstance().getCache("document-cache");
  }
  
  @Override
  public boolean containsKey(ParsedURL key)
  {
    return cache.isKeyInCache(key.toString());
  }

  @Override
  public Document get(ParsedURL key)
  {
    return (Document) cache.get(key).getObjectValue();
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
