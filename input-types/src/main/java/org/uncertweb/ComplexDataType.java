/*
 * XML Type:  ComplexDataType
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.ComplexDataType
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb;


/**
 * An XML ComplexDataType(@http://www.uncertweb.org).
 *
 * This is a complex type.
 */
public interface ComplexDataType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(ComplexDataType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s33CE46A20214C313208096452ACC64DA").resolveHandle("complexdatatyped7f9type");

    /**
     * Gets the "mimeType" attribute
     */
    java.lang.String getMimeType();

    /**
     * Gets (as xml) the "mimeType" attribute
     */
    net.opengis.ows.x11.MimeType xgetMimeType();

    /**
     * True if has "mimeType" attribute
     */
    boolean isSetMimeType();

    /**
     * Sets the "mimeType" attribute
     */
    void setMimeType(java.lang.String mimeType);

    /**
     * Sets (as xml) the "mimeType" attribute
     */
    void xsetMimeType(net.opengis.ows.x11.MimeType mimeType);

    /**
     * Unsets the "mimeType" attribute
     */
    void unsetMimeType();

    /**
     * Gets the "encoding" attribute
     */
    java.lang.String getEncoding();

    /**
     * Gets (as xml) the "encoding" attribute
     */
    org.apache.xmlbeans.XmlAnyURI xgetEncoding();

    /**
     * True if has "encoding" attribute
     */
    boolean isSetEncoding();

    /**
     * Sets the "encoding" attribute
     */
    void setEncoding(java.lang.String encoding);

    /**
     * Sets (as xml) the "encoding" attribute
     */
    void xsetEncoding(org.apache.xmlbeans.XmlAnyURI encoding);

    /**
     * Unsets the "encoding" attribute
     */
    void unsetEncoding();

    /**
     * Gets the "schema" attribute
     */
    java.lang.String getSchema();

    /**
     * Gets (as xml) the "schema" attribute
     */
    org.apache.xmlbeans.XmlAnyURI xgetSchema();

    /**
     * True if has "schema" attribute
     */
    boolean isSetSchema();

    /**
     * Sets the "schema" attribute
     */
    void setSchema(java.lang.String schema);

    /**
     * Sets (as xml) the "schema" attribute
     */
    void xsetSchema(org.apache.xmlbeans.XmlAnyURI schema);

    /**
     * Unsets the "schema" attribute
     */
    void unsetSchema();

    /**
     * A factory class with static methods for creating instances
     * of this type.
     */

    public static final class Factory
    {
        public static org.uncertweb.ComplexDataType newInstance() {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }

        public static org.uncertweb.ComplexDataType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }

        /** @param xmlAsString the string value to parse */
        public static org.uncertweb.ComplexDataType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }

        public static org.uncertweb.ComplexDataType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }

        /** @param file the file from which to load an xml document */
        public static org.uncertweb.ComplexDataType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }

        public static org.uncertweb.ComplexDataType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }

        public static org.uncertweb.ComplexDataType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }

        public static org.uncertweb.ComplexDataType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }

        public static org.uncertweb.ComplexDataType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }

        public static org.uncertweb.ComplexDataType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }

        public static org.uncertweb.ComplexDataType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }

        public static org.uncertweb.ComplexDataType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }

        public static org.uncertweb.ComplexDataType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }

        public static org.uncertweb.ComplexDataType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }

        public static org.uncertweb.ComplexDataType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }

        public static org.uncertweb.ComplexDataType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }

        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.uncertweb.ComplexDataType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }

        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.uncertweb.ComplexDataType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.uncertweb.ComplexDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }

        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }

        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }

        private Factory() { } // No instance of this class allowed
    }
}
