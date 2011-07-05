/*
 * XML Type:  AbstractInputType
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.AbstractInputType
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb;


/**
 * An XML AbstractInputType(@http://www.uncertweb.org).
 *
 * This is a complex type.
 */
public interface AbstractInputType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(AbstractInputType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s33CE46A20214C313208096452ACC64DA").resolveHandle("abstractinputtype182btype");
    
    /**
     * Gets the "Identifier" element
     */
    net.opengis.ows.x11.CodeType getIdentifier();
    
    /**
     * Sets the "Identifier" element
     */
    void setIdentifier(net.opengis.ows.x11.CodeType identifier);
    
    /**
     * Appends and returns a new empty "Identifier" element
     */
    net.opengis.ows.x11.CodeType addNewIdentifier();
    
    /**
     * Gets the "Title" element
     */
    net.opengis.ows.x11.LanguageStringType getTitle();
    
    /**
     * True if has "Title" element
     */
    boolean isSetTitle();
    
    /**
     * Sets the "Title" element
     */
    void setTitle(net.opengis.ows.x11.LanguageStringType title);
    
    /**
     * Appends and returns a new empty "Title" element
     */
    net.opengis.ows.x11.LanguageStringType addNewTitle();
    
    /**
     * Unsets the "Title" element
     */
    void unsetTitle();
    
    /**
     * Gets the "Abstract" element
     */
    net.opengis.ows.x11.LanguageStringType getAbstract();
    
    /**
     * True if has "Abstract" element
     */
    boolean isSetAbstract();
    
    /**
     * Sets the "Abstract" element
     */
    void setAbstract(net.opengis.ows.x11.LanguageStringType xabstract);
    
    /**
     * Appends and returns a new empty "Abstract" element
     */
    net.opengis.ows.x11.LanguageStringType addNewAbstract();
    
    /**
     * Unsets the "Abstract" element
     */
    void unsetAbstract();
    
    /**
     * Gets the "Reference" element
     */
    net.opengis.wps.x100.InputReferenceType getReference();
    
    /**
     * True if has "Reference" element
     */
    boolean isSetReference();
    
    /**
     * Sets the "Reference" element
     */
    void setReference(net.opengis.wps.x100.InputReferenceType reference);
    
    /**
     * Appends and returns a new empty "Reference" element
     */
    net.opengis.wps.x100.InputReferenceType addNewReference();
    
    /**
     * Unsets the "Reference" element
     */
    void unsetReference();
    
    /**
     * Gets the "Data" element
     */
    net.opengis.wps.x100.DataType getData();
    
    /**
     * True if has "Data" element
     */
    boolean isSetData();
    
    /**
     * Sets the "Data" element
     */
    void setData(net.opengis.wps.x100.DataType data);
    
    /**
     * Appends and returns a new empty "Data" element
     */
    net.opengis.wps.x100.DataType addNewData();
    
    /**
     * Unsets the "Data" element
     */
    void unsetData();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        /** @deprecated No need to be able to create instances of abstract types */
        public static org.uncertweb.AbstractInputType newInstance() {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        /** @deprecated No need to be able to create instances of abstract types */
        public static org.uncertweb.AbstractInputType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.uncertweb.AbstractInputType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.uncertweb.AbstractInputType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.uncertweb.AbstractInputType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.uncertweb.AbstractInputType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.uncertweb.AbstractInputType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.uncertweb.AbstractInputType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.uncertweb.AbstractInputType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.uncertweb.AbstractInputType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.uncertweb.AbstractInputType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.uncertweb.AbstractInputType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.uncertweb.AbstractInputType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.uncertweb.AbstractInputType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.uncertweb.AbstractInputType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.uncertweb.AbstractInputType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.uncertweb.AbstractInputType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.uncertweb.AbstractInputType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.uncertweb.AbstractInputType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
