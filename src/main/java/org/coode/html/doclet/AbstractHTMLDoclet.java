/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.coode.html.page.HTMLPage;
import org.coode.html.util.HTMLUtils;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 24, 2008<br><br>
 *
 * Default handling for doclet nesting, including handing the user object down through the hierarchy.
 * Content will be surrounded by whatever is created in renderHeader and renderFooter
 */
public abstract class AbstractHTMLDoclet<O> implements NestedHTMLDoclet<O> {

    private boolean pinned = false;

    private List<HTMLDoclet<O>> doclets = new ArrayList<>();

    private O userObject;

    protected abstract void renderHeader(URL pageURL, PrintWriter out);
    protected abstract void renderFooter(URL pageURL, PrintWriter out);

    @Override
    public void renderContent(URL pageURL, PrintWriter out) {
        for (HTMLDoclet<O> doclet : doclets){
            doclet.renderAll(pageURL, out);
        }
    }

    @Override
    public final void renderAll(URL pageURL, PrintWriter out) {
        try{
            beforeRender();
            renderHeader(pageURL, out);
            renderContent(pageURL, out);
            renderFooter(pageURL, out);
        }
        catch (java.io.UncheckedIOException | java.util.concurrent.CancellationException e) {
            throw e;
        }
        catch (Throwable e) {
            e.printStackTrace();
            out.println("<p>" + e.getMessage() + "</p>");
        }
        finally {
            afterRender();
        }
    }

    /** Prepare state scoped to a single rendering, never to the lifetime of a view. */
    protected void beforeRender() {}

    /** Release per-render state even when rendering fails. */
    protected void afterRender() {}

    @Override
    public final void addDoclet(HTMLDoclet<O> doclet){
        if (!doclet.isPinned()){
            doclet.setUserObject(userObject);
        }
        doclets.add(doclet);
    }

    @Override
    public final void addDoclet(HTMLDoclet<O> doclet, int index){
        if (!doclet.isPinned()){
            doclet.setUserObject(userObject);
        }
        doclets.add(index, doclet);
    }

    @Override
    public final void removeDoclet(HTMLDoclet<O> doclet) {
        doclets.remove(doclet);
    }

    @Override
    public final HTMLDoclet<O> getDoclet(String id) {
        for (HTMLDoclet<O> doclet : doclets){
            if (doclet.getID().equals(id)){
                return doclet;
            }
        }
        return null;
    }

    @Override
    public int getSubDocletCount() {
        return doclets.size();
    }

    protected List<HTMLDoclet<O>> getDoclets(){
        return Collections.unmodifiableList(doclets);
    }

    @Override
    public void clear() {
        doclets.clear();
    }

    @Override
    public final int indexOf(HTMLDoclet<O> doclet) {
        return doclets.indexOf(doclet);
    }

    @Override
    public void setUserObject(O object){
        this.userObject = object;
        for (HTMLDoclet<O> doclet : doclets){
            if (!doclet.isPinned()){
                doclet.setUserObject(object);
            }
        }
    }

    @Override
    public final O getUserObject(){
        return this.userObject;
    }

    public void setPinned(boolean pinned){
        this.pinned = pinned;
    }

    @Override
    public boolean isPinned(){
        return pinned;
    }

    @Override
    public boolean isFullPage() {
        return false;
    }

    @Override
    public HTMLPage<O> asPage() {
        return null; // not a page
    }

    @Override
    public Set<URL> getRequiredCSS() {
        Set<URL> css = new HashSet<>();
        for (HTMLDoclet<O> doclet : doclets){
            css.addAll(doclet.getRequiredCSS());
        }
        return css;
    }

    @Override
    public List<URL> getRequiredJS() {
        List<URL> js = new ArrayList<>();
        for (HTMLDoclet<O> doclet : doclets){
            js.addAll(doclet.getRequiredJS());
        }
        return js;
    }

    protected final void renderBoxStart(String name, PrintWriter out, URL pageURL) {
        String id = name != null ? name.toLowerCase().replace(" ", "_") : "ID" + new Random().nextLong();
        renderBoxStart(name, id, out, pageURL);
    }

    protected void renderBoxStart(String name, String id, PrintWriter out, URL pageURL) {
        HTMLUtils.renderBoxStart(name, id, out);
    }

    protected final static void renderBoxEnd(String name, PrintWriter out) {
        HTMLUtils.renderBoxEnd(name, out);
    }
}
