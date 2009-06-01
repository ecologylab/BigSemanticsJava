package ecologylab.semantics.html.documentstructure;

import ecologylab.net.MimeType;

public interface ImageConstants
extends MimeType
{

	public static final int	SPACER_MAX_DIM		= 23;

	public static final int	SPACER_MAX_AREA		= SPACER_MAX_DIM * SPACER_MAX_DIM;

	public static final int	MIN_WIDTH					= 30;

	public static final int	MIN_HEIGHT				= 25;

	// +++++++++++ design roles +++++++++++ //
	public static final int		UNKNOWN					= 0;

	public static final int		UN_INFORMATIVE		= -1;

	public static final int		INFORMATIVE			= 1;

	
	// +++++++++++++++ deprecated ++++++++++++++++ //

	public static final int	MIN_AREA					= MIN_HEIGHT * MIN_WIDTH;

	public static final int	NAV_MAX_HEIGHT		= 60;

	public static final int	TALL_AD_MAX_WIDTH	= 200;
	
	public static final int		SPACER					= 2;

	public static final int		HEADER					= 3;

	public static final int		ICON						= 4;

	public static final int		TOO_SMALL				= 5;

	public static final int		ANNOTATION			= 20;


}