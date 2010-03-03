package org.coode.browser.protege;

import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.coode.owl.mngr.HierarchyProvider;
import org.coode.owl.mngr.OWLClassExpressionParser;
import org.coode.owl.mngr.OWLServer;
import org.coode.owl.mngr.OWLServerListener;
import org.coode.owl.mngr.ServerPropertiesAdapter;
import org.coode.owl.mngr.ServerProperty;
import org.coode.owl.mngr.impl.OWLEntityFinderImpl;
import org.coode.owl.mngr.impl.ServerPropertiesAdapterImpl;
import org.coode.owl.mngr.impl.ServerPropertiesImpl;
import org.coode.owl.util.OWLObjectComparator;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Nick Drummond<br>
 *
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 7, 2007<br><br>
 * <p/>
 */
public class ProtegeServerImpl implements OWLServer {

    private OWLModelManager mngr;

    private OWLObjectComparator<OWLObject> comp;

    private HierarchyProvider<OWLClass> toldClassHierarchyReasoner;
    private HierarchyProvider<OWLObjectProperty> toldObjectPropertyHierarchyProvider;
    private HierarchyProvider<OWLDataProperty> toldDataPropertyHierarchyProvider;

    private ShortFormProvider shortformProvider;

    private Map<String, OWLClassExpressionParser> parserMap = new HashMap<String, OWLClassExpressionParser>();

    private ServerPropertiesAdapter<ServerProperty> properties;

    private org.coode.owl.mngr.OWLEntityFinder finder;

    private OntologyIRIShortFormProvider ontologyShortFormProvider;

    private BidirectionalShortFormProvider nameCache;



    public ProtegeServerImpl(OWLModelManager mngr) {
        this.mngr = mngr;

    }


    public OWLOntology getActiveOntology() {
        return mngr.getActiveOntology();
    }

    public void setActiveOntology(OWLOntology ontology) {
        mngr.setActiveOntology(ontology);
    }

    public Set<OWLOntology> getOntologies() {
        return mngr.getOntologies();
    }

    public Set<OWLOntology> getActiveOntologies() {
        return mngr.getActiveOntologies();
    }

    public OWLOntologyManager getOWLOntologyManager() {
        return mngr.getOWLOntologyManager();
    }

    public OWLReasoner getOWLReasoner() {
        return mngr.getOWLReasonerManager().getCurrentReasoner();
    }


    public HierarchyProvider<OWLClass> getClassHierarchyProvider() {
        if (toldClassHierarchyReasoner == null){
            toldClassHierarchyReasoner = new HierarchyProviderAdapter<OWLClass>(mngr.getOWLHierarchyManager().getOWLClassHierarchyProvider());
        }
        return toldClassHierarchyReasoner;        
    }


    public HierarchyProvider<OWLObjectProperty> getOWLObjectPropertyHierarchyProvider() {
        if (toldObjectPropertyHierarchyProvider == null){
            toldObjectPropertyHierarchyProvider = new HierarchyProviderAdapter<OWLObjectProperty>(mngr.getOWLHierarchyManager().getOWLObjectPropertyHierarchyProvider());
        }
        return toldObjectPropertyHierarchyProvider;
    }


    public HierarchyProvider<OWLDataProperty> getOWLDataPropertyHierarchyProvider() {
        if (toldDataPropertyHierarchyProvider == null){
            toldDataPropertyHierarchyProvider = new HierarchyProviderAdapter<OWLDataProperty>(mngr.getOWLHierarchyManager().getOWLDataPropertyHierarchyProvider());
        }
        return toldDataPropertyHierarchyProvider;
    }


    public Comparator<OWLObject> getComparator() {
        if (comp == null){
            comp = new OWLObjectComparator<OWLObject>(this);
        }
        return comp;
    }


    public org.coode.owl.mngr.OWLEntityFinder getFinder() {
        if (finder == null){
            finder = new OWLEntityFinderImpl(getNameCache(), this);
        }
        return finder;
    }


    public OWLEntityChecker getOWLEntityChecker() {
        throw new UnsupportedOperationException();
    }


    public ShortFormProvider getShortFormProvider() {
        if (shortformProvider == null){
            shortformProvider = new ProtegeShortformProviderWrapper(mngr);
        }
        return shortformProvider;
    }


    public OntologyIRIShortFormProvider getOntologyShortFormProvider() {
        if (ontologyShortFormProvider == null){
            ontologyShortFormProvider = new OntologyIRIShortFormProvider(){
                public String getShortForm(OWLOntology owlOntology) {
                    return mngr.getRendering(owlOntology);
                }
            };
        }
        return ontologyShortFormProvider;
    }


    public OWLClassExpressionParser getClassExpressionParser(String s) {
        throw new UnsupportedOperationException();
    }


    public void registerDescriptionParser(String s, OWLClassExpressionParser owlClassExpressionParser) {
        throw new UnsupportedOperationException();
    }


    public Set<String> getSupportedSyntaxes() {
        return parserMap.keySet();
    }


    public ServerPropertiesAdapter<ServerProperty> getProperties() {

        if (properties == null){
            properties = new ServerPropertiesAdapterImpl<ServerProperty>(new ServerPropertiesImpl());
        }
        return properties;
    }


    public void clear() {
        if (shortformProvider != null){
            shortformProvider.dispose();
            shortformProvider = null;
        }
        if (toldClassHierarchyReasoner != null){
            toldClassHierarchyReasoner.dispose();
            toldClassHierarchyReasoner = null;
        }
        if (toldObjectPropertyHierarchyProvider != null){
                toldObjectPropertyHierarchyProvider.dispose();
            toldObjectPropertyHierarchyProvider = null;
        }

        comp = null;
    }

    public void loadOntology(URI ontPhysicalURI) throws OWLOntologyCreationException {
        throw new UnsupportedOperationException();
    }


    public void removeOntology(OWLOntology owlOntology) {
        throw new UnsupportedOperationException();
    }


    public void clearOntologies() {
        throw new UnsupportedOperationException();
    }


    public void removeServerListener(OWLServerListener owlServerListener) {
        throw new UnsupportedOperationException();
    }

    public void addServerListener(OWLServerListener owlServerListener) {
        throw new UnsupportedOperationException();
    }

    public void dispose() {
        clear();
    }


    public boolean isDead() {
        return false;
    }


    private BidirectionalShortFormProvider getNameCache() {
        if (nameCache == null){
            nameCache = new ProtegeBidirectionalShortFormProvider(mngr);
        }
        return nameCache;
    }
}
