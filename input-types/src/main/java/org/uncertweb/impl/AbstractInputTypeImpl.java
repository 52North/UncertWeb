/*
 * XML Type:  AbstractInputType
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.AbstractInputType
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb.impl;
/**
 * An XML AbstractInputType(@http://www.uncertweb.org).
 *
 * This is a complex type.
 */
public class AbstractInputTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.uncertweb.AbstractInputType
{
    private static final long serialVersionUID = 1L;
    
    public AbstractInputTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName IDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.opengis.net/ows/1.1", "Identifier");
    private static final javax.xml.namespace.QName TITLE$2 = 
        new javax.xml.namespace.QName("http://www.opengis.net/ows/1.1", "Title");
    private static final javax.xml.namespace.QName ABSTRACT$4 = 
        new javax.xml.namespace.QName("http://www.opengis.net/ows/1.1", "Abstract");
    private static final javax.xml.namespace.QName REFERENCE$6 = 
        new javax.xml.namespace.QName("http://www.opengis.net/wps/1.0.0", "Reference");
    private static final javax.xml.namespace.QName DATA$8 = 
        new javax.xml.namespace.QName("http://www.opengis.net/wps/1.0.0", "Data");
    
    
    /**
     * Gets the "Identifier" element
     */
    public net.opengis.ows.x11.CodeType getIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.CodeType target = null;
            target = (net.opengis.ows.x11.CodeType)get_store().find_element_user(IDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "Identifier" element
     */
    public void setIdentifier(net.opengis.ows.x11.CodeType identifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.CodeType target = null;
            target = (net.opengis.ows.x11.CodeType)get_store().find_element_user(IDENTIFIER$0, 0);
            if (target == null)
            {
                target = (net.opengis.ows.x11.CodeType)get_store().add_element_user(IDENTIFIER$0);
            }
            target.set(identifier);
        }
    }
    
    /**
     * Appends and returns a new empty "Identifier" element
     */
    public net.opengis.ows.x11.CodeType addNewIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.CodeType target = null;
            target = (net.opengis.ows.x11.CodeType)get_store().add_element_user(IDENTIFIER$0);
            return target;
        }
    }
    
    /**
     * Gets the "Title" element
     */
    public net.opengis.ows.x11.LanguageStringType getTitle()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.LanguageStringType target = null;
            target = (net.opengis.ows.x11.LanguageStringType)get_store().find_element_user(TITLE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "Title" element
     */
    public boolean isSetTitle()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(TITLE$2) != 0;
        }
    }
    
    /**
     * Sets the "Title" element
     */
    public void setTitle(net.opengis.ows.x11.LanguageStringType title)
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.LanguageStringType target = null;
            target = (net.opengis.ows.x11.LanguageStringType)get_store().find_element_user(TITLE$2, 0);
            if (target == null)
            {
                target = (net.opengis.ows.x11.LanguageStringType)get_store().add_element_user(TITLE$2);
            }
            target.set(title);
        }
    }
    
    /**
     * Appends and returns a new empty "Title" element
     */
    public net.opengis.ows.x11.LanguageStringType addNewTitle()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.LanguageStringType target = null;
            target = (net.opengis.ows.x11.LanguageStringType)get_store().add_element_user(TITLE$2);
            return target;
        }
    }
    
    /**
     * Unsets the "Title" element
     */
    public void unsetTitle()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(TITLE$2, 0);
        }
    }
    
    /**
     * Gets the "Abstract" element
     */
    public net.opengis.ows.x11.LanguageStringType getAbstract()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.LanguageStringType target = null;
            target = (net.opengis.ows.x11.LanguageStringType)get_store().find_element_user(ABSTRACT$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "Abstract" element
     */
    public boolean isSetAbstract()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ABSTRACT$4) != 0;
        }
    }
    
    /**
     * Sets the "Abstract" element
     */
    public void setAbstract(net.opengis.ows.x11.LanguageStringType xabstract)
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.LanguageStringType target = null;
            target = (net.opengis.ows.x11.LanguageStringType)get_store().find_element_user(ABSTRACT$4, 0);
            if (target == null)
            {
                target = (net.opengis.ows.x11.LanguageStringType)get_store().add_element_user(ABSTRACT$4);
            }
            target.set(xabstract);
        }
    }
    
    /**
     * Appends and returns a new empty "Abstract" element
     */
    public net.opengis.ows.x11.LanguageStringType addNewAbstract()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.ows.x11.LanguageStringType target = null;
            target = (net.opengis.ows.x11.LanguageStringType)get_store().add_element_user(ABSTRACT$4);
            return target;
        }
    }
    
    /**
     * Unsets the "Abstract" element
     */
    public void unsetAbstract()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ABSTRACT$4, 0);
        }
    }
    
    /**
     * Gets the "Reference" element
     */
    public net.opengis.wps.x100.InputReferenceType getReference()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.InputReferenceType target = null;
            target = (net.opengis.wps.x100.InputReferenceType)get_store().find_element_user(REFERENCE$6, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "Reference" element
     */
    public boolean isSetReference()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(REFERENCE$6) != 0;
        }
    }
    
    /**
     * Sets the "Reference" element
     */
    public void setReference(net.opengis.wps.x100.InputReferenceType reference)
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.InputReferenceType target = null;
            target = (net.opengis.wps.x100.InputReferenceType)get_store().find_element_user(REFERENCE$6, 0);
            if (target == null)
            {
                target = (net.opengis.wps.x100.InputReferenceType)get_store().add_element_user(REFERENCE$6);
            }
            target.set(reference);
        }
    }
    
    /**
     * Appends and returns a new empty "Reference" element
     */
    public net.opengis.wps.x100.InputReferenceType addNewReference()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.InputReferenceType target = null;
            target = (net.opengis.wps.x100.InputReferenceType)get_store().add_element_user(REFERENCE$6);
            return target;
        }
    }
    
    /**
     * Unsets the "Reference" element
     */
    public void unsetReference()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(REFERENCE$6, 0);
        }
    }
    
    /**
     * Gets the "Data" element
     */
    public net.opengis.wps.x100.DataType getData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.DataType target = null;
            target = (net.opengis.wps.x100.DataType)get_store().find_element_user(DATA$8, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "Data" element
     */
    public boolean isSetData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(DATA$8) != 0;
        }
    }
    
    /**
     * Sets the "Data" element
     */
    public void setData(net.opengis.wps.x100.DataType data)
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.DataType target = null;
            target = (net.opengis.wps.x100.DataType)get_store().find_element_user(DATA$8, 0);
            if (target == null)
            {
                target = (net.opengis.wps.x100.DataType)get_store().add_element_user(DATA$8);
            }
            target.set(data);
        }
    }
    
    /**
     * Appends and returns a new empty "Data" element
     */
    public net.opengis.wps.x100.DataType addNewData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            net.opengis.wps.x100.DataType target = null;
            target = (net.opengis.wps.x100.DataType)get_store().add_element_user(DATA$8);
            return target;
        }
    }
    
    /**
     * Unsets the "Data" element
     */
    public void unsetData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(DATA$8, 0);
        }
    }
}
