/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.coode.html.impl.OWLHTMLConstants;
import org.coode.html.renderer.OWLHTMLRenderer;
import org.coode.owl.mngr.HierarchyProvider;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Feb 7, 2008<br><br>
 */
public class LeafNodeDoclet<O extends OWLObject> extends AbstractOWLDocDoclet<O>{

    private final HierarchyProvider<O> hp;

    private boolean autoExpandSubs = false;

    private boolean showSubs = false;

    private O focus;

    private O nodeObject;



    public LeafNodeDoclet(OWLHTMLKit kit, O nodeObject, HierarchyProvider<O> hp) {
        super(kit);
        setPinned(true); // you will never change the subs as they will be regenerated each time this changed
        this.hp = hp;
        this.nodeObject = nodeObject;
    }

    public O getNodeObject(){
        return nodeObject;
    }

    @Override
    public void setUserObject(O object) {
        focus = object;
    }

    protected List<O> asOrderedList(Set<O> objects) {
        List<O> list = new ArrayList<>(objects);
        if (getComparator() != null){
            Collections.sort(list, getComparator());
        }
        return list;
    }

    protected Comparator<? super O> getComparator() {
        return getOWLHTMLKit().getOWLObjectComparator();
    }

    protected O getFocus() {
        return focus;
    }

    public final void setAutoExpandEnabled(boolean enabled) {
        if (autoExpandSubs != enabled){
            this.autoExpandSubs = enabled;
            for (HTMLDoclet<O> subdoclet : getDoclets()){
                if (subdoclet instanceof LeafNodeDoclet){
                    ((LeafNodeDoclet<O>)subdoclet).setAutoExpandEnabled(enabled);
                }
            }
        }
    }


    public final void setShowSubs(boolean enabled) {
        if (showSubs != enabled){
            this.showSubs = enabled;
            LeafNodeDoclet<O> lastPathContainingFocusedNode = null;
            for (HTMLDoclet<O> subdoclet : getDoclets()){
                if (subdoclet instanceof LeafNodeDoclet &&
                    getHierarchyProvider().hasAncestor(((LeafNodeDoclet<O>)subdoclet).getUserObject(), focus)){
                    lastPathContainingFocusedNode = (LeafNodeDoclet<O>)subdoclet;
                }
            }
            if (lastPathContainingFocusedNode != null){
                lastPathContainingFocusedNode.setShowSubs(enabled);
            }
        }
    }


    protected HierarchyProvider<O> getHierarchyProvider() {
        return hp;
    }


    protected final boolean isAutoExpandSubs() {
        return autoExpandSubs;
    }


    protected final boolean isShowSubsEnabled(){
        return showSubs;
    }


    @Override
    public String getID() {
        return getUserObject().toString();
    }

    @Override
    protected void renderHeader(URL pageURL, PrintWriter out) {
        if (hp.isLeaf(getNodeObject())){
            out.print("<li>-&nbsp;");
        }
        else{
            out.print("<li><span class='expandable'>+</span>");
        }

        out.print("&nbsp;");

        final O node = getNodeObject();
        if (node != null){
            renderNode(node, new OWLHTMLRenderer(getOWLHTMLKit()), pageURL, out);
        }

        if (getSubDocletCount() > 0){
            out.println(); 
            out.println("<ul>");
        }
    }

    protected void renderNode(O node, OWLHTMLRenderer objRenderer, URL pageURL, PrintWriter out) {

        objRenderer.render(node, pageURL, out);

        for (O synonym : asOrderedList(hp.getEquivalents(node))){
            out.print(" ");
            out.print(OWLHTMLConstants.EQUIV_CHAR);
            out.print(" ");
            objRenderer.render(synonym, pageURL, out);
        }
    }

    @Override
    protected void renderFooter(URL pageURL, PrintWriter out) {
        if (getSubDocletCount() > 0){
            out.println("</ul>");
        }

        out.println("</li>");
    }
}
