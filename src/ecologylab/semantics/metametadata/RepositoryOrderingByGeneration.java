package ecologylab.semantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositoryOrderingByGeneration implements RepositoryOrdering
{

  static class TreeNode
  {
    String       name;

    MetaMetadata mmd;

    List<TreeNode> children;

    public void addChild(TreeNode child)
    {
      if (children == null)
        children = new ArrayList<TreeNode>();
      children.add(child);
    }
  }

  @Override
  public List<MetaMetadata> orderMetaMetadataForInheritance(List<MetaMetadata> mmds)
  {
    Map<String, TreeNode> nodes = new HashMap<String, TreeNode>(mmds.size());
    TreeNode root = null;
    
    // build the tree
    for (MetaMetadata mmd : mmds)
    {
      TreeNode node = new TreeNode();
      node.name = mmd.getName();
      node.mmd = mmd;
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
        superNode.addChild(node);
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
      if (node.children != null)
      {
        for (TreeNode child : node.children)
          result.add(child.mmd);
      }
    }

    return result;
  }
  
}
