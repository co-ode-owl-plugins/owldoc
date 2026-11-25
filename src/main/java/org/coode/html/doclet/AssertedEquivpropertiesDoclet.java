/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import java.util.Collection;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 25, 2008<br><br>
 */
public class AssertedEquivpropertiesDoclet<O extends OWLProperty> extends AbstractOWLElementsDoclet<O, O> {

    public AssertedEquivpropertiesDoclet(OWLHTMLKit kit) {
        super("Equivalent Properties", Format.list, kit);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Collection<O> getAssertedElements(Set<OWLOntology> onts) {
        O property = getUserObject();
        java.util.stream.Stream<? extends org.semanticweb.owlapi.model.OWLPropertyExpression> values;
        if (property.isOWLDataProperty()) {
            values = EntitySearcher.getEquivalentProperties(property.asOWLDataProperty(), onts.stream());
        } else if (property.isOWLAnnotationProperty()) {
            values = EntitySearcher.getEquivalentProperties(property.asOWLAnnotationProperty(), onts.stream());
        } else {
            values = EntitySearcher.getEquivalentProperties(property.asOWLObjectProperty(), onts.stream());
        }
        return values.filter(OWLProperty.class::isInstance).map(value -> (O) value)
                .collect(java.util.stream.Collectors.toSet());
    }
}
