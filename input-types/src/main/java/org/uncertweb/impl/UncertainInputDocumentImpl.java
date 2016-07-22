/*
 * An XML document type.
 * Localname: UncertainInput
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.UncertainInputDocument
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb.impl;
/**
 * A document containing one UncertainInput(@http://www.uncertweb.org) element.
 *
 * This is a complex type.
 */
public class UncertainInputDocumentImpl extends org.uncertweb.impl.AbstractUncertainInputDocumentImpl implements org.uncertweb.UncertainInputDocument
{
    private static final long serialVersionUID = 1L;
    
    public UncertainInputDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName UNCERTAININPUT$0 = 
        new javax.xml.namespace.QName("http://www.uncertweb.org", "UncertainInput");
    
    
    /**
     * Gets the "UncertainInput" element
     */
    public org.uncertweb.UncertainInputType getUncertainInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.UncertainInputType target = null;
            target = (org.uncertweb.UncertainInputType)get_store().find_element_user(UNCERTAININPUT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "UncertainInput" element
     */
    public void setUncertainInput(org.uncertweb.UncertainInputType uncertainInput)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.UncertainInputType target = null;
            target = (org.uncertweb.UncertainInputType)get_store().find_element_user(UNCERTAININPUT$0, 0);
            if (target == null)
            {
                target = (org.uncertweb.UncertainInputType)get_store().add_element_user(UNCERTAININPUT$0);
            }
            target.set(uncertainInput);
        }
    }
    
    /**
     * Appends and returns a new empty "UncertainInput" element
     */
    public org.uncertweb.UncertainInputType addNewUncertainInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.UncertainInputType target = null;
            target = (org.uncertweb.UncertainInputType)get_store().add_element_user(UNCERTAININPUT$0);
            return target;
        }
    }
}
