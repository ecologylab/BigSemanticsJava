package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.serialization.types.element.IMappable;

/**
 * 
 * @author quyin
 */
public class TestMmdScope
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
    feline.setId("feline");
    feline.put(mmd1.getName(), mmd1);
    feline.put(mmd3.getName(), mmd3);
    packages.put("feline", feline);

    MmdScope caline = new MmdScope();
    caline.setId("caline");
    caline.put(mmd2.getName(), mmd2);
    packages.put("caline", caline);
  }

  String serialize() throws SIMPLTranslationException
  {
    SimplTypesScope tscope = SimplTypesScope.get("test_simpl_w_mas",
                                                 TestMmdScope.class,
                                                 MmdScope.class);
    StringBuilder sb = tscope.serialize(this, StringFormat.XML);
    return sb == null ? null : sb.toString();
  }

  @Test
  public void testMmdScope() throws SIMPLTranslationException
  {
    String xml = serialize();
    assertNotNull(xml);
    assertTrue(xml.length() > 0);

    SimplTypesScope tscope = SimplTypesScope.get("test_simpl_w_mas",
                                                 TestMmdScope.class,
                                                 MmdScope.class);
    TestMmdScope test = (TestMmdScope) tscope
        .deserialize(xml, StringFormat.XML);

    assertNotNull(test);
    assertNotNull(test.mmds);
    assertEquals(3, test.mmds.size());
    assertContainsNames(test.mmds.values(), "cat", "dog", "tiger");
  }

  @Test
  public void testMapOfMmdScopes() throws SIMPLTranslationException
  {
    String xml = serialize();
    assertNotNull(xml);
    assertTrue(xml.length() > 0);

    SimplTypesScope tscope = SimplTypesScope.get("test_simpl_w_mas",
                                                 TestMmdScope.class,
                                                 MmdScope.class);
    TestMmdScope test = (TestMmdScope) tscope
        .deserialize(xml, StringFormat.XML);

    assertNotNull(test.packages);
    assertEquals(2, test.packages.size());
    assertContainsNames(test.packages.values(), "feline", "caline");
    MmdScope scope = test.packages.get("feline");
    assertContainsNames(scope.values(), "cat", "tiger");
    scope = test.packages.get("caline");
    assertContainsNames(scope.values(), "dog");
  }

  @Test
  public void testDeSerialization() throws SIMPLTranslationException
  {
    MetaMetadata foo = new MetaMetadata();
    foo.setName("foo");

    MmdGenericTypeVar bar = new MmdGenericTypeVar();
    bar.setName("bar");

    MetaMetadata baz = new MetaMetadata();
    baz.setName("baz");

    MmdScope moon = new MmdScope("moon");
    moon.put(foo.getName(), foo);

    baz.setScope(moon);

    MmdScope sun = new MmdScope("sun");
    sun.put(bar.getName(), bar);

    MmdScope earth = new MmdScope("earth", moon, sun);
    earth.put(baz.getName(), baz);

    MetaMetadataCompositeField field = new MetaMetadataCompositeField();
    field.setName("test_field");
    field.setScope(earth);

    String xml = SimplTypesScope.serialize(field, StringFormat.XML).toString();
    System.out.println(xml);

    SimplTypesScope tscope = SimplTypesScope.get(getClass().getSimpleName() + "-test1",
                                                 MetaMetadataCompositeField.class,
                                                 MetaMetadata.class,
                                                 MmdScope.class,
                                                 MmdGenericTypeVar.class);
    MetaMetadataCompositeField result =
        (MetaMetadataCompositeField) tscope.deserialize(xml, StringFormat.XML);

    assertNotNull(result);
    assertEquals("test_field", result.getName());

    MmdScope scope = result.getScope();
    assertNotNull(scope);
    assertEquals("earth", scope.getId());

    assertNotNull(scope.get("foo"));
    assertNotNull(scope.get("bar"));
    assertNotNull(scope.get("baz"));

    assertEquals(1, scope.size());
    assertNotNull(scope.getLocally("baz"));

    List<MmdScope> ancestors = scope.ancestors();
    assertEquals(2, ancestors.size());

    MmdScope ancestor = ancestors.get(0);
    assertEquals("moon", ancestor.getId());
    assertEquals(1, ancestor.size());
    assertNotNull(ancestor.getLocally("foo"));

    ancestor = ancestors.get(1);
    assertEquals("sun", ancestor.getId());
    assertEquals(1, ancestor.size());
    assertNotNull(ancestor.getLocally("bar"));
  }

  void assertContainsNames(Collection<?> values, String... names)
  {
    Set<String> set = new HashSet<String>();
    for (Object obj : values)
    {
      if (obj instanceof IMappable)
      {
        set.add(((IMappable<?>) obj).key().toString());
      }
    }
    for (int i = 0; i < names.length; ++i)
    {
      assertTrue(set.contains(names[i]));
    }
  }

}
