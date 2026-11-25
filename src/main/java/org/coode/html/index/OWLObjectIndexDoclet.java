package org.coode.html.index;

import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.coode.html.doclet.AbstractOWLDocDoclet;
import org.coode.html.doclet.AbstractOWLElementsDoclet;
import org.coode.html.doclet.AbstractTitleDoclet;
import org.coode.html.doclet.ElementsDoclet;
import org.coode.html.doclet.OWLSelectorDoclet;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Author: Nick Drummond<br>
 * nick.drummond@cs.manchester.ac.uk<br>
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 12, 2007<br><br>
 * <p/>
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)<br>
 * copyright 2006, The University of Manchester<br>
 */
public class OWLObjectIndexDoclet<O extends OWLObject> extends AbstractOWLDocDoclet<O> {

    protected Set<O> index = new HashSet<>();

    private AbstractOWLElementsDoclet<O, O> indexDoclet;

    protected String id = "Index";


    public OWLObjectIndexDoclet(OWLHTMLKit kit) {
        super(kit);

        OWLSelectorDoclet<O> selector = new OWLSelectorDoclet<>(kit);

        selector.addDoclet(new AbstractTitleDoclet<O>(kit){

            @Override
            public String getTitle() {
                return id + " (" + index.size() + ")";
            }

            @Override
            public String getSubtitle() {
                return null;
            }

        });

        indexDoclet = new AbstractOWLElementsDoclet<O, O>(id, ElementsDoclet.Format.list, kit) {
            @Override
            protected Collection<O> getAssertedElements(Set<OWLOntology> onts) {
                return index;
            }
            @Override
            public String getID() {
                return null;//return id + " (" + index.size() + ")";
            }
        };
        indexDoclet.setInlineMedia(false);
//        String contentWindow = getOWLHTMLKit().getHTMLProperties().get(OWLHTMLProperty.optionContentWindow);
//        if (contentWindow != null){
//            indexDoclet.setTarget(OWLHTMLConstants.LinkTarget.valueOf(contentWindow));
//        }
//        else{
//            indexDoclet.setTarget(null);
//        }
        selector.addDoclet(indexDoclet);

        addDoclet(selector);
    }

    public void add(O obj){
        index.add(obj);
    }

    public void addAll(Set<? extends O> objs){
        index.addAll(objs);
    }

    @Override
    protected void renderHeader(URL pageURL, PrintWriter out) {
        // do nothing
    }

    @Override
    protected void renderFooter(URL pageURL, PrintWriter out) {
        // do nothing
    }

    @Override
    public String getID() {
        return id;
    }

    public void setTitle(String title) {
        this.id = title;
    }
}
