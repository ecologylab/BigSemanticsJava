<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:redirect="http://xml.apache.org/xalan/redirect"
	extension-element-prefixes="redirect" version="1.0">
	<xsl:param name="newFile"/>

	<xsl:output method="xml" standalone="no" encoding="ISO-8859-1" />

	<xsl:template match="/">
		<!--xsl:variable name="filename"
			select="concat('oldxmldirectory/','Latest_news_new','.xml')" /-->
		<xsl:variable name="filename"
			select="concat('oldxmldirectory/',$newFile)" />
		<xsl:message>
			Creating
			<xsl:value-of select="$filename" />
		</xsl:message>
		<redirect:write file="{$filename}">
			<xsl:apply-templates />
		</redirect:write>
	</xsl:template>
	<!--redirect:write file="Latest_news_new.xml"-->
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="rss|image">
			<xsl:element name="{name()}">
				<xsl:for-each select="@*">
					<xsl:message>Attribute</xsl:message>
					<xsl:element name="{name()}">
						<xsl:value-of select="." />
					</xsl:element>
					<!--xsl:text>&#xa;</xsl:text-->
				</xsl:for-each>
			</xsl:element>
		</xsl:template>
	<!--/redirect:write-->
</xsl:stylesheet>