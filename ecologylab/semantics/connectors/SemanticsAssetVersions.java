/**
 * 
 */
package ecologylab.semantics.connectors;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.appframework.EnvironmentGeneric;
import ecologylab.io.AssetsRoot;

/**
 * Semantics specific asset version numbers.
 * 
 * @author andruid
 * @author abhinav
 */
public interface SemanticsAssetVersions
{
	public static final float	DICTIONARY_ASSET_VERSION 	    = 2.122f;
	
	public static final float 	METAMETADATA_ASSET_VERSION		= 2.686f;
	
	public static final AssetsRoot	SEMANTICS_ASSETS_ROOT =  new AssetsRoot("semantics/", ApplicationEnvironment.runningInEclipse() ? EnvironmentGeneric.configDir().file() : null);
}
