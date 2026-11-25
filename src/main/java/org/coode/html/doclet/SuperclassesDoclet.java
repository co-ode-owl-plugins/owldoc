/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import static org.semanticweb.owlapi.search.EntitySearcher.getSuperClasses;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.coode.html.OWLHTMLKit;
import org.coode.owl.util.OWLUtils;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 25, 2008<br><br>
 */
public class SuperclassesDoclet extends AbstractOWLElementsDoclet<OWLClass, OWLClassExpression> {

    public SuperclassesDoclet(OWLHTMLKit kit) {
        super("Superclasses", Format.list, kit);
    }

    @Override
    protected Collection<OWLClassExpression> getAssertedElements(Set<OWLOntology> onts) {
        return getSuperClasses(getUserObject(), onts.stream()).collect(java.util.stream.Collectors.toSet());
    }

    @Override
    protected String getCSSClass(OWLClassExpression object) {
        return super.getCSSClass(object);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected Collection<OWLClassExpression> getInferredElements(Set<OWLOntology> onts) {
        final OWLReasoner r = getOWLHTMLKit().getOWLServer().getOWLReasoner();

        Set<OWLClassExpression> supers = new HashSet<>();
        supers.addAll(r.getSuperClasses(getUserObject(), true).getFlattened());
        for (OWLClass ancestor : r.getSuperClasses(getUserObject(), false).getFlattened()){
            for (OWLClassExpression e : getSuperClasses(ancestor, onts.stream()).collect(java.util.stream.Collectors.toSet())) {
                if (e.isAnonymous()){
                    supers.add(e);
                }
            }
        }

        if (OWLUtils.isStructural(r)){
            for (OWLClass equiv : r.getEquivalentClasses(getUserObject())){
                for (OWLClassExpression e : getSuperClasses(equiv, onts.stream()).collect(java.util.stream.Collectors.toSet())) {
                    supers.add(e);
                }
            }
        }
        return supers;
    }
}
