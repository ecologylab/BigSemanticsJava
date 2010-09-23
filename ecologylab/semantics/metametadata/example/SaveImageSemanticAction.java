package ecologylab.semantics.metametadata.example;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * This semantic action is used to save weather images to local drive, as an example of reusing
 * existing semantic actions. Note that by using @xml_tag with the same name, we can override
 * default implementation of this semantic action (need to use SemanticAction.register() to register
 * it).
 * 
 * @author quyin
 *
 */
@simpl_inherit
@xml_tag("create_and_visualize_img_surrogate")
public class SaveImageSemanticAction extends SemanticAction
{

	@Override
	public String getActionName()
	{
		return "create_and_visualize_img_surrogate";
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object perform(Object obj)
	{
		/*
		 * use getArgument*() methods to retrieve argument values. note that getArgumentValue() allows
		 * you retrieve the literal in meta-metadata XML. other getArgument*() methods will use that
		 * literal as the name and look up corresponding objects in the semantic action environment.
		 * you may need to use <get_field> or something else to put metadata field values into that
		 * environment first.
		 */
		String title = (String) getArgumentObject("title");
		ParsedURL url = (ParsedURL) getArgumentObject("image_url");
		
		downloadImage(title, url);
		return null;
	}

	private void downloadImage(String title, ParsedURL url)
	{
		if (title == null || url == null)
			return;
		
		try
		{
			BufferedInputStream bis = new BufferedInputStream(url.connect().inputStream());
			FileOutputStream fos = new FileOutputStream(title.replaceAll("[^\\w\\d]", "_") + ".gif");
			byte[] buffer = new byte[65536];
			int count = 0;
			while ((count = bis.read(buffer)) >= 0)
			{
				fos.write(buffer, 0, count);
			}
			bis.close();
			fos.close();
		}
		catch (MalformedURLException e)
		{
			error("Illegal URL: " + url + ";\nError message: " + e.getMessage());
		}
		catch (IOException e)
		{
			error("I/O error: " + e.getMessage());
		}
	}
	
}
