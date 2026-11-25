package org.coode.owl.mngr.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.coode.owl.mngr.HierarchyProvider;
import org.coode.owl.mngr.OWLServer;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jul 26, 2010<br><br>
 */
public class OntologyHierarchyProvider implements HierarchyProvider<OWLOntology> {

    private OWLServer server;

    public OntologyHierarchyProvider(OWLServer server) {
        this.server = server;
    }

    @Override
    public Class<? extends OWLOntology> getNodeClass() {
        return OWLOntology.class;
    }

    @Override
    public Set<OWLOntology> getRoots() {
        return Collections.singleton(server.getRootOntology());
    }

    @Override
    public boolean isRoot(OWLOntology node) {
        return getRoots().contains(node);
    }

    @Override
    public boolean isLeaf(OWLOntology node) {
        return getChildren(node).isEmpty();
    }

    @Override
    public Set<OWLOntology> getChildren(OWLOntology node) {
        return node.getDirectImports();
    }

    @Override
    public Set<OWLOntology> getEquivalents(OWLOntology node) {
        return Collections.emptySet();
    }

    @Override
    public Set<OWLOntology> getDescendants(OWLOntology node) {
        return node.getImports();
    }

    @Override
    public Set<OWLOntology> getParents(OWLOntology node) {
        Set<OWLOntology> parents = new HashSet<>();
        for (OWLOntology parent : server.getOntologies()){
            if (parent.getDirectImports().contains(node)){
                parents.add(parent);
            }
        }
        return parents;
    }

    @Override
    public Set<OWLOntology> getAncestors(OWLOntology node) {
        Set<OWLOntology> ancestors = new HashSet<>();
        for (OWLOntology ancestor : server.getOntologies()){
            if (ancestor.getImports().contains(node)){
                ancestors.add(ancestor);
            }
        }
        return ancestors;
    }

    @Override
    public boolean hasAncestor(OWLOntology node, OWLOntology ancestor) {
        return !node.equals(ancestor) && ancestor.getImports().contains(node);
    }

    @Override
    public void dispose() {
        server = null;
    }
}
