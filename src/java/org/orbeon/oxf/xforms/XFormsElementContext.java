/**
 *  Copyright (C) 2004 Orbeon, Inc.
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version
 *  2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  The full text of the license is available at http://www.gnu.org/copyleft/lesser.html
 */
package org.orbeon.oxf.xforms;

import org.orbeon.oxf.pipeline.api.PipelineContext;
import org.orbeon.oxf.processor.xforms.output.element.XFormsElement;
import org.orbeon.oxf.xml.NamespaceSupport3;
import org.xml.sax.ContentHandler;

import java.util.*;

/**
 * Context in which control elements are executed (this is legacy XForms Classic only).
 */
public class XFormsElementContext extends XFormsControls {

    private PipelineContext pipelineContext;
    private ContentHandler contentHandler;

    private Map repeatIdToIndex = new HashMap();
    private Stack elements = new Stack();

    private NamespaceSupport3 namespaceSupport = new NamespaceSupport3();

    private String encryptionPassword;

    public XFormsElementContext(PipelineContext pipelineContext, XFormsContainingDocument containingDocument, ContentHandler contentHandler) {

        super(containingDocument, null, null);
        super.initialize(pipelineContext);

        this.pipelineContext = pipelineContext;
        this.contentHandler = contentHandler;
        this.encryptionPassword = XFormsProperties.getXFormsPassword();
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public void pushElement(XFormsElement element) {
        elements.push(element);
    }

    public XFormsElement popElement() {
        return (XFormsElement) elements.pop();
    }

    public XFormsElement peekElement() {
        return (XFormsElement) elements.peek();
    }

    public XFormsElement getParentElement(int level) {
        return elements.size() > level + 1 ? (XFormsElement) elements.get(elements.size() - (level + 2)) : null;
    }

    public PipelineContext getPipelineContext() {
        return pipelineContext;
    }

    public String getEncryptionPassword() {
        return encryptionPassword;
    }

    public void pushBinding(String ref, String context, String nodeset, String model, String bind) {
        getContextStack().pushBinding(pipelineContext, ref, context, nodeset, model, bind, null, getCurrentPrefixToURIMap());
    }

    public void setRepeatIdIndex(String repeatId, int index) {
        // Update current element of nodeset in stack
        getContextStack().popBinding();
        final List newNodeset = new ArrayList();
        newNodeset.add(getContextStack().getCurrentNodeset().get(index - 1));
        final XFormsContextStack.BindingContext currentBindingContext = getContextStack().getCurrentBindingContext();
        getContextStack().legacyGetStack().push(new XFormsContextStack.BindingContext(currentBindingContext, currentBindingContext.getModel(),
                newNodeset, 1, repeatId, true, null, null, false, null));//TODO: check this

        if (repeatId != null)
            repeatIdToIndex.put(repeatId, new Integer(index));
    }

    public void endRepeatId(String repeatId) {
        if (repeatId != null)
            repeatIdToIndex.remove(repeatId);
        getContextStack().popBinding();
    }

    public void startRepeatId(String repeatId) {
        getContextStack().legacyGetStack().push(null);
    }

    public Map getRepeatIdToIndex() {
        return repeatIdToIndex;
    }

    public Map getCurrentPrefixToURIMap() {
        Map prefixToURI = new HashMap();
        for (Enumeration e = namespaceSupport.getPrefixes(); e.hasMoreElements();) {
            String prefix = (String) e.nextElement();
            prefixToURI.put(prefix, namespaceSupport.getURI(prefix));
        }
        return prefixToURI;
    }

    public NamespaceSupport3 getNamespaceSupport() {
        return namespaceSupport;
    }

    /**
     * Returns the text value of the currently referenced node in the instance.
     */
    public String getRefValue() {
        return XFormsInstance.getValueForNodeInfo(getContextStack().getCurrentBindingContext().getSingleNode());
    }
}
