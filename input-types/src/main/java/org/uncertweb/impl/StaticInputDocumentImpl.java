/*
 * An XML document type.
 * Localname: StaticInput
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.StaticInputDocument
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb.impl;
/**
 * A document containing one StaticInput(@http://www.uncertweb.org) element.
 *
 * This is a complex type.
 */
public class StaticInputDocumentImpl extends org.uncertweb.impl.AbstractStaticInputDocumentImpl implements org.uncertweb.StaticInputDocument
{
    private static final long serialVersionUID = 1L;
    
    public StaticInputDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATICINPUT$0 = 
        new javax.xml.namespace.QName("http://www.uncertweb.org", "StaticInput");
    
    
    /**
     * Gets the "StaticInput" element
     */
    public org.uncertweb.StaticInputType getStaticInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.StaticInputType target = null;
            target = (org.uncertweb.StaticInputType)get_store().find_element_user(STATICINPUT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "StaticInput" element
     */
    public void setStaticInput(org.uncertweb.StaticInputType staticInput)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.StaticInputType target = null;
            target = (org.uncertweb.StaticInputType)get_store().find_element_user(STATICINPUT$0, 0);
            if (target == null)
            {
                target = (org.uncertweb.StaticInputType)get_store().add_element_user(STATICINPUT$0);
            }
            target.set(staticInput);
        }
    }
    
    /**
     * Appends and returns a new empty "StaticInput" element
     */
    public org.uncertweb.StaticInputType addNewStaticInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.StaticInputType target = null;
            target = (org.uncertweb.StaticInputType)get_store().add_element_user(STATICINPUT$0);
            return target;
        }
    }
}
