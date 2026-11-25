/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.owl.mngr.impl;

import static org.semanticweb.owlapi.search.EntitySearcher.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.coode.owl.mngr.ActiveOntologyProvider;
import org.coode.owl.mngr.HierarchyProvider;
import org.coode.owl.mngr.OWLServer;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubPropertyAxiom;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 23, 2008<br><br>
 */
public abstract class AbstractPropertyHierarchyProvider<P extends OWLProperty> implements HierarchyProvider<P> {

    private OWLServer server;

    private Set<P> implicitRoots;

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
                    if (change.getAxiom() instanceof OWLSubPropertyAxiom){
                        reset();
                        return;
                    }
                }
            }
        }
    };


    public AbstractPropertyHierarchyProvider(OWLServer server) {
        this.server = server;
        server.addActiveOntologyListener(serverListener);
        server.getOWLOntologyManager().addOntologyChangeListener(ontologyListener);
    }

    protected abstract P getTopProperty();

    protected abstract Set<P> getPropertiesInSignature(OWLOntology ont);

    protected OWLServer getServer() {
        return server;
    }

    @Override
    public Set<P> getRoots() {
        return Collections.singleton(getTopProperty());
    }

    @Override
    public boolean isRoot(P node) {
        return node.equals(getTopProperty());
    }

    @Override
    public boolean isLeaf(P node) {
        return getChildren(node).isEmpty();
    }

    @Override
    public boolean hasAncestor(P node, P ancestor) {
        return getAncestors(node).contains(ancestor);
    }

    @Override
    public Set<P> getParents(P node) {
        Set<P> supers;
        if (isRoot(node)){
            supers = Collections.emptySet();
        }
        else{
            supers = new HashSet<>();
            Collection properties;
            if (node.isDataPropertyExpression()) {
                properties = getSuperProperties(node.asOWLDataProperty(),
                        getOntologies().stream()).collect(Collectors.toSet());
            } else {
                properties = getSuperProperties(node.asOWLObjectProperty(),
                        getOntologies().stream()).collect(Collectors.toSet());
            }
            for (Object pe : properties) {
                if (pe instanceof OWLProperty){
                    supers.add((P)pe); // it must be a P
                }
            }
            if (supers.isEmpty()){
                supers.addAll(getRoots());
            }
        }
        return supers;
    }


    @Override
    public Set<P> getChildren(P node) {
        if (isRoot(node)){
            return getImplicitRoots();
        }

        Set<P> subs = new HashSet<>();
        Collection properties;
        if (node.isDataPropertyExpression()) {
            properties = getSubProperties(node.asOWLDataProperty(),
                    getOntologies().stream()).collect(Collectors.toSet());
        } else {
            properties = getSubProperties(node.asOWLObjectProperty(),
                    getOntologies().stream()).collect(Collectors.toSet());
        }
        for (Object pe : properties) {
                subs.add((P)pe);
        }

        return subs;
    }


    @Override
    public Set<P> getEquivalents(P node) {
        Set<P> equivs = new HashSet<>();
        Collection properties;
        if (node.isDataPropertyExpression()) {
            properties = getEquivalentProperties(node.asOWLDataProperty(),
                    getOntologies().stream()).collect(Collectors.toSet());
        } else {
            properties = getEquivalentProperties(node.asOWLObjectProperty(),
                    getOntologies().stream()).collect(Collectors.toSet());
        }
        for (Object pe : properties) {
                equivs.add((P)pe);
        }
        return equivs;
    }


    @Override
    public Set<P> getDescendants(P node) {
        if (isRoot(node)){
            return getAllReferencedProperties();
        }

        Set<P> descendants = new HashSet<>();
        List<P> nodes = new ArrayList<>();
        nodes.add(node);
        for(int i=0; i<nodes.size(); i++){
            for (P child : getChildren(nodes.get(i))){
                if (!nodes.contains(child)){
                    nodes.add(child);
                }
            }
        }
        descendants.addAll(nodes);
        descendants.remove(node);
        return descendants;
    }

    @Override
    public Set<P> getAncestors(P node) {
        if (isRoot(node)){
            return Collections.emptySet();
        }

        Set<P> ancestors;
        List<P> nodes = new ArrayList<>();
        nodes.add(node);
        for(int i=0; i<nodes.size(); i++){
            for (P child : getParents(nodes.get(i))){
                if (!nodes.contains(child)){
                    nodes.add(child);
                }
            }
        }
        if (nodes.size() == 1){
            ancestors = getRoots();
        }
        else{
            ancestors = new HashSet<>(nodes);
            ancestors.remove(node);
        }
        return ancestors;
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

    private Set<P> getAllReferencedProperties() {
        Set<P> props = new HashSet<>();
        for (OWLOntology ont : getOntologies()){
            props.addAll(getPropertiesInSignature(ont));
        }
        return props;
    }

    private Set<P> getImplicitRoots() {
        if (implicitRoots == null){
            implicitRoots = getAllReferencedProperties();
            implicitRoots.removeAll(getRoots());
            for (Iterator i=implicitRoots.iterator(); i.hasNext();){
                P p = (P)i.next();
                Collection properties;
                if (p.isDataPropertyExpression()) {
                    properties = getSuperProperties(p.asOWLDataProperty(),
                            getOntologies().stream()).collect(Collectors.toSet());
                } else {
                    properties = getSuperProperties(p.asOWLObjectProperty(),
                            getOntologies().stream()).collect(Collectors.toSet());
                }
                if (properties.isEmpty()) {
                    // do nothing
                } else if (properties.equals(getRoots())) {
                    // do nothing
                }
                else{
                    i.remove();
                }
            }
        }
        return implicitRoots;
    }
}