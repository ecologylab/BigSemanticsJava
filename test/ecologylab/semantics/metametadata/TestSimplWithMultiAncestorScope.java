package ecologylab.semantics.metametadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.serialization.types.element.IMappable;

public class TestSimplWithMultiAncestorScope
{

  @simpl_composite
  MmdScope              mmds;

  @simpl_map("mmd_scope")
  Map<String, MmdScope> packages;

  @Before
  public void init()
  {
    mmds = new MmdScope();

    MetaMetadata mmd1 = new MetaMetadata();
    mmd1.setName("cat");
    mmds.put(mmd1.getName(), mmd1);

    MetaMetadata mmd2 = new MetaMetadata();
    mmd2.setName("dog");
    mmds.put(mmd2.getName(), mmd2);

    MetaMetadata mmd3 = new MetaMetadata();
    mmd3.setName("tiger");
    mmds.put(mmd3.getName(), mmd3);

    packages = new HashMap<String, MmdScope>();

    MmdScope feline = new MmdScope();
    feline.name = "feline";
    feline.put(mmd1.getName(), mmd1);
    feline.put(mmd3.getName(), mmd3);
    packages.put("feline", feline);

    MmdScope caline = new MmdScope();
    caline.name = "caline";
    caline.put(mmd2.getName(), mmd2);
    packages.put("caline", caline);
  }

  String serialize() throws SIMPLTranslationException
  {
    SimplTypesScope tscope = SimplTypesScope.get("test_simpl_w_mas",
                                                 TestSimplWithMultiAncestorScope.class,
                                                 MmdScope.class);
    StringBuilder sb = tscope.serialize(this, StringFormat.XML);
    return sb == null ? null : sb.toString();
  }

  @Test
  public void testMmdScope() throws SIMPLTranslationException
  {
    String xml = serialize();
    Assert.assertNotNull(xml);
    Assert.assertTrue(xml.length() > 0);

    SimplTypesScope tscope = SimplTypesScope.get("test_simpl_w_mas",
                                                 TestSimplWithMultiAncestorScope.class,
                                                 MmdScope.class);
    TestSimplWithMultiAncestorScope test = (TestSimplWithMultiAncestorScope) tscope
        .deserialize(xml, StringFormat.XML);

    Assert.assertNotNull(test);
    Assert.assertNotNull(test.mmds);
    Assert.assertEquals(3, test.mmds.size());
    assertContainsNames(test.mmds.values(), "cat", "dog", "tiger");
  }

  @Test
  public void testMapOfMmdScope() throws SIMPLTranslationException
  {
    String xml = serialize();
    Assert.assertNotNull(xml);
    Assert.assertTrue(xml.length() > 0);

    SimplTypesScope tscope = SimplTypesScope.get("test_simpl_w_mas",
                                                 TestSimplWithMultiAncestorScope.class,
                                                 MmdScope.class);
    TestSimplWithMultiAncestorScope test = (TestSimplWithMultiAncestorScope) tscope
        .deserialize(xml, StringFormat.XML);

    Assert.assertNotNull(test.packages);
    Assert.assertEquals(2, test.packages.size());
    assertContainsNames(test.packages.values(), "feline", "caline");
    MmdScope scope = test.packages.get("feline");
    assertContainsNames(scope.values(), "cat", "tiger");
    scope = test.packages.get("caline");
    assertContainsNames(scope.values(), "dog");
  }

  <T extends IMappable<String>> void assertContainsNames(Collection<T> values, String... names)
  {
    Set<String> set = new HashSet<String>();
    for (IMappable<String> item : values)
      set.add(item.key());
    for (int i = 0; i < names.length; ++i)
      Assert.assertTrue(set.contains(names[i]));
  }

}
