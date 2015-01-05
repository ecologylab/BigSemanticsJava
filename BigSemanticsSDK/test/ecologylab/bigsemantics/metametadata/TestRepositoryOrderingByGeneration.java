package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.RepositoryOrdering;
import ecologylab.bigsemantics.metametadata.RepositoryOrderingByGeneration;

/**
 * 
 * @author quyin
 */
public class TestRepositoryOrderingByGeneration
{

  @Test
  public void testOrderingByGeneration()
  {
    List<MetaMetadata> mmds = new ArrayList<MetaMetadata>();
    mmds.add(mockMmd("metadata", null, null));
    mmds.add(mockMmd("n10", "metadata", null));
    mmds.add(mockMmd("n11", null, "metadata"));
    mmds.add(mockMmd("n20", "n10", null));
    mmds.add(mockMmd("n21", null, "n10"));
    
    Collections.shuffle(mmds);
    
    RepositoryOrdering ordering = new RepositoryOrderingByGeneration();
    mmds = ordering.orderMetaMetadataForInheritance(mmds);
    List<String> names = new ArrayList<String>(mmds.size());
    for (MetaMetadata mmd : mmds)
      names.add(mmd.getName());
    
    Assert.assertTrue(names.indexOf("metadata") < names.indexOf("n10"));
    Assert.assertTrue(names.indexOf("metadata") < names.indexOf("n11"));
    Assert.assertTrue(names.indexOf("n10") < names.indexOf("n20"));
    Assert.assertTrue(names.indexOf("n10") < names.indexOf("n21"));
  }
  
  private MetaMetadata mockMmd(String name, String type, String extendsAttribute)
  {
    MetaMetadata mockMmd = new MetaMetadata();
    mockMmd.setName(name);
    mockMmd.setType(type);
    mockMmd.setExtendsAttribute(extendsAttribute);
    return mockMmd;
  }

}
