/*
 * An XML document type.
 * Localname: AbstractInput
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.AbstractInputDocument
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb.impl;
/**
 * A document containing one AbstractInput(@http://www.uncertweb.org) element.
 *
 * This is a complex type.
 */
public class AbstractInputDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.uncertweb.AbstractInputDocument
{
    private static final long serialVersionUID = 1L;
    
    public AbstractInputDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ABSTRACTINPUT$0 = 
        new javax.xml.namespace.QName("http://www.uncertweb.org", "AbstractInput");
    private static final org.apache.xmlbeans.QNameSet ABSTRACTINPUT$1 = org.apache.xmlbeans.QNameSet.forArray( new javax.xml.namespace.QName[] { 
        new javax.xml.namespace.QName("http://www.uncertweb.org", "AbstractStaticInput"),
        new javax.xml.namespace.QName("http://www.uncertweb.org", "AbstractInput"),
        new javax.xml.namespace.QName("http://www.uncertweb.org", "StaticInput"),
        new javax.xml.namespace.QName("http://www.uncertweb.org", "AbstractUncertainInput"),
        new javax.xml.namespace.QName("http://www.uncertweb.org", "UncertainInput"),
    });
    
    
    /**
     * Gets the "AbstractInput" element
     */
    public org.uncertweb.AbstractInputType getAbstractInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.AbstractInputType target = null;
            target = (org.uncertweb.AbstractInputType)get_store().find_element_user(ABSTRACTINPUT$1, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "AbstractInput" element
     */
    public void setAbstractInput(org.uncertweb.AbstractInputType abstractInput)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.AbstractInputType target = null;
            target = (org.uncertweb.AbstractInputType)get_store().find_element_user(ABSTRACTINPUT$1, 0);
            if (target == null)
            {
                target = (org.uncertweb.AbstractInputType)get_store().add_element_user(ABSTRACTINPUT$0);
            }
            target.set(abstractInput);
        }
    }
    
    /**
     * Appends and returns a new empty "AbstractInput" element
     */
    public org.uncertweb.AbstractInputType addNewAbstractInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.AbstractInputType target = null;
            target = (org.uncertweb.AbstractInputType)get_store().add_element_user(ABSTRACTINPUT$0);
            return target;
        }
    }
}
