/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.coode.html.impl.OWLHTMLConstants;
import org.coode.html.page.HTMLPage;
import org.coode.html.util.URLUtils;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 25, 2008<br><br>
 */
public class LinkDoclet<O extends OWLEntity> implements HTMLDoclet<O>{

    private O object;

    private OWLHTMLKit kit;

    private OWLHTMLConstants.LinkTarget target;

    private Map<String, String> attributes = new HashMap<>();


    public LinkDoclet(O object, OWLHTMLKit kit) {
        this.object = object;
        this.kit = kit;
    }

    @Override
    public String getID() {
        return "doclet.link." + object.toString();
    }

    @Override
    public void renderContent(URL pageURL, PrintWriter out) {
        String shortName = kit.getOWLServer().getShortFormProvider().getShortForm(object);
        URL href = kit.getURLScheme().getURLForOWLObject(object);

        out.print("<a href='");
        out.print(URLUtils.createRelativeURL(pageURL, href));
        out.print("'");

        for (String attr : attributes.keySet()){
            out.print(" ");
            out.print(attr);
            out.print("='");
            out.print(attributes.get(attr));
            out.print("'");
        }
        if (target != null && !isSingleFrameNavigation()){
            out.print(" target='" + target + "'");
        }
        out.println(" >" + shortName + "</a>");
    }

    @Override
    public void renderAll(URL pageURL, PrintWriter out) {
        renderContent(pageURL, out);
    }

    @Override
    public void setUserObject(O object) {
        this.object = object;
    }

    @Override
    public O getUserObject() {
        return object;
    }

    @Override
    public boolean isPinned() {
        return true;
    }

    @Override
    public boolean isFullPage() {
        return false;
    }

    @Override
    public HTMLPage<O> asPage() {
        return null;
    }

    @Override
    public Set<URL> getRequiredCSS() {
        return Collections.emptySet();
    }

    @Override
    public List<URL> getRequiredJS() {
        return Collections.emptyList();
    }

    public void setCSSClass(String cssClass){
        attributes.put("class", cssClass);
    }

    public void setCSS(String css){
        attributes.put("style", css);
    }

    public void setTarget(OWLHTMLConstants.LinkTarget target){
        this.target = target;
    }

    private boolean isSingleFrameNavigation() {
        String target = kit.getHTMLProperties().get(org.coode.html.impl.OWLHTMLProperty.optionContentWindow);
        return target == null || target.isEmpty();
    }

    public void addAttribute(String attr, String value) {
        attributes.put(attr, value);
    }
}
