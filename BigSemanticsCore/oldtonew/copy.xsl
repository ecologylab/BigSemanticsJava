<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:redirect="http://xml.apache.org/xalan/redirect"
    extension-element-prefixes="redirect"
    version="1.0">
    
<xsl:output method="xml" standalone="no" encoding="ISO-8859-1"/>

<!-- The main template loops over all of the
    classes and creates a file for each one --> 

<xsl:template match="/">

	<xsl:variable name="filename" select="concat('oldxmldirectory/','copynews_new','.xml')" />
	<xsl:message>Creating
            <xsl:value-of select="$filename" /></xsl:message>
    <redirect:write file="{$filename}">
    	<!-- Identity transform, passes nodes through untouched -->
		<xsl:template match="@*|node()|text()">
 			<xsl:copy>
   				<xsl:copy-of select="@*|node()"/>
 			</xsl:copy>
		</xsl:template>
    </redirect:write>        
</xsl:template>



</xsl:stylesheet>