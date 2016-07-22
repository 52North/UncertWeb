/*
 * XML Type:  ComplexDataType
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.ComplexDataType
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb.impl;
/**
 * An XML ComplexDataType(@http://www.uncertweb.org).
 *
 * This is a complex type.
 */
public class ComplexDataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.uncertweb.ComplexDataType
{
    private static final long serialVersionUID = 1L;

    public ComplexDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }

    private static final javax.xml.namespace.QName MIMETYPE$0 =
        new javax.xml.namespace.QName("", "mimeType");
    private static final javax.xml.namespace.QName ENCODING$2 =
        new javax.xml.namespace.QName("", "encoding");
    private static final javax.xml.namespace.QName SCHEMA$4 =
        new javax.xml.namespace.QName("", "schema");


    /**
     * Gets the "mimeType" attribute
     */
    public java.lang.String getMimeType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MIMETYPE$0);
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
            target = (net.opengis.ows.x11.MimeType)get_store().find_attribute_user(MIMETYPE$0);
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
            return get_store().find_attribute_user(MIMETYPE$0) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MIMETYPE$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(MIMETYPE$0);
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
            target = (net.opengis.ows.x11.MimeType)get_store().find_attribute_user(MIMETYPE$0);
            if (target == null)
            {
                target = (net.opengis.ows.x11.MimeType)get_store().add_attribute_user(MIMETYPE$0);
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
            get_store().remove_attribute(MIMETYPE$0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ENCODING$2);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ENCODING$2);
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
            return get_store().find_attribute_user(ENCODING$2) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ENCODING$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ENCODING$2);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ENCODING$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(ENCODING$2);
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
            get_store().remove_attribute(ENCODING$2);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SCHEMA$4);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SCHEMA$4);
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
            return get_store().find_attribute_user(SCHEMA$4) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SCHEMA$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SCHEMA$4);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SCHEMA$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SCHEMA$4);
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
            get_store().remove_attribute(SCHEMA$4);
        }
    }
}
