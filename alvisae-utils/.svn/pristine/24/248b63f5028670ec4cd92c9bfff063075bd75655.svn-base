<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

  <xsl:output method="text" encoding="UTF-8"/>

  <xsl:template match="raw">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="quoted">
    <xsl:text>"</xsl:text><xsl:value-of select="."/><xsl:text>"</xsl:text>
  </xsl:template>

  <xsl:template match="array">
    <xsl:text>[</xsl:text>
    <xsl:for-each select="*">
      <xsl:if test="position() != 1">,</xsl:if>
      <xsl:apply-templates select="."/>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
  </xsl:template>

  <xsl:template match="object">
    <xsl:text>{</xsl:text>
    <xsl:for-each select="pair">
      <xsl:if test="position() != 1">,</xsl:if>
      <xsl:apply-templates select="."/>
    </xsl:for-each>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="pair">
    <xsl:text>"</xsl:text><xsl:value-of select="@key"/><xsl:text>":</xsl:text>
    <xsl:apply-templates select="*"/>
  </xsl:template>

</xsl:stylesheet>
