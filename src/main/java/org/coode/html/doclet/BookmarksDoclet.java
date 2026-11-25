/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.coode.html.bookmarks.OntologyBookmarks;
import org.coode.html.impl.OWLHTMLConstants;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Feb 11, 2008<br><br>
 */
public class BookmarksDoclet extends AbstractOWLElementsDoclet<OWLOntology, OWLEntity>{

    public BookmarksDoclet(OWLHTMLKit kit) {
        super(OWLHTMLConstants.BOOKMARKS_LABEL, ElementsDoclet.Format.list, kit);
//        setTarget(OWLHTMLConstants.LinkTarget.content);
    }

    @Override
    protected Collection<OWLEntity> getAssertedElements(Set<OWLOntology> onts) {
        Set<OWLEntity> bookmarks = new HashSet<>();
        for (OWLOntology ont : onts){
            bookmarks.addAll(new OntologyBookmarks(getOWLHTMLKit().getOWLServer().getOWLOntologyManager(), ont).getBookmarks());
        }
        return bookmarks;
    }

    @Override
    public void setUserObject(OWLOntology ontology) {
        super.setUserObject(ontology);

        if (ontology != null){
        setOntologies(Collections.singleton(ontology));
        }
        else{
              setOntologies(null);
        }
    }
}
