/*
 * XML Type:  InputReferenceType
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.InputReferenceType
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb.impl;
/**
 * An XML InputReferenceType(@http://www.uncertweb.org).
 *
 * This is a complex type.
 */
public class InputReferenceTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.uncertweb.InputReferenceType
{
    private static final long serialVersionUID = 1L;
    
    public InputReferenceTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HEADER$0 = 
        new javax.xml.namespace.QName("http://www.uncertweb.org", "Header");
    private static final javax.xml.namespace.QName BODY$2 = 
        new javax.xml.namespace.QName("http://www.uncertweb.org", "Body");
    private static final javax.xml.namespace.QName BODYREFERENCE$4 = 
        new javax.xml.namespace.QName("http://www.uncertweb.org", "BodyReference");
    private static final javax.xml.namespace.QName HREF$6 = 
        new javax.xml.namespace.QName("http://www.w3.org/1999/xlink", "href");
    private static final javax.xml.namespace.QName METHOD$8 = 
        new javax.xml.namespace.QName("", "method");
    private static final javax.xml.namespace.QName MIMETYPE$10 = 
        new javax.xml.namespace.QName("", "mimeType");
    private static final javax.xml.namespace.QName ENCODING$12 = 
        new javax.xml.namespace.QName("", "encoding");
    private static final javax.xml.namespace.QName SCHEMA$14 = 
        new javax.xml.namespace.QName("", "schema");
    
    
    /**
     * Gets array of all "Header" elements
     */
    public org.uncertweb.InputReferenceType.Header[] getHeaderArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(HEADER$0, targetList);
            org.uncertweb.InputReferenceType.Header[] result = new org.uncertweb.InputReferenceType.Header[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "Header" element
     */
    public org.uncertweb.InputReferenceType.Header getHeaderArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.InputReferenceType.Header target = null;
            target = (org.uncertweb.InputReferenceType.Header)get_store().find_element_user(HEADER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "Header" element
     */
    public int sizeOfHeaderArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(HEADER$0);
        }
    }
    
    /**
     * Sets array of all "Header" element
     */
    public void setHeaderArray(org.uncertweb.InputReferenceType.Header[] headerArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(headerArray, HEADER$0);
        }
    }
    
    /**
     * Sets ith "Header" element
     */
    public void setHeaderArray(int i, org.uncertweb.InputReferenceType.Header header)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.InputReferenceType.Header target = null;
            target = (org.uncertweb.InputReferenceType.Header)get_store().find_element_user(HEADER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(header);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "Header" element
     */
    public org.uncertweb.InputReferenceType.Header insertNewHeader(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.InputReferenceType.Header target = null;
            target = (org.uncertweb.InputReferenceType.Header)get_store().insert_element_user(HEADER$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "Header" element
     */
    public org.uncertweb.InputReferenceType.Header addNewHeader()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.InputReferenceType.Header target = null;
            target = (org.uncertweb.InputReferenceType.Header)get_store().add_element_user(HEADER$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "Header" element
     */
    public void removeHeader(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(HEADER$0, i);
        }
    }
    
    /**
     * Gets the "Body" element
     */
    public org.apache.xmlbeans.XmlObject getBody()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(BODY$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "Body" element
     */
    public boolean isSetBody()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(BODY$2) != 0;
        }
    }
    
    /**
     * Sets the "Body" element
     */
    public void setBody(org.apache.xmlbeans.XmlObject body)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(BODY$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(BODY$2);
            }
            target.set(body);
        }
    }
    
    /**
     * Appends and returns a new empty "Body" element
     */
    public org.apache.xmlbeans.XmlObject addNewBody()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(BODY$2);
            return target;
        }
    }
    
    /**
     * Unsets the "Body" element
     */
    public void unsetBody()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(BODY$2, 0);
        }
    }
    
    /**
     * Gets the "BodyReference" element
     */
    public org.uncertweb.InputReferenceType.BodyReference getBodyReference()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.InputReferenceType.BodyReference target = null;
            target = (org.uncertweb.InputReferenceType.BodyReference)get_store().find_element_user(BODYREFERENCE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "BodyReference" element
     */
    public boolean isSetBodyReference()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(BODYREFERENCE$4) != 0;
        }
    }
    
    /**
     * Sets the "BodyReference" element
     */
    public void setBodyReference(org.uncertweb.InputReferenceType.BodyReference bodyReference)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.InputReferenceType.BodyReference target = null;
            target = (org.uncertweb.InputReferenceType.BodyReference)get_store().find_element_user(BODYREFERENCE$4, 0);
            if (target == null)
            {
                target = (org.uncertweb.InputReferenceType.BodyReference)get_store().add_element_user(BODYREFERENCE$4);
            }
            target.set(bodyReference);
        }
    }
    
    /**
     * Appends and returns a new empty "BodyReference" element
     */
    public org.uncertweb.InputReferenceType.BodyReference addNewBodyReference()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.InputReferenceType.BodyReference target = null;
            target = (org.uncertweb.InputReferenceType.BodyReference)get_store().add_element_user(BODYREFERENCE$4);
            return target;
        }
    }
    
    /**
     * Unsets the "BodyReference" element
     */
    public void unsetBodyReference()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(BODYREFERENCE$4, 0);
        }
    }
    
    /**
     * Gets the "href" attribute
     */
    public java.lang.String getHref()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(HREF$6);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "href" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetHref()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(HREF$6);
            return target;
        }
    }
    
    /**
     * Sets the "href" attribute
     */
    public void setHref(java.lang.String href)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(HREF$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(HREF$6);
            }
            target.setStringValue(href);
        }
    }
    
    /**
     * Sets (as xml) the "href" attribute
     */
    public void xsetHref(org.apache.xmlbeans.XmlAnyURI href)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(HREF$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(HREF$6);
            }
            target.set(href);
        }
    }
    
    /**
     * Gets the "method" attribute
     */
    public org.uncertweb.InputReferenceType.Method.Enum getMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(METHOD$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_default_attribute_value(METHOD$8);
            }
            if (target == null)
            {
                return null;
            }
            return (org.uncertweb.InputReferenceType.Method.Enum)target.getEnumValue();
        }
    }
    
    /**
     * Gets (as xml) the "method" attribute
     */
    public org.uncertweb.InputReferenceType.Method xgetMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.InputReferenceType.Method target = null;
            target = (org.uncertweb.InputReferenceType.Method)get_store().find_attribute_user(METHOD$8);
            if (target == null)
            {
                target = (org.uncertweb.InputReferenceType.Method)get_default_attribute_value(METHOD$8);
            }
            return target;
        }
    }
    
    /**
     * True if has "method" attribute
     */
    public boolean isSetMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(METHOD$8) != null;
        }
    }
    
    /**
     * Sets the "method" attribute
     */
    public void setMethod(org.uncertweb.InputReferenceType.Method.Enum method)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(METHOD$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(METHOD$8);
            }
            target.setEnumValue(method);
        }
    }
    
    /**
     * Sets (as xml) the "method" attribute
     */
    public void xsetMethod(org.uncertweb.InputReferenceType.Method method)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.InputReferenceType.Method target = null;
            target = (org.uncertweb.InputReferenceType.Method)get_store().find_attribute_user(METHOD$8);
            if (target == null)
            {
                target = (org.uncertweb.InputReferenceType.Method)get_store().add_attribute_user(METHOD$8);
            }
            target.set(method);
        }
    }
    
    /**
     * Unsets the "method" attribute
     */
    public void unsetMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(METHOD$8);
        }
    }
    
    /**
     * Gets the "mimeType" attribute
     */
    public java.lang.String getMimeType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MIMETYPE$10);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "mimeType" attribute
     */
    public net.opengis.ows.x11.MimeType xgetMimeType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.MimeType target = null;
            target = (net.opengis.ows.x11.MimeType)get_store().find_attribute_user(MIMETYPE$10);
            return target;
        }
    }
    
    /**
     * True if has "mimeType" attribute
     */
    public boolean isSetMimeType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(MIMETYPE$10) != null;
        }
    }
    
    /**
     * Sets the "mimeType" attribute
     */
    public void setMimeType(java.lang.String mimeType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MIMETYPE$10);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(MIMETYPE$10);
            }
            target.setStringValue(mimeType);
        }
    }
    
    /**
     * Sets (as xml) the "mimeType" attribute
     */
    public void xsetMimeType(net.opengis.ows.x11.MimeType mimeType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.MimeType target = null;
            target = (net.opengis.ows.x11.MimeType)get_store().find_attribute_user(MIMETYPE$10);
            if (target == null)
            {
                target = (net.opengis.ows.x11.MimeType)get_store().add_attribute_user(MIMETYPE$10);
            }
            target.set(mimeType);
        }
    }
    
    /**
     * Unsets the "mimeType" attribute
     */
    public void unsetMimeType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(MIMETYPE$10);
        }
    }
    
    /**
     * Gets the "encoding" attribute
     */
    public java.lang.String getEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ENCODING$12);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "encoding" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ENCODING$12);
            return target;
        }
    }
    
    /**
     * True if has "encoding" attribute
     */
    public boolean isSetEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(ENCODING$12) != null;
        }
    }
    
    /**
     * Sets the "encoding" attribute
     */
    public void setEncoding(java.lang.String encoding)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ENCODING$12);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ENCODING$12);
            }
            target.setStringValue(encoding);
        }
    }
    
    /**
     * Sets (as xml) the "encoding" attribute
     */
    public void xsetEncoding(org.apache.xmlbeans.XmlAnyURI encoding)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ENCODING$12);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(ENCODING$12);
            }
            target.set(encoding);
        }
    }
    
    /**
     * Unsets the "encoding" attribute
     */
    public void unsetEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(ENCODING$12);
        }
    }
    
    /**
     * Gets the "schema" attribute
     */
    public java.lang.String getSchema()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SCHEMA$14);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "schema" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetSchema()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SCHEMA$14);
            return target;
        }
    }
    
    /**
     * True if has "schema" attribute
     */
    public boolean isSetSchema()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(SCHEMA$14) != null;
        }
    }
    
    /**
     * Sets the "schema" attribute
     */
    public void setSchema(java.lang.String schema)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SCHEMA$14);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SCHEMA$14);
            }
            target.setStringValue(schema);
        }
    }
    
    /**
     * Sets (as xml) the "schema" attribute
     */
    public void xsetSchema(org.apache.xmlbeans.XmlAnyURI schema)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SCHEMA$14);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SCHEMA$14);
            }
            target.set(schema);
        }
    }
    
    /**
     * Unsets the "schema" attribute
     */
    public void unsetSchema()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(SCHEMA$14);
        }
    }
    /**
     * An XML Header(@http://www.uncertweb.org).
     *
     * This is a complex type.
     */
    public static class HeaderImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.uncertweb.InputReferenceType.Header
    {
        private static final long serialVersionUID = 1L;
        
        public HeaderImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName KEY$0 = 
            new javax.xml.namespace.QName("", "key");
        private static final javax.xml.namespace.QName VALUE$2 = 
            new javax.xml.namespace.QName("", "value");
        
        
        /**
         * Gets the "key" attribute
         */
        public java.lang.String getKey()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(KEY$0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "key" attribute
         */
        public org.apache.xmlbeans.XmlString xgetKey()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(KEY$0);
                return target;
            }
        }
        
        /**
         * Sets the "key" attribute
         */
        public void setKey(java.lang.String key)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(KEY$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(KEY$0);
                }
                target.setStringValue(key);
            }
        }
        
        /**
         * Sets (as xml) the "key" attribute
         */
        public void xsetKey(org.apache.xmlbeans.XmlString key)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(KEY$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(KEY$0);
                }
                target.set(key);
            }
        }
        
        /**
         * Gets the "value" attribute
         */
        public java.lang.String getValue()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VALUE$2);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "value" attribute
         */
        public org.apache.xmlbeans.XmlString xgetValue()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(VALUE$2);
                return target;
            }
        }
        
        /**
         * Sets the "value" attribute
         */
        public void setValue(java.lang.String value)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VALUE$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VALUE$2);
                }
                target.setStringValue(value);
            }
        }
        
        /**
         * Sets (as xml) the "value" attribute
         */
        public void xsetValue(org.apache.xmlbeans.XmlString value)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(VALUE$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(VALUE$2);
                }
                target.set(value);
            }
        }
    }
    /**
     * An XML BodyReference(@http://www.uncertweb.org).
     *
     * This is a complex type.
     */
    public static class BodyReferenceImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.uncertweb.InputReferenceType.BodyReference
    {
        private static final long serialVersionUID = 1L;
        
        public BodyReferenceImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName HREF$0 = 
            new javax.xml.namespace.QName("http://www.w3.org/1999/xlink", "href");
        
        
        /**
         * Gets the "href" attribute
         */
        public java.lang.String getHref()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(HREF$0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "href" attribute
         */
        public org.apache.xmlbeans.XmlAnyURI xgetHref()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlAnyURI target = null;
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(HREF$0);
                return target;
            }
        }
        
        /**
         * Sets the "href" attribute
         */
        public void setHref(java.lang.String href)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(HREF$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(HREF$0);
                }
                target.setStringValue(href);
            }
        }
        
        /**
         * Sets (as xml) the "href" attribute
         */
        public void xsetHref(org.apache.xmlbeans.XmlAnyURI href)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlAnyURI target = null;
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(HREF$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(HREF$0);
                }
                target.set(href);
            }
        }
    }
    /**
     * An XML method(@).
     *
     * This is an atomic type that is a restriction of org.uncertweb.InputReferenceType$Method.
     */
    public static class MethodImpl extends org.apache.xmlbeans.impl.values.JavaStringEnumerationHolderEx implements org.uncertweb.InputReferenceType.Method
    {
        private static final long serialVersionUID = 1L;
        
        public MethodImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, false);
        }
        
        protected MethodImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
    }
}
