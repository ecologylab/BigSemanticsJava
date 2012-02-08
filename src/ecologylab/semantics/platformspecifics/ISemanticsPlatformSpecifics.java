package ecologylab.semantics.platformspecifics;

/**
 * an interface intended on structuring platform specific dependencies into different projects
 * 
 * @author shenfeng
 * 
 */

public interface ISemanticsPlatformSpecifics
{
	Object getStrokeColor(int generation, int maxgeneration, Object[] strokecolor, float strokehue);
}
