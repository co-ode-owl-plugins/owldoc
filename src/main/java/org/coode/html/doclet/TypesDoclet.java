/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import static org.semanticweb.owlapi.search.EntitySearcher.getTypes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.coode.owl.util.OWLUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Feb 5, 2008<br><br>
 */
public class TypesDoclet extends AbstractOWLElementsDoclet<OWLNamedIndividual, OWLClassExpression> {

    public TypesDoclet(OWLHTMLKit kit) {
        super("Types", Format.list, kit);
    }

    @Override
    protected Collection<OWLClassExpression> getAssertedElements(Set<OWLOntology> onts) {
        return getTypes(getUserObject(), onts.stream()).collect(java.util.stream.Collectors.toSet());
    }

    @Override
    protected Collection<OWLClassExpression> getInferredElements(Set<OWLOntology> ontologies) {
        final OWLReasoner r = getOWLHTMLKit().getOWLServer().getOWLReasoner();

        Set<OWLClassExpression> types = new HashSet<>();
        types.addAll(r.getTypes(getUserObject(), true).getFlattened());

        if (OWLUtils.isStructural(r)){
            for (OWLNamedIndividual same : r.getSameIndividuals(getUserObject())){
                types.addAll(r.getTypes(same, true).getFlattened());
            }
        }
        return types;
    }
}
