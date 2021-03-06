/**
 * 
 */
package ecologylab.bigsemantics.namesandnums;

import ecologylab.appframework.EnvironmentGeneric;
import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.io.AssetsRoot;

/**
 * Semantics specific asset version numbers.
 * 
 * @author andruid
 * @author abhinav
 */
@Deprecated
public interface SemanticsAssetVersions
{
	public static final float	DICTIONARY_ASSET_VERSION 	    = 2.123f;
	
	public static final float 	METAMETADATA_ASSET_VERSION		= 2.90f;
	
	public static final AssetsRoot	SEMANTICS_ASSETS_ROOT =  new AssetsRoot("", SingletonApplicationEnvironment.runningInEclipse() ? EnvironmentGeneric.configDir().file() : null);
}
