package org.coode.owl.mngr.impl;

import static org.semanticweb.owlapi.search.EntitySearcher.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.coode.owl.mngr.ActiveOntologyProvider;
import org.coode.owl.mngr.HierarchyProvider;
import org.coode.owl.mngr.OWLServer;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 6, 2010<br><br>
 */
public class OWLIndividualByClassHierarchyProvider implements HierarchyProvider<OWLObject> {

    private OWLServer server;

    private Map<OWLClassExpression, Set<OWLIndividual>> cache;

    private ActiveOntologyProvider.Listener serverListener = new ActiveOntologyProvider.Listener(){
        @Override
        public void activeOntologyChanged(OWLOntology ont) {
            reset();
        }
    };

    private OWLOntologyChangeListener ontologyListener = new OWLOntologyChangeListener(){

        @Override
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
            for (OWLOntologyChange change : changes){
                if (change.isAxiomChange()){
                    if (change.getAxiom() instanceof OWLClassAssertionAxiom){
                        reset();
                        return;
                    }
                }
            }
        }
    };


    public OWLIndividualByClassHierarchyProvider(OWLServer server) {
        this.server = server;
        server.addActiveOntologyListener(serverListener);
        server.getOWLOntologyManager().addOntologyChangeListener(ontologyListener);
        reset();
    }


    @Override
    public Class<? extends OWLObject> getNodeClass() {
        return OWLNamedIndividual.class;
    }

    @Override
    public Set<OWLObject> getRoots() {
        return new HashSet<OWLObject>(cache.keySet());
    }

    @Override
    public boolean isRoot(OWLObject node) {
        return node instanceof OWLClassExpression;
    }

    @Override
    public boolean isLeaf(OWLObject node) {
        return node instanceof OWLIndividual;
    }


    @Override
    public Set<OWLObject> getParents(OWLObject node) {
        if (node instanceof OWLClassExpression){
            return Collections.emptySet();
        }
        Set<OWLObject> types = new HashSet<OWLObject>(getTypes(
                (OWLIndividual) node, getOntologies().stream()).collect(Collectors.toSet()));
        if (types.isEmpty()){
            types = Collections.<OWLObject>singleton(server.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
        }
        return types;
    }


    @Override
    public Set<OWLObject> getChildren(OWLObject node) {
        if (node instanceof OWLIndividual){
            return Collections.emptySet();
        }
        return new HashSet<OWLObject>(cache.get(node));
    }


    @Override
    public Set<OWLObject> getEquivalents(OWLObject node) {
        if (node instanceof OWLIndividual){
            Set<OWLObject> sameIndividuals = new HashSet<>();
            for (OWLOntology ont : getOntologies()){
                sameIndividuals.addAll(getSameIndividuals((OWLIndividual) node,
                        ont).collect(Collectors.toSet()));
            }
            return sameIndividuals;
        }
        return Collections.emptySet();
    }


    @Override
    public Set<OWLObject> getDescendants(OWLObject node) {
        return getChildren(node);
    }


    @Override
    public Set<OWLObject> getAncestors(OWLObject node) {
        return getParents(node);
    }

    @Override
    public boolean hasAncestor(OWLObject node, OWLObject ancestor) {
        return getParents(node).contains(ancestor);
    }


    protected Set<OWLOntology> getOntologies() {
        return server.getActiveOntologies();
    }


    @Override
    public void dispose() {
        server.getOWLOntologyManager().removeOntologyChangeListener(ontologyListener);
        server.removeActiveOntologyListener(serverListener);
        server = null;
    }


    protected void reset() {

        OWLClass owlThing = server.getOWLOntologyManager().getOWLDataFactory().getOWLThing();

        cache = new HashMap<>();

        Set<OWLIndividual> allIndividuals = new HashSet<>();

        for (OWLOntology ont : getOntologies()){
            allIndividuals.addAll(ont.getIndividualsInSignature());
        }

        for (OWLOntology ont : getOntologies()){
            for (OWLClassAssertionAxiom ax : ont.getAxioms(AxiomType.CLASS_ASSERTION)){
                if (!ax.getClassExpression().equals(owlThing)){
                    Set<OWLIndividual> inds = cache.get(ax.getClassExpression());
                    if (inds == null){
                        inds = new HashSet<>();
                        cache.put(ax.getClassExpression(), inds);
                    }

                    inds.add(ax.getIndividual());

                    allIndividuals.remove(ax.getIndividual());
                }
            }
        }

        // any individuals left with no asserted type are added to owl:Thing
        cache.put(owlThing, allIndividuals);
    }
}
