/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import java.util.Collection;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 25, 2008<br><br>
 */
public class RangesDoclet<O extends OWLPropertyExpression, E extends OWLPropertyRange>
        extends AbstractOWLElementsDoclet<O, E> {

    public RangesDoclet(OWLHTMLKit kit) {
        super("Ranges", Format.list, kit);
    }

    @Override
    protected Collection<E> getAssertedElements(Set<OWLOntology> onts) {
        O userObject = getUserObject();
        if(userObject.isDataPropertyExpression()) {
            return (Collection<E>) EntitySearcher.getRanges((OWLDataProperty)userObject, onts.stream()).collect(java.util.stream.Collectors.toSet());
        }
        return (Collection<E>) EntitySearcher.getRanges((OWLObjectPropertyExpression)userObject, onts.stream()).collect(java.util.stream.Collectors.toSet());
    }
}
