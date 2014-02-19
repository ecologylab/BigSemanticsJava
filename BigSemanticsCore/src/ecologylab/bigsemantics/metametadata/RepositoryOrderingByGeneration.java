package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

public class RepositoryOrderingByGeneration implements RepositoryOrdering
{

  @simpl_tag("node")
  public static class TreeNode
  {

    @simpl_scalar
    String       name;

    MetaMetadata mmd;

    @simpl_scalar
    String exampleUrl;
    
    @simpl_collection("all_example_url")
    @simpl_nowrap
    List<String> allExampleUrls;

    @simpl_collection("subtype")
    @simpl_nowrap
    List<TreeNode> subtypes;
    
    public void addSubtype(TreeNode child)
    {
      if (subtypes == null)
        subtypes = new ArrayList<TreeNode>();
      subtypes.add(child);
    }

  }
  
  public TreeNode root;

  @Override
  public List<MetaMetadata> orderMetaMetadataForInheritance(List<MetaMetadata> mmds)
  {
    Map<String, TreeNode> nodes = new HashMap<String, TreeNode>(mmds.size());
    root = null;
    
    // build the tree
    for (MetaMetadata mmd : mmds)
    {
      TreeNode node = new TreeNode();
      node.name = mmd.getName();
      node.mmd = mmd;
      ArrayList<ExampleUrl> exampleUrls = mmd.getExampleUrls();
      List<String> nodeExampleUrls = new ArrayList<String>();
      if (exampleUrls != null && exampleUrls.size() > 0)
      {
        for (ExampleUrl url : exampleUrls)
        {
          ParsedURL purl = url.getUrl();
          if (purl != null)
          {
            nodeExampleUrls.add(purl.toString());
          }
        }
        if (nodeExampleUrls.size() > 0)
        {
          node.allExampleUrls = nodeExampleUrls;
          node.exampleUrl = node.allExampleUrls.get(0);
        }
      }
      if (MetaMetadata.isRootMetaMetadata(mmd))
      {
        root = node;
      }
      nodes.put(node.name, node);
    }
    for (MetaMetadata mmd : mmds)
    {
      TreeNode node = nodes.get(mmd.getName());
      if (!MetaMetadata.isRootMetaMetadata(mmd))
      {
        String superName = mmd.getType() == null ? mmd.getExtendsAttribute() : mmd.getType(); 
        if (superName == null)
          throw new RuntimeException("Non-root mmd without base mmd: " + mmd.getName());
        TreeNode superNode = nodes.get(superName);
        superNode.addSubtype(node);
      }
    }
    
    // BFS and output
    if (root == null)
      throw new RuntimeException("No root mmd found!");
    List<MetaMetadata> result = new ArrayList<MetaMetadata>(mmds.size());
    result.add(root.mmd);
    int p = 0;
    while (p < result.size())
    {
      TreeNode node = nodes.get(result.get(p++).getName());
      if (node.subtypes != null)
      {
        for (TreeNode child : node.subtypes)
          result.add(child.mmd);
      }
    }

    return result;
  }
  
}
