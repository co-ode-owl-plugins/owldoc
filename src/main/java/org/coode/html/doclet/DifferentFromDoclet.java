/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import java.util.Collection;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Feb 5, 2008<br><br>
 */
public class DifferentFromDoclet extends AbstractOWLElementsDoclet<OWLNamedIndividual, OWLIndividual> {

    public DifferentFromDoclet(OWLHTMLKit kit) {
        super("Different From", Format.csv, kit);
    }

    @Override
    protected Collection<OWLIndividual> getAssertedElements(Set<OWLOntology> onts) {
        return EntitySearcher.getDifferentIndividuals(getUserObject(), onts.stream()).collect(java.util.stream.Collectors.toSet());
    }
}
