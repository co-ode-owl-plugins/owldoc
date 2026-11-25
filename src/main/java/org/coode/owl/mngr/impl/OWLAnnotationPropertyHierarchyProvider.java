package org.coode.owl.mngr.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coode.owl.mngr.ActiveOntologyProvider;
import org.coode.owl.mngr.HierarchyProvider;
import org.coode.owl.mngr.OWLServer;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
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
public class OWLAnnotationPropertyHierarchyProvider implements HierarchyProvider<OWLAnnotationProperty> {

   private OWLServer server;

    private Set<OWLAnnotationProperty> implicitRoots;

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
                    if (change.getAxiom() instanceof OWLAnnotationAssertionAxiom){
                        reset();
                        return;
                    }
                }
            }
        }
    };


    public OWLAnnotationPropertyHierarchyProvider(OWLServer server) {
        this.server = server;
        server.addActiveOntologyListener(serverListener);
        server.getOWLOntologyManager().addOntologyChangeListener(ontologyListener);
    }


    @Override
    public Class<? extends OWLAnnotationProperty> getNodeClass() {
        return OWLAnnotationProperty.class;
    }

    @Override
    public Set<OWLAnnotationProperty> getRoots() {
        if (implicitRoots == null){
            implicitRoots = new HashSet<>();
            for (OWLOntology ont : getOntologies()){
                implicitRoots.addAll(ont.getAnnotationPropertiesInSignature());
            }
        }
        return implicitRoots;
    }

    @Override
    public boolean isRoot(OWLAnnotationProperty node) {
        return true;
    }

    @Override
    public boolean isLeaf(OWLAnnotationProperty node) {
        return true;
    }


    @Override
    public Set<OWLAnnotationProperty> getParents(OWLAnnotationProperty node) {
        return Collections.emptySet();
    }


    @Override
    public Set<OWLAnnotationProperty> getChildren(OWLAnnotationProperty node) {
        return Collections.emptySet();
    }


    @Override
    public Set<OWLAnnotationProperty> getEquivalents(OWLAnnotationProperty node) {
        return Collections.emptySet();
    }


    @Override
    public Set<OWLAnnotationProperty> getDescendants(OWLAnnotationProperty node) {
        return Collections.emptySet();
    }


    @Override
    public Set<OWLAnnotationProperty> getAncestors(OWLAnnotationProperty node) {
        return Collections.emptySet();
    }

    @Override
    public boolean hasAncestor(OWLAnnotationProperty node, OWLAnnotationProperty ancestor) {
        return false;
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
        implicitRoots = null;
    }
}
