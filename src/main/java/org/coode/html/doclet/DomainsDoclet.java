/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br>
 * <br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 25, 2008<br>
 * <br>
 */
public class DomainsDoclet<O extends OWLProperty> extends AbstractOWLElementsDoclet<O, OWLClassExpression> {

    public DomainsDoclet(OWLHTMLKit kit) {
        super("Domains", Format.list, kit);
    }

    @Override
    protected Collection<OWLClassExpression> getAssertedElements(Set<OWLOntology> onts) {
        O userObject = getUserObject();
        if (userObject.isOWLDataProperty()) {
            return EntitySearcher.getDomains(userObject.asOWLDataProperty(), onts.stream()).collect(java.util.stream.Collectors.toSet());
        }
        if (userObject.isOWLObjectProperty()) {
            return EntitySearcher.getDomains(userObject.asOWLObjectProperty(), onts.stream()).collect(java.util.stream.Collectors.toSet());
        }
        if (userObject.isObjectPropertyExpression()) {
            return EntitySearcher.getDomains((OWLObjectPropertyExpression) userObject, onts.stream()).collect(java.util.stream.Collectors.toSet());
        }
        return Collections.emptyList();
    }
}
