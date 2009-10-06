/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;

/**
 * This class is the base class for semantic actions which can have nested semantic actions inside
 * them. Right now only FOREACH and IF semantic actions can have other semantic actions nested
 * inside them.
 * 
 * @author amathur
 * 
 */

public abstract class NestedSemanticAction<SA extends SemanticAction> extends SemanticAction
{

	/**
	 * List of nested semantic actions.
	 */
	@xml_collection()
	@xml_classes(
	{ CreateAndVisualizeImgSurrogateSemanticAction.class,
			CreateContainerForSearchSemanticAction.class, CreateContainerSemanticAction.class,
			ForEachSemanticAction.class, GeneralSemanticAction.class,
			ProcessDocumentSemanticAction.class, ProcessSearchSemanticAction.class,
			SetMetadataSemanticAction.class, SetterSemanticAction.class,
			CreateSearchSemanticAction.class, GetFieldSemanticAction.class,
			CreateSemanticAnchorSemanticAction.class, QueueDocumentDownloadSemanticAction.class,
			ApplyXPathSemanticAction.class, IfSemanticAction.class,
			CreateAndVisualizeTextSurrogateSemanticAction.class, TrySyncNestedMetadata.class,
			EvaluateRankWeight.class })
	private ArrayList<SA>	nestedSemanticActionList;

	/**
	 * @return the nestedSemanticActionList
	 */
	public ArrayList<SA> getNestedSemanticActionList()
	{
		return nestedSemanticActionList;
	}

	/**
	 * @param nestedSemanticActionList
	 *          the nestedSemanticActionList to set
	 */
	public void setNestedSemanticActionList(ArrayList<SA> nestedSemanticActionList)
	{
		this.nestedSemanticActionList = nestedSemanticActionList;
	}
}
