/*
 * An XML document type.
 * Localname: AbstractUncertainInput
 * Namespace: http://www.uncertweb.org
 * Java type: org.uncertweb.AbstractUncertainInputDocument
 *
 * Automatically generated - do not modify.
 */
package org.uncertweb.impl;
/**
 * A document containing one AbstractUncertainInput(@http://www.uncertweb.org) element.
 *
 * This is a complex type.
 */
public class AbstractUncertainInputDocumentImpl extends org.uncertweb.impl.AbstractInputDocumentImpl implements org.uncertweb.AbstractUncertainInputDocument
{
    private static final long serialVersionUID = 1L;

    public AbstractUncertainInputDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }

    private static final javax.xml.namespace.QName ABSTRACTUNCERTAININPUT$0 =
        new javax.xml.namespace.QName("http://www.uncertweb.org", "AbstractUncertainInput");
    private static final org.apache.xmlbeans.QNameSet ABSTRACTUNCERTAININPUT$1 = org.apache.xmlbeans.QNameSet.forArray( new javax.xml.namespace.QName[] {
        new javax.xml.namespace.QName("http://www.uncertweb.org", "AbstractUncertainInput"),
        new javax.xml.namespace.QName("http://www.uncertweb.org", "UncertainInput"),
    });


    /**
     * Gets the "AbstractUncertainInput" element
     */
    public org.uncertweb.AbstractUncertainInputType getAbstractUncertainInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.AbstractUncertainInputType target = null;
            target = (org.uncertweb.AbstractUncertainInputType)get_store().find_element_user(ABSTRACTUNCERTAININPUT$1, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }

    /**
     * Sets the "AbstractUncertainInput" element
     */
    public void setAbstractUncertainInput(org.uncertweb.AbstractUncertainInputType abstractUncertainInput)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.AbstractUncertainInputType target = null;
            target = (org.uncertweb.AbstractUncertainInputType)get_store().find_element_user(ABSTRACTUNCERTAININPUT$1, 0);
            if (target == null)
            {
                target = (org.uncertweb.AbstractUncertainInputType)get_store().add_element_user(ABSTRACTUNCERTAININPUT$0);
            }
            target.set(abstractUncertainInput);
        }
    }

    /**
     * Appends and returns a new empty "AbstractUncertainInput" element
     */
    public org.uncertweb.AbstractUncertainInputType addNewAbstractUncertainInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.uncertweb.AbstractUncertainInputType target = null;
            target = (org.uncertweb.AbstractUncertainInputType)get_store().add_element_user(ABSTRACTUNCERTAININPUT$0);
            return target;
        }
    }
}
