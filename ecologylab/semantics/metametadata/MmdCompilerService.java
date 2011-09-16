package ecologylab.semantics.metametadata;

import java.io.IOException;
import java.util.List;

public interface MmdCompilerService
{

	void appendAnnotation(Appendable appendable, String indent, Class annotationClass, boolean quoted, String params) throws IOException;
	
	void appendAnnotation(Appendable appendable, String indent, Class annotationClass) throws IOException;
	
	void appendAnnotation(Appendable appendable, String indent, Class annotationClass, boolean quoted, List<String> params) throws IOException;
	
}
