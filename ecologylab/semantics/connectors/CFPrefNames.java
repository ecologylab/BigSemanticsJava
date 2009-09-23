package ecologylab.semantics.connectors;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.appframework.types.prefs.PrefBoolean;

/**
 * combinFormation-specific set of reusable String constants for getting properties from the environment.
 * 
 * KEEP all actual parameter calls out of here, because this gets loaded before
 * properties do, and they will get stuck at null for that reason!!!
 * BEWARE!
 * <p/>
 * The golden rule here is: "Do not define my constants, before my properties are loaded!"
 * 
 * @author andruid
 * @author blake
 */

public interface CFPrefNames
{
	public static final String	SEED_SET				= "seed_set";

	public static final String	CF_APPLICATION_NAME		= "combinFormation";
	
	public static final String	DISPLAY_STATUS			= "display_status";
	
	public static final String  ENABLE_INCONTEXT_SLIDER = "incontext_slider";
	
	public static final String	APPLICATION_ENVIRONMENT	= "application_environment";

	public static final String	DASHBOARD_ENABLED_NAME	= "dashboard_enabled";
	
	public static final String	DASHBOARD_NAME			= "dashboard";
	
	public static final String	SEED_VISIBILITY_CLOSED 	= "closed";

	/**
	 * The name of the user interface currently in use, and its path in /config/interface.
	 */
	public static final String 	DEFAULT_INTERFACE  		= "in_context_interface";

	public static final String  USERINTERFACE_PREF_NAME		= "userinterface";

	public static String 		INFO_EXTRACTION_PARAM	= "info_extraction_method";
	public static int 			OLD_UNSTRUCTURED_EXTRACTION		= 0;
	public static int 			EUNYEE_STRUCTURED_EXTRACTION	= 1;

	/**
	 * The reduced interface for non-generative cF.
	 */
	public static final String REDUCED = "reduced";

	public static final String		CURATED			= "curated";
	
	
	public static final String 	IGNORE_PDF			= "ignore_pdf";
	
	public static final String	STUDY_COMPOSITION_SAVE_LOCATION	= "study_composition_save_location";
	
	public static final String	DECAY_INTEREST  = "decay_interest";
}
