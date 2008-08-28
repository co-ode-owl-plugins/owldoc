package org.coode.browser.protege;

import org.apache.log4j.Logger;
import org.coode.html.OWLHTMLServer;
import org.coode.html.impl.OWLHTMLConstants;
import org.coode.html.url.StaticFilesURLScheme;
import org.coode.html.url.URLScheme;
import org.coode.owl.mngr.*;
import org.coode.owl.mngr.impl.OWLNamedObjectFinderImpl;
import org.coode.owl.mngr.impl.ServerPropertiesImpl;
import org.coode.owl.mngr.impl.ToldPropertyHierarchyReasoner;
import org.coode.owl.util.OWLObjectComparator;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.*;
import org.semanticweb.owl.util.ToldClassHierarchyReasoner;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

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
public class ProtegeOntologyServer implements OWLHTMLServer {

    // as all URLs in links should be relative, this should not matter
    private static URL DEFAULT_BASE;

    static {
        try {
            DEFAULT_BASE = new URL("http://www.co-ode.org/");
        }
        catch (MalformedURLException e) {
            Logger.getLogger(ProtegeOntologyServer.class).error(e);
        }
    }

    private OWLModelManager mngr;

    private OWLObjectComparator<OWLObject> comp;

    private URLScheme urlScheme;

    private OWLNameMapper nameMapper;

    private ToldClassHierarchyReasoner toldClassHierarchyReasoner;

    private ToldPropertyHierarchyReasoner toldPropertyHierarchyProvider;

    private NamedObjectShortFormProvider shortformProvider;

    private String label;

    private Map<String, OWLDescriptionParser> parserMap = new HashMap<String, OWLDescriptionParser>();

    private ServerProperties properties;

    private OWLNamedObjectFinder finder;


    public ProtegeOntologyServer(OWLModelManager mngr) {
        this.mngr = mngr;
        properties = new ServerPropertiesImpl();
        properties.set(OWLHTMLConstants.OPTION_CONTENT_WINDOW, OWLHTMLConstants.LinkTarget.content.toString());
        properties.set(OWLHTMLConstants.OPTION_INDEX_ALL_URL, OWLHTMLConstants.DEFAULT_INDEX_ALL_URL);
        properties.set(OWLHTMLConstants.OPTION_DEFAULT_CSS, OWLHTMLConstants.CSS_DEFAULT);
        properties.set(OWLHTMLConstants.OPTION_SHOW_MINI_HIERARCHIES, ServerConstants.TRUE);
        properties.set(OWLHTMLConstants.OPTION_RENDER_SUBS, ServerConstants.TRUE);

    }

    public String getID() {
        return "ID" + Math.random();
    }

    public OWLOntology getActiveOntology() {
        return mngr.getActiveOntology();
    }

    public OWLOntology getOntology(URI uri) {
        return mngr.getOWLOntologyManager().getOntology(uri);
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

    public ToldClassHierarchyReasoner getClassHierarchyProvider() {
        if (toldClassHierarchyReasoner == null){
            toldClassHierarchyReasoner = new ToldClassHierarchyReasoner(mngr.getOWLOntologyManager());
            toldClassHierarchyReasoner.loadOntologies(mngr.getOntologies());
            toldClassHierarchyReasoner.classify();
        }
        return toldClassHierarchyReasoner;
    }


    public ToldPropertyHierarchyReasoner getPropertyHierarchyProvider() {
        if (toldPropertyHierarchyProvider == null){
            toldPropertyHierarchyProvider = new ToldPropertyHierarchyReasoner(mngr.getOWLOntologyManager());
        }
        return toldPropertyHierarchyProvider;
    }


    public Comparator<OWLObject> getComparator() {
        if (comp == null){
            comp = new OWLObjectComparator<OWLObject>(this);
        }
        return comp;
    }


    public OWLNamedObjectFinder getFinder() {
        if (finder == null){
            finder = new OWLNamedObjectFinderImpl(getNameMapper(), this){
                public Set<? extends OWLNamedObject> getOWLNamedObjects(String s, NamedObjectType type) {
                    switch(type){
                        case classes: return Collections.singleton(mngr.getOWLClass(s));
                        case objectproperties: return Collections.singleton(mngr.getOWLObjectProperty(s));
                        case dataproperties: return Collections.singleton(mngr.getOWLDataProperty(s));
                        case individuals: return Collections.singleton(mngr.getOWLIndividual(s));
                    }
                    return Collections.emptySet();
                }
            };
        }
        return finder;
    }


    public URLScheme getURLScheme() {
        if (urlScheme == null){
            urlScheme = new StaticFilesURLScheme(this);
        }
        return urlScheme;
    }


    public void setURLScheme(URLScheme urlScheme) {
        this.urlScheme = urlScheme;
    }


    public Set<OWLOntology> getVisibleOntologies() {
        return mngr.getActiveOntologies();
    }


    public void setOntologyVisible(OWLOntology owlOntology, boolean b) {
        throw new NotImplementedException(); // we never ask to hide certain ontologies
    }


    public void setCurrentLabel(String string) {
        label = string;
    }


    public String getCurrentLabel() {
        return label;
    }


    public OWLNameMapper getNameMapper() {
        if (nameMapper == null){
            nameMapper = new ProtegeNameMapperWrapper(mngr);
        }
        return nameMapper;
    }

    public URL getBaseURL() {
        return DEFAULT_BASE;
    }

    public NamedObjectShortFormProvider getNameRenderer() {
        if (shortformProvider == null){
            shortformProvider = new ProtegeShortformProviderWrapper(mngr);
        }
        return shortformProvider;
    }


    public OWLDescriptionParser getDescriptionParser(String string) {
        return parserMap.get(string);
    }


    public void registerDescriptionParser(String string, OWLDescriptionParser owlDescriptionParser) {
        parserMap.put(string, owlDescriptionParser);
    }


    public Set<String> getSupportedSyntaxes() {
        return parserMap.keySet();
    }


    public ServerProperties getProperties() {
        return properties;
    }


    public void clear() {
        if (nameMapper != null){
            nameMapper.dispose();
            nameMapper = null;
        }
        if (shortformProvider != null){
            shortformProvider.dispose();
            shortformProvider = null;
        }
        if (toldClassHierarchyReasoner != null){
            toldClassHierarchyReasoner.dispose();
            toldClassHierarchyReasoner = null;
        }
        if (toldPropertyHierarchyProvider != null){
            try {
                toldPropertyHierarchyProvider.dispose();
            }
            catch (OWLReasonerException e) {
                e.printStackTrace();
            }
            toldPropertyHierarchyProvider = null;
        }

        urlScheme = null;
        comp = null;
    }

    public void loadOntology(URI ontPhysicalURI) throws OWLOntologyCreationException {
        mngr.getOWLOntologyManager().loadOntologyFromPhysicalURI(ontPhysicalURI);
    }

    public void removeOntology(URI uri) {
        mngr.getOWLOntologyManager().removeOntology(uri);
    }


    public void clearOntologies() {
        throw new NotImplementedException();
    }


    public void removeServerListener(OWLServerListener owlServerListener) {
        throw new NotImplementedException();
    }

    public void addServerListener(OWLServerListener owlServerListener) {
        throw new NotImplementedException();
    }

    public void dispose() {
        clear();
    }

    class ProtegeOWLEntityRenderer implements NamedObjectShortFormProvider{
        public String getShortForm(OWLNamedObject owlNamedObject) {
            return mngr.getRendering(owlNamedObject);
        }

        public String getShortForm(OWLEntity owlEntity) {
            return mngr.getRendering(owlEntity);
        }

        public void dispose() {
        }
    }
}
