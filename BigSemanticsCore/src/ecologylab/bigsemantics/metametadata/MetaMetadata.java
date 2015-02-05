/**
 * 
 */
package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ecologylab.bigsemantics.actions.SemanticAction;
import ecologylab.bigsemantics.actions.SemanticActionTranslationScope;
import ecologylab.bigsemantics.collecting.LinkedMetadataMonitor;
import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_other_tags;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.types.element.IMappable;

/**
 * The meta-metadata class.
 * 
 * @author quyin
 * @author damaraju
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@simpl_inherit
public class MetaMetadata extends MetaMetadataCompositeField
    implements IMappable<String>// , HasLocalTranslationScope
{

  public enum Visibility
  {
    GLOBAL,
    PACKAGE,
  }

  @simpl_collection("selector")
  @simpl_nowrap
  @mm_dont_inherit
  private ArrayList<MetaMetadataSelector>         selectors;

  @simpl_collection("example_url")
  @simpl_nowrap
  @mm_dont_inherit
  private ArrayList<ExampleUrl>                   exampleUrls;

  @simpl_composite
  @mm_dont_inherit
  private FilterLocation                          filterLocation;

  @simpl_scalar
  private String                                  parser;

  @simpl_scalar
  @mm_dont_inherit
  private String                                  extractWith;

  @simpl_collection
  @simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
  @mm_dont_inherit
  private ArrayList<SemanticAction>               beforeSemanticActions;

  @simpl_collection
  @simpl_tag("operations")
  @simpl_other_tags({ "semantic_actions" })
  @simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
  private ArrayList<SemanticAction>               semanticActions;

  @simpl_collection
  @simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
  @mm_dont_inherit
  private ArrayList<SemanticAction>               afterSemanticActions;

  @simpl_scalar
  @mm_dont_inherit
  private boolean                                 builtIn;

  @simpl_scalar
  private RedirectHandling                        redirectHandling;

  @simpl_scalar
  private String                                  bibtexType;

  /**
   * Mixins are needed so that we can have objects of multiple metadata classes in side a single
   * metadata class. It basically provide us to simulate the functionality of multiple inheritance
   * which is missing in java.
   */
  @simpl_collection("mixins")
  @simpl_nowrap
  private ArrayList<String>                       mixins;

  @simpl_scalar
  private String                                  collectionOf;

  @simpl_collection("url_generator")
  @simpl_nowrap
  @mm_dont_inherit
  private ArrayList<UrlGenerator>                 urlGenerators;

  private Map<String, MetaMetadataField>          naturalIds;

  @simpl_map("link_with")
  @simpl_nowrap
  private HashMap<String, LinkWith>               linkWiths;

  @simpl_scalar
  protected Visibility                            visibility  = Visibility.GLOBAL;

  @simpl_scalar
  private boolean                                 noCache;

  @simpl_scalar
  @mm_dont_inherit
  private String                                  cacheLife;

  private long                                    cacheLifeMs = -1;

  private Map<MetaMetadataSelector, MetaMetadata> reselectMap;

  SimplTypesScope                                 localMetadataTranslationScope;

  public MetaMetadata()
  {
    super();
  }

  protected MetaMetadata(MetaMetadataField copy, String name)
  {
    super(copy, name);
  }

  public ArrayList<MetaMetadataSelector> getSelectors()
  {
    if (selectors == null)
      return MetaMetadataSelector.NULL_SELECTOR;
    return selectors;
  }

  public ArrayList<ExampleUrl> getExampleUrls()
  {
    return exampleUrls;
  }

  public FilterLocation getFilterLocation()
  {
    return filterLocation;
  }

  public String getParser()
  {
    return parser;
  }

  public ArrayList<SemanticAction> getBeforeSemanticActions()
  {
    return beforeSemanticActions;
  }

  /**
   * @return the semanticActions
   */
  public ArrayList<SemanticAction> getSemanticActions()
  {
    return semanticActions;
  }

  public ArrayList<SemanticAction> getAfterSemanticActions()
  {
    return afterSemanticActions;
  }

  /**
   * @return the collectionOf
   */
  public String getCollectionOf()
  {
    return collectionOf;
  }

  /**
   * @return the redirectHandling
   */
  public RedirectHandling getRedirectHandling()
  {
    return redirectHandling;
  }

  public Map<String, MetaMetadataField> getNaturalIdFields()
  {
    return naturalIds;
  }

  public MetaMetadataField getNaturalIdField(String fieldName)
  {
    return naturalIds == null ? null : naturalIds.get(fieldName);
  }

  public Map<String, LinkWith> getLinkWiths()
  {
    return linkWiths;
  }

  public long getCacheLifeMs()
  {
    if (cacheLifeMs >= 0)
    {
      return cacheLifeMs;
    }

    cacheLifeMs = getCacheLifeMsHelper(cacheLife);
    if (cacheLifeMs < 0)
    {
      cacheLifeMs = getCacheLifeMsHelper(getRepository().getDefaultCacheLife());
    }
    if (cacheLifeMs < 0)
    {
      cacheLifeMs = 0;
    }
    return cacheLifeMs;
  }

  private long getCacheLifeMsHelper(String cacheLifeSpec)
  {
    if (cacheLifeSpec != null)
    {
      long num = -1;
      String unit = null;

      // parse cacheLife
      int i = 0;
      while (i < cacheLifeSpec.length() && Character.isDigit(cacheLifeSpec.charAt(i)))
      {
        ++i;
      }
      if (i > 0 && i < cacheLifeSpec.length())
      {
        num = Long.parseLong(cacheLifeSpec.substring(0, i));
        unit = cacheLifeSpec.substring(i);
      }

      if (num > 0 && unit != null)
      {
        if (unit.equals("ms"))
        {
          return num;
        }
        if (unit.equals("s"))
        {
          return num * 1000;
        }
        if (unit.equals("m"))
        {
          return num * 1000 * 60;
        }
        if (unit.equals("h"))
        {
          return num * 1000 * 60 * 60;
        }
        if (unit.equals("d"))
        {
          return num * 1000 * 60 * 60 * 24;
        }
      }
    }

    return -1;
  }

  public Map<MetaMetadataSelector, MetaMetadata> getReselectMap()
  {
    return reselectMap;
  }

  public SimplTypesScope getLocalMetadataTypesScope()
  {
    return this.localMetadataTranslationScope;
  }

  SimplTypesScope localMetadataTypesScope(SimplTypesScope metadataTScope)
  {
    return localMetadataTranslationScope != null ? localMetadataTranslationScope : SimplTypesScope
        .get("mmd_local_tscope:" + this.getName(), new SimplTypesScope[]
        { metadataTScope });
  }

  /**
   * This always returns null since there is no "field" that this mmd inherits from.
   */
  @Override
  public MetaMetadataField getSuperField()
  {
    return null;
  }

  public MetaMetadata getSuperMmd()
  {
    return (MetaMetadata) super.getSuperField();
  }

  public void setSuperMmd(MetaMetadata result)
  {
    setSuperField(result);
  }

  @Override
  protected boolean isInlineDefinition()
  {
    return false;
  }

  @Override
  public boolean isBuiltIn()
  {
    return builtIn;
  }

  public boolean isRootMetaMetadata()
  {
    return isRootMetaMetadata(this);
  }

  public boolean isNoCache()
  {
    return noCache;
  }

  public boolean isDerivedFrom(MetaMetadata base)
  {
    MetaMetadata mmd = this;
    while (mmd != null)
    {
      if (mmd == base)
        return true;
      mmd = mmd.getSuperMmd();
    }
    return false;
  }

  public void addSelector(MetaMetadataSelector s)
  {
    if (selectors == null)
      selectors = new ArrayList<MetaMetadataSelector>();

    selectors.add(s);
  }

  /**
   * @return the mimeTypes
   */
  public ArrayList<String> getMimeTypes()
  {
    ArrayList<String> result = null;

    for (MetaMetadataSelector selector : getSelectors())
    {
      if (result == null)
        result = new ArrayList<String>();

      ArrayList<String> mimeTypes = selector.getMimeTypes();
      if (mimeTypes != null)
        result.addAll(mimeTypes);
    }

    return result;
  }

  public ArrayList<String> getSuffixes()
  {
    ArrayList<String> result = null;

    for (MetaMetadataSelector selector : getSelectors())
    {
      if (result == null)
        result = new ArrayList<String>();

      ArrayList<String> suffixes = selector.getSuffixes();
      if (suffixes != null)
        result.addAll(suffixes);
    }

    return result;
  }

  public void addNaturalIdField(String naturalId, MetaMetadataField childField)
  {
    if (naturalIds == null)
    {
      naturalIds = new HashMap<String, MetaMetadataField>();
    }
    naturalIds.put(naturalId, childField);
  }

  public void addLinkWith(LinkWith lw)
  {
    if (linkWiths == null)
    {
      linkWiths = new HashMap<String, LinkWith>();
    }
    linkWiths.put(lw.key(), lw);
  }

  public void addReselectEntry(MetaMetadataSelector selector, MetaMetadata mmd)
  {
    if (reselectMap == null)
    {
      reselectMap = new HashMap<MetaMetadataSelector, MetaMetadata>();
    }
    reselectMap.put(selector, mmd);
  }

  public static boolean isRootMetaMetadata(MetaMetadata mmd)
  {
    return mmd.getName().equals(ROOT_MMD_NAME);
  }

  @Override
  public String key()
  {
    return getName();
  }

  @Override
  public String getTypeName()
  {
    if (getType() != null)
      return getType();
    return getName();
  }

  /**
   * @return for meta-metadata defining a new mmd type, return the super meta-metadata type name
   *         (extends= or "metadata"); for meta-metadata decorating an existent mmd type, return the
   *         decorated meta-metadata type name (type=).
   */
  public String getSuperMmdTypeName()
  {
    // decorative
    if (getType() != null)
      return getType();

    // definitive
    if (getExtendsAttribute() != null)
      return getExtendsAttribute();
    else
      return "metadata";
  }

  @Override
  protected String getMetadataClassName()
  {
    return this.packageName() + "." + this.getMetadataClassSimpleName();
  }

  /**
   * 
   * @return the corresponding Metadata class simple name.
   */
  @Override
  protected String getMetadataClassSimpleName()
  {
    if (this.isBuiltIn() || this.isNewMetadataClass())
    {
      // new definition
      return XMLTools.classNameFromElementName(this.getName());
    }
    else
    {
      // re-using existing type
      // do not use this.type directly because we don't know if that is a definition or just
      // re-using exsiting type
      MetaMetadata inheritedMmd = this.getSuperMmd();
      return inheritedMmd == null ? null : inheritedMmd.getMetadataClassSimpleName();
    }
  }

  public Metadata constructMetadata()
  {
    return constructMetadata(this.getRepository().metadataTranslationScope());
  }

  /**
   * Lookup the Metadata class that corresponds to the (tag) name of this, using the
   * DefaultMetadataTranslationSpace. Assuming that is found, use reflection to instantiate it.
   * 
   * @return An instance of the Metadata subclass that corresponds to this, or null, if there is
   *         none.
   */
  public Metadata constructMetadata(SimplTypesScope ts)
  {
    Metadata result = null;
    Class<? extends Metadata> metadataClass =
        (Class<? extends Metadata>) this.getMetadataClassDescriptor().getDescribedClass();

    if (metadataClass != null)
    {
      Class[] argClasses = new Class[]
      { MetaMetadataCompositeField.class };
      Object[] argObjects = new Object[]
      { this };

      result = ReflectionTools.getInstance(metadataClass, argClasses, argObjects);
      if (mixins != null && mixins.size() > 0)
      {
        for (String mixinName : mixins)
        {
          MetaMetadata mixinMM = getRepository().getMMByName(mixinName);
          if (mixinMM != null)
          {
            Metadata mixinMetadata = mixinMM.constructMetadata(ts);
            if (mixinMetadata != null)
              result.addMixin(mixinMetadata);
          }
        }
      }
      result.setMetaMetadataName(getName());
      result.setMetaMetadata(this);
    }
    return result;
  }

  public ParsedURL generateUrl(String naturalId, String value)
  {
    if (urlGenerators != null)
    {
      for (UrlGenerator ug : urlGenerators)
      {
        if (ug.canGenerate(naturalId))
        {
          return ug.generate(getRepository(), naturalId, value);
        }
      }
    }
    return null;
  }

  void setUpLinkWith(MetaMetadataRepository repository)
  {
    LinkedMetadataMonitor monitor = repository.getLinkedMetadataMonitor();
    if (linkWiths != null)
    {
      for (String lwName : linkWiths.keySet())
      {
        LinkWith lw = linkWiths.get(lwName);
        MetaMetadata targetMmd = this.getRepository().getMMByName(lw.getName());
        if (targetMmd != null)
        {
          monitor.registerName(lw.getName());
          String name = this.getName();
          monitor.registerName(name);

          if (targetMmd.getLinkWiths() != null && targetMmd.getLinkWiths().containsKey(name))
          {
            // if there is already a reverse link, just make sure the reverse link reference is set
            LinkWith r = targetMmd.getLinkWiths().get(name);
            if (!r.isReverse())
            {
              // warning("not encouraging explicitly defining reverse links!");
              r.setReverse(true);
              lw.setReverseLink(r);
            }
          }
          else
          {
            // if there isn't, create a new one
            LinkWith r = lw.createReverseLink(name);
            targetMmd.addLinkWith(r);
          }
        }
        else
        {
          error("link_with: meta-metadata not found: " + lw.getName());
        }
      }
    }
  }

  protected void inheritSemanticActions(MetaMetadata inheritedMmd)
  {
    if (semanticActions == null)
    {
      semanticActions = inheritedMmd.getSemanticActions();
    }
    if (afterSemanticActions == null)
    {
      afterSemanticActions = inheritedMmd.getAfterSemanticActions();
    }
  }

  @Override
  public MetadataClassDescriptor bindMetadataClassDescriptor(SimplTypesScope metadataTScope)
  {
    if (this.getMetadataClassDescriptor() != null)
      return this.getMetadataClassDescriptor();

    // create a temporary local metadata translation scope
    SimplTypesScope localMetadataTScope = localMetadataTypesScope(metadataTScope);

    // record the initial number of classes in the local translation scope
    int initialLocalTScopeSize = localMetadataTScope.entriesByClassName().size();

    // do actual stuff ...
    super.bindMetadataClassDescriptor(localMetadataTScope);

    // if tag overlaps, or there are fields using classes not in metadataTScope, use localTScope
    MetadataClassDescriptor thisCd = this.getMetadataClassDescriptor();
    if (thisCd != null)
    {
      thisCd.setDefiningMmdIfNotSet(this);

      MetadataClassDescriptor thatCd = (MetadataClassDescriptor) metadataTScope
          .getClassDescriptorByTag(thisCd.getTagName());
      if (thisCd != thatCd)
      {
        localMetadataTScope.addTranslation(thisCd);
        this.localMetadataTranslationScope = localMetadataTScope;
      }
      else if (localMetadataTScope.entriesByClassName().size() > initialLocalTScopeSize)
        this.localMetadataTranslationScope = localMetadataTScope;
      else
        this.localMetadataTranslationScope = metadataTScope;

      // we should have stuffs in the scope already
      thisCd.resolvePolymorphicAnnotations();
      thisCd.resolveUnresolvedClassesAnnotationFDs();
      thisCd.resolveUnresolvedScopeAnnotationFDs();
    }

    // return the bound metadata class descriptor
    return thisCd;
  }

  @Override
  void findOrGenerateMetadataClassDescriptor(SimplTypesScope tscope)
  {
    if (this.getMetadataClassDescriptor() == null)
    {
      // this.inheritMetaMetadata(inheritanceHandler);
      MetaMetadata superMmd = this.getSuperMmd();
      if (superMmd != null)
      {
        superMmd.findOrGenerateMetadataClassDescriptor(tscope);
      }
      
      if (this.getMetadataClassDescriptor() == null)
      {
    	  // here we need to do another check, because superMmd.findOrGenerateMetadataClassDescriptor()
    	  // can be recursive and already set the class descriptor for this.
    	  
      MetadataClassDescriptor superCd =
          superMmd == null ? null : superMmd.getMetadataClassDescriptor();
      if (this.isNewMetadataClass())
      {
        String tagOrName = this.getTagOrName();
        String className = XMLTools.classNameFromElementName(this.getName());
        String packageName = this.packageName();
        MetadataClassDescriptor cd = new MetadataClassDescriptor(this,
                                                                 tagOrName,
                                                                 this.getComment(),
                                                                 packageName,
                                                                 className,
                                                                 superCd,
                                                                 null);
        // setting this early allows referring to the same class in fields
        this.setMetadataClassDescriptor(cd);

        for (MetaMetadataField f : this.getChildrenMap())
        {
          if (f.getDeclaringMmd() == this && f.getSuperField() == null)
          {
            MetadataFieldDescriptor fd = f.findOrGenerateMetadataFieldDescriptor(tscope, cd);
            cd.addMetadataFieldDescriptor(fd);
          }
          // if (!f.isCloned() && f instanceof MetaMetadataNestedField)
          if (f.parent() == this && f instanceof MetaMetadataNestedField)
          {
            ((MetaMetadataNestedField) f).findOrGenerateMetadataClassDescriptor(tscope);
          }
          
        }

        MetadataClassDescriptor existingCdWithThisTag = (MetadataClassDescriptor) tscope
            .getClassDescriptorByTag(tagOrName);
        if (existingCdWithThisTag != null)
        {
          warning("Class descriptor exists for tag [" + tagOrName + "]: " + existingCdWithThisTag);
        }
        tscope.addTranslation(cd);
      }
      else
      {
        this.setMetadataClassDescriptor(superCd);
      }

      if (this.getScope() != null)
      {
        for (Object obj : this.getScope().values())
        {
          if (obj instanceof MetaMetadata)
          {
            MetaMetadata inlineMmd = (MetaMetadata) obj;
            inlineMmd.findOrGenerateMetadataClassDescriptor(tscope);
          }
        }
      }
      }
    }
  }

  @Override
  protected String getFingerprintString()
  {
    StringBuilder sb = StringBuilderUtils.acquire();
    sb.append(super.getFingerprintString());
    addToFp(sb, parser);
    addToFp(sb, getExtendsAttribute());
    addCollectionToFp(sb, mixins);
    String fp = sb.toString();
    StringBuilderUtils.release(sb);
    return fp;
  }

}
