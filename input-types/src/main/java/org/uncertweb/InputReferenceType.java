/*
 * XML Type:  InputReferenceType
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.InputReferenceType
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb;


/**
 * An XML InputReferenceType(@http://www.uncertweb.org).
 *
 * This is a complex type.
 */
public interface InputReferenceType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(InputReferenceType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s33CE46A20214C313208096452ACC64DA").resolveHandle("inputreferencetype8780type");
    
    /**
     * Gets array of all "Header" elements
     */
    org.uncertweb.InputReferenceType.Header[] getHeaderArray();
    
    /**
     * Gets ith "Header" element
     */
    org.uncertweb.InputReferenceType.Header getHeaderArray(int i);
    
    /**
     * Returns number of "Header" element
     */
    int sizeOfHeaderArray();
    
    /**
     * Sets array of all "Header" element
     */
    void setHeaderArray(org.uncertweb.InputReferenceType.Header[] headerArray);
    
    /**
     * Sets ith "Header" element
     */
    void setHeaderArray(int i, org.uncertweb.InputReferenceType.Header header);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "Header" element
     */
    org.uncertweb.InputReferenceType.Header insertNewHeader(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "Header" element
     */
    org.uncertweb.InputReferenceType.Header addNewHeader();
    
    /**
     * Removes the ith "Header" element
     */
    void removeHeader(int i);
    
    /**
     * Gets the "Body" element
     */
    org.apache.xmlbeans.XmlObject getBody();
    
    /**
     * True if has "Body" element
     */
    boolean isSetBody();
    
    /**
     * Sets the "Body" element
     */
    void setBody(org.apache.xmlbeans.XmlObject body);
    
    /**
     * Appends and returns a new empty "Body" element
     */
    org.apache.xmlbeans.XmlObject addNewBody();
    
    /**
     * Unsets the "Body" element
     */
    void unsetBody();
    
    /**
     * Gets the "BodyReference" element
     */
    org.uncertweb.InputReferenceType.BodyReference getBodyReference();
    
    /**
     * True if has "BodyReference" element
     */
    boolean isSetBodyReference();
    
    /**
     * Sets the "BodyReference" element
     */
    void setBodyReference(org.uncertweb.InputReferenceType.BodyReference bodyReference);
    
    /**
     * Appends and returns a new empty "BodyReference" element
     */
    org.uncertweb.InputReferenceType.BodyReference addNewBodyReference();
    
    /**
     * Unsets the "BodyReference" element
     */
    void unsetBodyReference();
    
    /**
     * Gets the "href" attribute
     */
    java.lang.String getHref();
    
    /**
     * Gets (as xml) the "href" attribute
     */
    org.apache.xmlbeans.XmlAnyURI xgetHref();
    
    /**
     * Sets the "href" attribute
     */
    void setHref(java.lang.String href);
    
    /**
     * Sets (as xml) the "href" attribute
     */
    void xsetHref(org.apache.xmlbeans.XmlAnyURI href);
    
    /**
     * Gets the "method" attribute
     */
    org.uncertweb.InputReferenceType.Method.Enum getMethod();
    
    /**
     * Gets (as xml) the "method" attribute
     */
    org.uncertweb.InputReferenceType.Method xgetMethod();
    
    /**
     * True if has "method" attribute
     */
    boolean isSetMethod();
    
    /**
     * Sets the "method" attribute
     */
    void setMethod(org.uncertweb.InputReferenceType.Method.Enum method);
    
    /**
     * Sets (as xml) the "method" attribute
     */
    void xsetMethod(org.uncertweb.InputReferenceType.Method method);
    
    /**
     * Unsets the "method" attribute
     */
    void unsetMethod();
    
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
     * An XML Header(@http://www.uncertweb.org).
     *
     * This is a complex type.
     */
    public interface Header extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Header.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s33CE46A20214C313208096452ACC64DA").resolveHandle("headerb509elemtype");
        
        /**
         * Gets the "key" attribute
         */
        java.lang.String getKey();
        
        /**
         * Gets (as xml) the "key" attribute
         */
        org.apache.xmlbeans.XmlString xgetKey();
        
        /**
         * Sets the "key" attribute
         */
        void setKey(java.lang.String key);
        
        /**
         * Sets (as xml) the "key" attribute
         */
        void xsetKey(org.apache.xmlbeans.XmlString key);
        
        /**
         * Gets the "value" attribute
         */
        java.lang.String getValue();
        
        /**
         * Gets (as xml) the "value" attribute
         */
        org.apache.xmlbeans.XmlString xgetValue();
        
        /**
         * Sets the "value" attribute
         */
        void setValue(java.lang.String value);
        
        /**
         * Sets (as xml) the "value" attribute
         */
        void xsetValue(org.apache.xmlbeans.XmlString value);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static org.uncertweb.InputReferenceType.Header newInstance() {
              return (org.uncertweb.InputReferenceType.Header) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static org.uncertweb.InputReferenceType.Header newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (org.uncertweb.InputReferenceType.Header) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML BodyReference(@http://www.uncertweb.org).
     *
     * This is a complex type.
     */
    public interface BodyReference extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(BodyReference.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s33CE46A20214C313208096452ACC64DA").resolveHandle("bodyreferencee4d5elemtype");
        
        /**
         * Gets the "href" attribute
         */
        java.lang.String getHref();
        
        /**
         * Gets (as xml) the "href" attribute
         */
        org.apache.xmlbeans.XmlAnyURI xgetHref();
        
        /**
         * Sets the "href" attribute
         */
        void setHref(java.lang.String href);
        
        /**
         * Sets (as xml) the "href" attribute
         */
        void xsetHref(org.apache.xmlbeans.XmlAnyURI href);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static org.uncertweb.InputReferenceType.BodyReference newInstance() {
              return (org.uncertweb.InputReferenceType.BodyReference) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static org.uncertweb.InputReferenceType.BodyReference newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (org.uncertweb.InputReferenceType.BodyReference) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML method(@).
     *
     * This is an atomic type that is a restriction of org.uncertweb.InputReferenceType$Method.
     */
    public interface Method extends org.apache.xmlbeans.XmlString
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Method.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s33CE46A20214C313208096452ACC64DA").resolveHandle("method01c1attrtype");
        
        org.apache.xmlbeans.StringEnumAbstractBase enumValue();
        void set(org.apache.xmlbeans.StringEnumAbstractBase e);
        
        static final Enum GET = Enum.forString("GET");
        static final Enum POST = Enum.forString("POST");
        
        static final int INT_GET = Enum.INT_GET;
        static final int INT_POST = Enum.INT_POST;
        
        /**
         * Enumeration value class for org.uncertweb.InputReferenceType$Method.
         * These enum values can be used as follows:
         * <pre>
         * enum.toString(); // returns the string value of the enum
         * enum.intValue(); // returns an int value, useful for switches
         * // e.g., case Enum.INT_GET
         * Enum.forString(s); // returns the enum value for a string
         * Enum.forInt(i); // returns the enum value for an int
         * </pre>
         * Enumeration objects are immutable singleton objects that
         * can be compared using == object equality. They have no
         * public constructor. See the constants defined within this
         * class for all the valid values.
         */
        static final class Enum extends org.apache.xmlbeans.StringEnumAbstractBase
        {
            /**
             * Returns the enum value for a string, or null if none.
             */
            public static Enum forString(java.lang.String s)
                { return (Enum)table.forString(s); }
            /**
             * Returns the enum value corresponding to an int, or null if none.
             */
            public static Enum forInt(int i)
                { return (Enum)table.forInt(i); }
            
            private Enum(java.lang.String s, int i)
                { super(s, i); }
            
            static final int INT_GET = 1;
            static final int INT_POST = 2;
            
            public static final org.apache.xmlbeans.StringEnumAbstractBase.Table table =
                new org.apache.xmlbeans.StringEnumAbstractBase.Table
            (
                new Enum[]
                {
                    new Enum("GET", INT_GET),
                    new Enum("POST", INT_POST),
                }
            );
            private static final long serialVersionUID = 1L;
            private java.lang.Object readResolve() { return forInt(intValue()); } 
        }
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static org.uncertweb.InputReferenceType.Method newValue(java.lang.Object obj) {
              return (org.uncertweb.InputReferenceType.Method) type.newValue( obj ); }
            
            public static org.uncertweb.InputReferenceType.Method newInstance() {
              return (org.uncertweb.InputReferenceType.Method) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static org.uncertweb.InputReferenceType.Method newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (org.uncertweb.InputReferenceType.Method) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.uncertweb.InputReferenceType newInstance() {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.uncertweb.InputReferenceType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.uncertweb.InputReferenceType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.uncertweb.InputReferenceType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.uncertweb.InputReferenceType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.uncertweb.InputReferenceType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.uncertweb.InputReferenceType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.uncertweb.InputReferenceType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.uncertweb.InputReferenceType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.uncertweb.InputReferenceType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.uncertweb.InputReferenceType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.uncertweb.InputReferenceType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.uncertweb.InputReferenceType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.uncertweb.InputReferenceType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.uncertweb.InputReferenceType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.uncertweb.InputReferenceType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.uncertweb.InputReferenceType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.uncertweb.InputReferenceType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.uncertweb.InputReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
