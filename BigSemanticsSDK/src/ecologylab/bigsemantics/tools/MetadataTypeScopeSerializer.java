package ecologylab.bigsemantics.tools;

import java.io.File;

import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.builtins.ClippableDocument;
import ecologylab.bigsemantics.metadata.builtins.Clipping;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.Format;

/**
 * 
 * @author quyin
 *
 */
public class MetadataTypeScopeSerializer
{

  /**
   * 
   * @param serializedScopeFile
   * @param format
   * @throws SIMPLTranslationException 
   */
  public void serializeScope(File serializedScopeFile, Format format) throws SIMPLTranslationException
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;

    SimplTypesScope scope = RepositoryMetadataTranslationScope.get();
    createDerivedScopes(scope);
    SimplTypesScope.serialize(scope, serializedScopeFile, format);
  }
  
  /**
   * Derived scopes are referred to in simpl_scope annotations. They have to be created so that
   * those annotations can be resolved correctly during de/serialization.
   * 
   * @param repositoryMetadata
   */
  public static void createDerivedScopes(SimplTypesScope repositoryMetadata)
  {
    SimplTypesScope documentsScope = repositoryMetadata.getAssignableSubset("repository_documents",
                                                                            Document.class);
    SimplTypesScope clippingsTypeScope = repositoryMetadata
        .getAssignableSubset("repository_clippings", Clipping.class);
//    SimplTypesScope noAnnotationsScope = repositoryMetadata
//        .getSubtractedSubset("repository_no_annotations", Annotation.class);
    SimplTypesScope mediaTypesScope = repositoryMetadata
        .getAssignableSubset("repository_media", ClippableDocument.class);
    mediaTypesScope.addTranslation(Clipping.class);
  }

  /**
   * @param args
   * @throws SIMPLTranslationException 
   */
  public static void main(String[] args) throws SIMPLTranslationException
  {
    if (args.length != 2)
    {
      System.err.println("args: <serialized-scope-file-path> <format>");
      System.err.println("    <format> can be xml or json");
      System.exit(-1);
    }
    
    File serializedScopeFile = new File(args[0]);
    String fmtName = args[1].toLowerCase();
    Format fmt = null;
    if ("xml".equals(fmtName))
      fmt = Format.XML;
    else if ("json".equals(fmtName))
      fmt = Format.JSON;
    else
    {
      System.err.println("only xml and json formats are supported.");
      System.exit(-2);
    }
    
    MetadataTypeScopeSerializer mtss = new MetadataTypeScopeSerializer();
    mtss.serializeScope(serializedScopeFile, fmt);
  }

}
