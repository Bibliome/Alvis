<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

  <xsl:param name="embed">no</xsl:param>

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:template match="alvisae-schema">
    <xsl:choose>
      <xsl:when test="$embed = 'yes'">
	<object>
	  <pair key="schema">
	    <object>
	      <xsl:apply-templates select="*"/>
	    </object>
	  </pair>
	</object>
      </xsl:when>
      <xsl:otherwise>
	<object>
	  <xsl:apply-templates select="*"/>
	</object>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="text-bound">
    <pair>
      <xsl:attribute name="key">
	<xsl:value-of select="@type"/>
      </xsl:attribute>
      <object>
	<xsl:call-template name="common">
	  <xsl:with-param name="kind">0</xsl:with-param>
	  <xsl:with-param name="node" select="."/>
	</xsl:call-template>
	<pair key="txtBindingDef">
	  <object>
	    <pair key="minFrag"><raw><xsl:value-of select="@minFrag"/></raw></pair>
	    <pair key="maxFrag"><raw><xsl:value-of select="@maxFrag"/></raw></pair>
	    <pair key="boundRef">
	      <xsl:choose>
		<xsl:when test="@boundRef = ''">
		  <raw>null</raw>
		</xsl:when>
		<xsl:otherwise>
		  <quoted><xsl:value-of select="@boundRef"/></quoted>
		</xsl:otherwise>
	      </xsl:choose>
	    </pair>
	    <pair key="crossingAllowed"><raw><xsl:value-of select="@crossingAllowed"/></raw></pair>
	  </object>
	</pair>
      </object>
    </pair>
  </xsl:template>

  <xsl:template match="group">
    <pair>
      <xsl:attribute name="key">
	<xsl:value-of select="@type"/>
      </xsl:attribute>
      <object>
	<xsl:call-template name="common">
	  <xsl:with-param name="kind">1</xsl:with-param>
	  <xsl:with-param name="node" select="."/>
	</xsl:call-template>
	<pair key="groupDef">
	  <object>
	    <pair key="minComp"><raw><xsl:value-of select="@minComp"/></raw></pair>
	    <pair key="maxComp"><raw><xsl:value-of select="@maxComp"/></raw></pair>
	    <pair key="compType">
	      <array>
		<xsl:for-each select="component">
		  <quoted><xsl:value-of select="@type"/></quoted>
		</xsl:for-each>
	      </array>
	    </pair>
	    <pair key="homogeneous"><raw><xsl:value-of select="@homogeneous"/></raw></pair>
	  </object>
	</pair>
      </object>
    </pair>
  </xsl:template>

  <xsl:template match="relation">
    <pair>
      <xsl:attribute name="key">
	<xsl:value-of select="@type"/>
      </xsl:attribute>
      <object>
	<xsl:call-template name="common">
	  <xsl:with-param name="kind">2</xsl:with-param>
	  <xsl:with-param name="node" select="."/>
	</xsl:call-template>
	<pair key="relationDef">
	  <array>
	    <xsl:for-each select="argument">
	      <object>
		<pair>
		  <xsl:attribute name="key"><xsl:value-of select="@role"/></xsl:attribute>
		  <array>
		    <xsl:for-each select="type">
		      <quoted><xsl:value-of select="@id"/></quoted>
		    </xsl:for-each>
		  </array>
		</pair>
	      </object>
	    </xsl:for-each>
	  </array>
	</pair>
      </object>
    </pair>
  </xsl:template>

  <xsl:template match="property">
    <pair>
      <xsl:attribute name="key"><xsl:value-of select="@key"/></xsl:attribute>
      <object>
	<pair key="key"><quoted><xsl:value-of select="@key"/></quoted></pair>
	<pair key="mandatory"><raw><xsl:value-of select="@mandatory"/></raw></pair>
	<pair key="minVal"><raw><xsl:value-of select="@minVal"/></raw></pair>
	<pair key="maxVal"><raw><xsl:value-of select="@maxVal"/></raw></pair>
	<pair key="valType"><raw><xsl:value-of select="."/></raw></pair>
      </object>
    </pair>
  </xsl:template>

  <xsl:template name="common">
    <xsl:param name="kind"/>
    <xsl:param name="node"/>
    <pair key="kind"><raw><xsl:value-of select="$kind"/></raw></pair>
    <pair key="type"><quoted><xsl:value-of select="@type"/></quoted></pair>
    <pair key="color"><quoted><xsl:value-of select="@color"/></quoted></pair>
    <pair key="propDef">
      <object>
	<xsl:apply-templates select="property"/>
      </object>
    </pair>
  </xsl:template>
</xsl:stylesheet>
