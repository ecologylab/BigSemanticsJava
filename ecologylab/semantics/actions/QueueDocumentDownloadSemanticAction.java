package ecologylab.semantics.actions;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
public @xml_tag(SemanticActionStandardMethods.QUEUE_DOCUMENT_DOWNLOAD)
class QueueDocumentDownloadSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return QUEUE_DOCUMENT_DOWNLOAD;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

}
