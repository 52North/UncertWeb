/*
 * An XML document type.
 * Localname: AbstractStaticInput
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.AbstractStaticInputDocument
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb.impl;
/**
 * A document containing one AbstractStaticInput(@http://www.uncertweb.org) element.
 *
 * This is a complex type.
 */
public class AbstractStaticInputDocumentImpl extends org.uncertweb.impl.AbstractInputDocumentImpl implements org.uncertweb.AbstractStaticInputDocument
{
    private static final long serialVersionUID = 1L;
    
    public AbstractStaticInputDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ABSTRACTSTATICINPUT$0 = 
        new javax.xml.namespace.QName("http://www.uncertweb.org", "AbstractStaticInput");
    private static final org.apache.xmlbeans.QNameSet ABSTRACTSTATICINPUT$1 = org.apache.xmlbeans.QNameSet.forArray( new javax.xml.namespace.QName[] { 
        new javax.xml.namespace.QName("http://www.uncertweb.org", "AbstractStaticInput"),
        new javax.xml.namespace.QName("http://www.uncertweb.org", "StaticInput"),
    });
    
    
    /**
     * Gets the "AbstractStaticInput" element
     */
    public org.uncertweb.AbstractStaticInputType getAbstractStaticInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.AbstractStaticInputType target = null;
            target = (org.uncertweb.AbstractStaticInputType)get_store().find_element_user(ABSTRACTSTATICINPUT$1, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "AbstractStaticInput" element
     */
    public void setAbstractStaticInput(org.uncertweb.AbstractStaticInputType abstractStaticInput)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.AbstractStaticInputType target = null;
            target = (org.uncertweb.AbstractStaticInputType)get_store().find_element_user(ABSTRACTSTATICINPUT$1, 0);
            if (target == null)
            {
                target = (org.uncertweb.AbstractStaticInputType)get_store().add_element_user(ABSTRACTSTATICINPUT$0);
            }
            target.set(abstractStaticInput);
        }
    }
    
    /**
     * Appends and returns a new empty "AbstractStaticInput" element
     */
    public org.uncertweb.AbstractStaticInputType addNewAbstractStaticInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.AbstractStaticInputType target = null;
            target = (org.uncertweb.AbstractStaticInputType)get_store().add_element_user(ABSTRACTSTATICINPUT$0);
            return target;
        }
    }
}
