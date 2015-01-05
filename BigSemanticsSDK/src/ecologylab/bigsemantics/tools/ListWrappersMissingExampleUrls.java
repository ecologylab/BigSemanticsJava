package ecologylab.bigsemantics.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTypesScope;
import ecologylab.bigsemantics.metametadata.ExampleUrl;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataSelector;

public class ListWrappersMissingExampleUrls
{

  SemanticsSessionScope scope;

  List<String>          names;

  public ListWrappersMissingExampleUrls()
  {
    scope = new SemanticsSessionScope(RepositoryMetadataTypesScope.get(),
                                      CybernekoWrapper.class);
  }

  public void list()
  {
    names = new ArrayList<String>();
    Collection<MetaMetadata> mmds = scope.getMetaMetadataRepository().getMetaMetadataCollection();
    for (MetaMetadata mmd : mmds)
    {
      if (mmd.isBuiltIn())
      {
        continue;
      }

      ArrayList<MetaMetadataSelector> selectors = mmd.getSelectors();
      if (selectors != null && selectors.size() > 0)
      {
        if (hasUrlBasedSelector(selectors))
        {
          ArrayList<ExampleUrl> exampleUrls = mmd.getExampleUrls();
          if (exampleUrls == null || exampleUrls.size() == 0)
          {
            names.add(mmd.getName());
          }
        }
      }
    }
    output(names);
  }

  private boolean hasUrlBasedSelector(ArrayList<MetaMetadataSelector> selectors)
  {
    for (MetaMetadataSelector selector : selectors)
    {
      if (isUrlBased(selector))
      {
        return true;
      }
    }
    return false;
  }

  private boolean isUrlBased(MetaMetadataSelector selector)
  {
    if (selector.getUrlStripped() != null && selector.getUrlStripped().toString().length() > 0)
    {
      return true;
    }
    if (selector.getUrlPathTree() != null && selector.getUrlPathTree().toString().length() > 0)
    {
      return true;
    }
    if (selector.getUrlRegex() != null && selector.getUrlRegex().pattern().length() > 0)
    {
      return true;
    }
    if (selector.getUrlRegexFragment() != null
        && selector.getUrlRegexFragment().pattern().length() > 0)
    {
      return true;
    }
    return false;
  }

  private void output(List<String> names)
  {
    Collections.sort(names);
    for (String name : names)
    {
      System.out.println(name);
    }
  }

  public static void main(String[] args)
  {
    ListWrappersMissingExampleUrls l = new ListWrappersMissingExampleUrls();
    l.list();
  }

}
