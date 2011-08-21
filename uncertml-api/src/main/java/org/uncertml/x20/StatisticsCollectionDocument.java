/*
 * An XML document type.
 * Localname: StatisticsCollection
 * Namespace: http://www.uncertml.org/2.0
 * Java type: org.uncertml.x20.StatisticsCollectionDocument
 *
 * Automatically generated - do not modify.
 */
package org.uncertml.x20;


/**
 * A document containing one StatisticsCollection(@http://www.uncertml.org/2.0) element.
 *
 * This is a complex type.
 */
public interface StatisticsCollectionDocument extends org.uncertml.x20.AbstractSummaryStatisticDocument
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(StatisticsCollectionDocument.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s05A405BC14F7072A77DBB1861811F2C7").resolveHandle("statisticscollection59fadoctype");
    
    /**
     * Gets the "StatisticsCollection" element
     */
    org.uncertml.x20.StatisticsCollectionDocument.StatisticsCollection getStatisticsCollection();
    
    /**
     * Sets the "StatisticsCollection" element
     */
    void setStatisticsCollection(org.uncertml.x20.StatisticsCollectionDocument.StatisticsCollection statisticsCollection);
    
    /**
     * Appends and returns a new empty "StatisticsCollection" element
     */
    org.uncertml.x20.StatisticsCollectionDocument.StatisticsCollection addNewStatisticsCollection();
    
    /**
     * An XML StatisticsCollection(@http://www.uncertml.org/2.0).
     *
     * This is a complex type.
     */
    public interface StatisticsCollection extends org.uncertml.x20.StatisticsCollectionType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(StatisticsCollection.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s05A405BC14F7072A77DBB1861811F2C7").resolveHandle("statisticscollectionb1fdelemtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static org.uncertml.x20.StatisticsCollectionDocument.StatisticsCollection newInstance() {
              return (org.uncertml.x20.StatisticsCollectionDocument.StatisticsCollection) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static org.uncertml.x20.StatisticsCollectionDocument.StatisticsCollection newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (org.uncertml.x20.StatisticsCollectionDocument.StatisticsCollection) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.uncertml.x20.StatisticsCollectionDocument newInstance() {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.uncertml.x20.StatisticsCollectionDocument parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.uncertml.x20.StatisticsCollectionDocument parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.uncertml.x20.StatisticsCollectionDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.uncertml.x20.StatisticsCollectionDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.uncertml.x20.StatisticsCollectionDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.uncertml.x20.StatisticsCollectionDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}