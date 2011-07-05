/*
 * XML Type:  LiteralDataType
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.LiteralDataType
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb.impl;
/**
 * An XML LiteralDataType(@http://www.uncertweb.org).
 *
 * This is an atomic type that is a restriction of org.uncertweb.LiteralDataType.
 */
public class LiteralDataTypeImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements org.uncertweb.LiteralDataType
{
    private static final long serialVersionUID = 1L;
    
    public LiteralDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType, true);
    }
    
    protected LiteralDataTypeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
    {
        super(sType, b);
    }
    
    private static final javax.xml.namespace.QName DATATYPE$0 = 
        new javax.xml.namespace.QName("", "dataType");
    private static final javax.xml.namespace.QName UOM$2 = 
        new javax.xml.namespace.QName("", "uom");
    
    
    /**
     * Gets the "dataType" attribute
     */
    public java.lang.String getDataType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DATATYPE$0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "dataType" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetDataType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(DATATYPE$0);
            return target;
        }
    }
    
    /**
     * True if has "dataType" attribute
     */
    public boolean isSetDataType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(DATATYPE$0) != null;
        }
    }
    
    /**
     * Sets the "dataType" attribute
     */
    public void setDataType(java.lang.String dataType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DATATYPE$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(DATATYPE$0);
            }
            target.setStringValue(dataType);
        }
    }
    
    /**
     * Sets (as xml) the "dataType" attribute
     */
    public void xsetDataType(org.apache.xmlbeans.XmlAnyURI dataType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(DATATYPE$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(DATATYPE$0);
            }
            target.set(dataType);
        }
    }
    
    /**
     * Unsets the "dataType" attribute
     */
    public void unsetDataType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(DATATYPE$0);
        }
    }
    
    /**
     * Gets the "uom" attribute
     */
    public java.lang.String getUom()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(UOM$2);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "uom" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetUom()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(UOM$2);
            return target;
        }
    }
    
    /**
     * True if has "uom" attribute
     */
    public boolean isSetUom()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(UOM$2) != null;
        }
    }
    
    /**
     * Sets the "uom" attribute
     */
    public void setUom(java.lang.String uom)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(UOM$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(UOM$2);
            }
            target.setStringValue(uom);
        }
    }
    
    /**
     * Sets (as xml) the "uom" attribute
     */
    public void xsetUom(org.apache.xmlbeans.XmlAnyURI uom)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(UOM$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(UOM$2);
            }
            target.set(uom);
        }
    }
    
    /**
     * Unsets the "uom" attribute
     */
    public void unsetUom()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(UOM$2);
        }
    }
}
