/*
 * XML Type:  DataType
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.DataType
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb.impl;
/**
 * An XML DataType(@http://www.uncertweb.org).
 *
 * This is a complex type.
 */
public class DataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.uncertweb.DataType
{
    private static final long serialVersionUID = 1L;

    public DataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }

    private static final javax.xml.namespace.QName COMPLEXDATA$0 =
        new javax.xml.namespace.QName("http://www.uncertweb.org", "ComplexData");
    private static final javax.xml.namespace.QName LITERALDATA$2 =
        new javax.xml.namespace.QName("http://www.uncertweb.org", "LiteralData");
    private static final javax.xml.namespace.QName BOUNDINGBOXDATA$4 =
        new javax.xml.namespace.QName("http://www.uncertweb.org", "BoundingBoxData");


    /**
     * Gets the "ComplexData" element
     */
    public net.opengis.wps.x100.ComplexDataType getComplexData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.ComplexDataType target = null;
            target = (net.opengis.wps.x100.ComplexDataType)get_store().find_element_user(COMPLEXDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }

    /**
     * True if has "ComplexData" element
     */
    public boolean isSetComplexData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COMPLEXDATA$0) != 0;
        }
    }

    /**
     * Sets the "ComplexData" element
     */
    public void setComplexData(net.opengis.wps.x100.ComplexDataType complexData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.ComplexDataType target = null;
            target = (net.opengis.wps.x100.ComplexDataType)get_store().find_element_user(COMPLEXDATA$0, 0);
            if (target == null)
            {
                target = (net.opengis.wps.x100.ComplexDataType)get_store().add_element_user(COMPLEXDATA$0);
            }
            target.set(complexData);
        }
    }

    /**
     * Appends and returns a new empty "ComplexData" element
     */
    public net.opengis.wps.x100.ComplexDataType addNewComplexData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.ComplexDataType target = null;
            target = (net.opengis.wps.x100.ComplexDataType)get_store().add_element_user(COMPLEXDATA$0);
            return target;
        }
    }

    /**
     * Unsets the "ComplexData" element
     */
    public void unsetComplexData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COMPLEXDATA$0, 0);
        }
    }

    /**
     * Gets the "LiteralData" element
     */
    public net.opengis.wps.x100.LiteralDataType getLiteralData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.LiteralDataType target = null;
            target = (net.opengis.wps.x100.LiteralDataType)get_store().find_element_user(LITERALDATA$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }

    /**
     * True if has "LiteralData" element
     */
    public boolean isSetLiteralData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LITERALDATA$2) != 0;
        }
    }

    /**
     * Sets the "LiteralData" element
     */
    public void setLiteralData(net.opengis.wps.x100.LiteralDataType literalData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.LiteralDataType target = null;
            target = (net.opengis.wps.x100.LiteralDataType)get_store().find_element_user(LITERALDATA$2, 0);
            if (target == null)
            {
                target = (net.opengis.wps.x100.LiteralDataType)get_store().add_element_user(LITERALDATA$2);
            }
            target.set(literalData);
        }
    }

    /**
     * Appends and returns a new empty "LiteralData" element
     */
    public net.opengis.wps.x100.LiteralDataType addNewLiteralData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.LiteralDataType target = null;
            target = (net.opengis.wps.x100.LiteralDataType)get_store().add_element_user(LITERALDATA$2);
            return target;
        }
    }

    /**
     * Unsets the "LiteralData" element
     */
    public void unsetLiteralData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LITERALDATA$2, 0);
        }
    }

    /**
     * Gets the "BoundingBoxData" element
     */
    public net.opengis.ows.x11.BoundingBoxType getBoundingBoxData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.BoundingBoxType target = null;
            target = (net.opengis.ows.x11.BoundingBoxType)get_store().find_element_user(BOUNDINGBOXDATA$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }

    /**
     * True if has "BoundingBoxData" element
     */
    public boolean isSetBoundingBoxData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(BOUNDINGBOXDATA$4) != 0;
        }
    }

    /**
     * Sets the "BoundingBoxData" element
     */
    public void setBoundingBoxData(net.opengis.ows.x11.BoundingBoxType boundingBoxData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.BoundingBoxType target = null;
            target = (net.opengis.ows.x11.BoundingBoxType)get_store().find_element_user(BOUNDINGBOXDATA$4, 0);
            if (target == null)
            {
                target = (net.opengis.ows.x11.BoundingBoxType)get_store().add_element_user(BOUNDINGBOXDATA$4);
            }
            target.set(boundingBoxData);
        }
    }

    /**
     * Appends and returns a new empty "BoundingBoxData" element
     */
    public net.opengis.ows.x11.BoundingBoxType addNewBoundingBoxData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.BoundingBoxType target = null;
            target = (net.opengis.ows.x11.BoundingBoxType)get_store().add_element_user(BOUNDINGBOXDATA$4);
            return target;
        }
    }

    /**
     * Unsets the "BoundingBoxData" element
     */
    public void unsetBoundingBoxData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(BOUNDINGBOXDATA$4, 0);
        }
    }
}
