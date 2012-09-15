package ecologylab.semantics.metametadata.visualize;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.cyberneko.CybernekoWrapper;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * This class generates SVG graphs using dot (graphviz), to visualize the type hierarchy in the
 * meta-metadata repository.
 * 
 * @author quyin
 */
public class MmdTypeHierarchyVisualizer extends SingletonApplicationEnvironment
{
  
  public static final int MAGIC_EXIT_VALUE = -6174;
  
  protected static class Node
  {
    public String    name           = "";

    public Node      parent         = null;

    public String    parentLinkInfo = null;

    public Set<Node> children       = new HashSet<Node>();
  }

  SemanticsSessionScope sessionScope;
  
  Map<String, Node>     allNodes;
  
  Node                  rootOfRoots;
  
  Set<String>           rootNames;
  
  public MmdTypeHierarchyVisualizer() throws SIMPLTranslationException
  {
    super(MmdTypeHierarchyVisualizer.class.getSimpleName());
    
    check(isDotRunnable(), "Cannot run graphviz/dot: is it installed and in your PATH?");
    
    SimplTypesScope metadataTScope = RepositoryMetadataTranslationScope.get();
    sessionScope = new SemanticsSessionScope(metadataTScope, CybernekoWrapper.class);
    allNodes = new HashMap<String, Node>();
    for (MetaMetadata mmd : sessionScope.getMetaMetadataRepository().getMetaMetadataCollection())
    {
      String name = mmd.getName();
      if (name != null && !name.isEmpty())
      {
        Node node = null;
        if (allNodes.containsKey(name))
        {
          node = allNodes.get(name);
        }
        else
        {
          node = new Node();
          node.name = mmd.getName();
          allNodes.put(name, node);
        }
      
        String extendedMmdName = mmd.getExtendsAttribute();
        String typeMmdName = mmd.getType();
        String parentNodeName = null;
        if (extendedMmdName != null && !extendedMmdName.isEmpty())
        {
          // extends
          node.parentLinkInfo = "extends";
          parentNodeName = extendedMmdName;
        }
        else if (typeMmdName != null && !typeMmdName.isEmpty())
        {
          // type
          node.parentLinkInfo = "type";
          parentNodeName = typeMmdName;
        }
        else
        {
          if (rootOfRoots == null)
            rootOfRoots = node;
          else
            error("Cannot find extends or type attribute: " + node.name);
        }
        
        if (parentNodeName != null)
        {
          if (allNodes.containsKey(parentNodeName))
          {
            node.parent = allNodes.get(parentNodeName);
          }
          else
          {
            node.parent = new Node();
            node.parent.name = parentNodeName;
            allNodes.put(parentNodeName, node.parent);
          }
          node.parent.children.add(node);
        }
      }
      else
      {
        error("Meta-metadata with empty name: " + SimplTypesScope.serialize(mmd, StringFormat.XML));
      }
    }
  }
  
  protected boolean isDotRunnable()
  {
    StringBuilder err = new StringBuilder();
    boolean testDot = runProgram(null, null, err, "dot", "-V") == 0
                      && err.toString().startsWith("dot - graphviz version");
    err = new StringBuilder();
    boolean testUnflatten = runProgram(null, null, err, "unflatten", "-?") == 0
                            && err.toString().startsWith("Usage:");
    return testDot && testUnflatten;
  }
  
  public void visualize(String outSvgPathPrefix, boolean showTypeMmds, String... roots)
      throws IOException
  {
    List<Node> orderedNodes = new ArrayList<Node>();
    sortNodesPreOrder(orderedNodes);
    
    rootNames = new HashSet<String>();
    for (String rootName : roots)
      rootNames.add(rootName);
    if (rootNames.isEmpty())
      rootNames.add(rootOfRoots.name);
    
    for (Node root : orderedNodes)
    {
      if (rootNames.contains(root.name))
      {
        String dotScriptSrc = generateDotScript(root, showTypeMmds);
        generateSvg(dotScriptSrc, outSvgPathPrefix + "-" + root.name + ".svg");
      }
    }
  }
  
  protected void sortNodesPreOrder(List<Node> result)
  {
    sortNodesPreOrderHelper(result, rootOfRoots);
  }
  
  protected void sortNodesPreOrderHelper(List<Node> result, Node current)
  {
    result.add(current);
    for (Node child : current.children)
      sortNodesPreOrderHelper(result, child);
  }

  protected String generateDotScript(Node root, boolean showTypeMmds) throws IOException
  {
    StringBuilder result = new StringBuilder();
    
    appendDotScriptHead(result);
    generateDotScriptHelper(root, result, showTypeMmds);
    appendDotScriptTail(result);
    
    return result.toString();
  }
  
  protected void appendDotScriptHead(Appendable appendable) throws IOException
  {
    appendable.append("// Generated by DotScriptGenerator\n");
    appendable.append("\n");
    appendable.append("digraph mmd_type_hierarchy {\n");
    appendable.append("  graph [ rankdir=\"BT\" ]\n");
    appendable.append("  node [ shape=\"box\", style=\"rounded\" ]\n");
    appendable.append("  edge [ penwidth=2 ]\n");
    appendable.append("  // mmd type inheritance relations below:\n");
  }

  protected void generateDotScriptHelper(Node currentNode, Appendable result, boolean showTypeMmds)
      throws IOException
  {
    for (Node child : currentNode.children)
    {
      boolean isARoot = rootNames.contains(child.name);
      if (isARoot)
      {
        // special style for subgraph roots
        result.append(String.format("  %s [ style=\"rounded,filled\" ]\n", child.name));
      }
      
      if ("extends".equals(child.parentLinkInfo))
      {
        // extends
        result.append(String.format("  %s -> %s\n", child.name, currentNode.name));
      }
      else if ("type".equals(child.parentLinkInfo) && showTypeMmds)
      {
        // type
        // special style for type mmd
        result.append(String.format("  %s [ color=\"gray\", style=\"rounded,dashed\" ]\n", child.name));
        result.append(String.format("  %s -> %s [ style=\"dashed\", color=\"gray\"]\n",
                                    child.name,
                                    currentNode.name));
      }
      
      if (!isARoot)
        generateDotScriptHelper(child, result, showTypeMmds);
    }
  }
  
  protected void appendDotScriptTail(Appendable appendable) throws IOException
  {
    appendable.append("}\n");
    appendable.append("\n");
    appendable.append("// End of generated Dot script\n");
    appendable.append("\n");
  }
  
  protected boolean generateSvg(String dotScriptSrc, String outSvgPath)
  {
    StringBuilder out = new StringBuilder();
    int exitCode = runProgram(dotScriptSrc, out, null, "unflatten", "-f", "-l6");
    if (exitCode == 0)
    {
      String unflattenSrc = out.toString();
      StringBuilder err = new StringBuilder();
      exitCode = runProgram(unflattenSrc,
                            out,
                            err,
                            "dot",
                            "-Tsvg",
                            "-o" + outSvgPath);
      if (exitCode == 0)
      {
        return true;
      }
      else
      {
        error("Exit code: " + exitCode);
        error("Stdout output: " + out.toString());
        error("Stderr output: " + err.toString());
      }
    }
    
    return false;
  }
  
  protected void readAll(Reader reader, Appendable out) throws IOException
  {
    char[] buffer = new char[1024];
    while (true)
    {
      int n = reader.read(buffer, 0, 1024);
      if (n < 0)
        break;
      if (out != null)
      {
        String s = String.valueOf(buffer, 0, n);
        out.append(s);
      }
    }
  }

  protected int runProgram(CharSequence stdIn, Appendable stdOut, Appendable stdErr, String... cmd)
  {
    ProcessBuilder procBuilder = new ProcessBuilder(cmd);
    Process proc = null;
    int exitValue = MAGIC_EXIT_VALUE;
    
    try
    {
      proc = procBuilder.start();
      
      if (stdIn != null)
      {
        OutputStreamWriter writer = new OutputStreamWriter(proc.getOutputStream());
        writer.append(stdIn);
        writer.close();
      }
      
      InputStreamReader outReader = new InputStreamReader(proc.getInputStream());
      readAll(outReader, stdOut);
      outReader.close();
      
      InputStreamReader errReader = new InputStreamReader(proc.getErrorStream());
      readAll(errReader, stdErr);
      errReader.close();
      
      exitValue = proc.waitFor();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (proc != null)
        proc.destroy();
    }
    
    return exitValue;
  }
  
  public static void showHelpAndExit()
  {
    String progName = MmdTypeHierarchyVisualizer.class.getSimpleName();
    System.err.format("Usage: %s <OPTIONS> [<output-SVG-path-prefix>]\n", progName);
    System.err.format("Opts:\n");
    System.err.format("  -h    Show this help message.\n");
    System.err.format("  -s    Show only types that define new schemas.\n");
    System.err.format("  -r    Comma separated list of root types. For each root type, an individual SVG file will be generated.\n");
    System.err.format("Args:\n");
    System.err.format("  <output-SVG-path-prefix>\n");
    System.err.format("        Path prefix for output SVG files. Default to the user's document "
    		              + "folder, starting with mmd_types.\n");
    System.exit(-1);
  }

  public static void main(String[] args) throws SIMPLTranslationException, IOException
  {
    boolean showTypeMmds = true;
    String outSvgPathPrefix = PropertiesAndDirectories.userDocumentDir()
                              + File.separator
                              + "mmd_types";
    String[] roots = { "metadata", "document", "search", "compound_document", "creative_work" };
    
    for (int i = 0; i < args.length; ++i)
    {
      if ("-h".equals(args[i]))
      {
        showHelpAndExit();
      }
      else if ("-s".equals(args[i]))
      {
        showTypeMmds = false;
      }
      else if ("-r".equals(args[i]))
      {
        i++;
        roots = args[i].split(",");
      }
      else
      {
        outSvgPathPrefix = args[i];
      }
    }
  
    MmdTypeHierarchyVisualizer v = new MmdTypeHierarchyVisualizer();
    v.visualize(outSvgPathPrefix, showTypeMmds, roots);
  }

}
