/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import java.util.Collection;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 25, 2008<br><br>
 */
public class AssertedEquivalentsDoclet extends AbstractOWLElementsDoclet<OWLClass, OWLClassExpression> {

    public AssertedEquivalentsDoclet(OWLHTMLKit kit) {
        super("Equivalents", Format.list, kit);
    }

    @Override
    protected Collection<OWLClassExpression> getAssertedElements(Set<OWLOntology> onts) {
        return EntitySearcher.getEquivalentClasses(getUserObject(), onts.stream()).collect(java.util.stream.Collectors.toSet());
    }
}
