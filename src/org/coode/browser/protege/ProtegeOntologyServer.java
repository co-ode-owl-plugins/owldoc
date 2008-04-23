package org.coode.browser.protege;

import org.apache.log4j.Logger;
import org.coode.html.OWLHTMLConstants;
import org.coode.html.OWLHTMLServer;
import org.coode.html.url.OWLDocURLMapper;
import org.coode.html.url.URLMapper;
import org.coode.owl.mngr.MyShortformProvider;
import org.coode.owl.mngr.OWLDescriptionParser;
import org.coode.owl.mngr.OWLNameMapper;
import org.coode.owl.mngr.OWLServerListener;
import org.coode.owl.util.OWLObjectComparator;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.NoOpReasoner;
import org.semanticweb.owl.inference.OWLClassReasoner;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.*;
import org.semanticweb.owl.util.ToldClassHierarchyReasoner;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    private OWLClassReasoner toldClassReasoner;

    private OWLObjectComparator<OWLObject> comp;

    private URLMapper urlMapper;

    private OWLNameMapper nameMapper;

    private ToldClassHierarchyReasoner toldClassHierarchyReasoner;

    private MyShortformProvider shortformProvider;
    
    private Map<String, String> options = new HashMap<String, String>();

    public ProtegeOntologyServer(OWLModelManager mngr) {
        this.mngr = mngr;
        options.put(OWLHTMLConstants.OPTION_CONTENT_WINDOW, "content");
    }

    public Map<String, String> getOptions() {
        return options;
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

    public OWLClassReasoner getOWLClassReasoner() {
        OWLClassReasoner r = mngr.getReasoner();
        if (r instanceof NoOpReasoner){
            if (toldClassReasoner == null){
                toldClassReasoner = new ToldClassHierarchyReasoner(mngr.getOWLOntologyManager());
                try {
                    toldClassReasoner.loadOntologies(mngr.getActiveOntologies());
                }
                catch (OWLReasonerException e) {
                    Logger.getLogger(ProtegeOntologyServer.class).error(e);
                }
            }
            r = toldClassReasoner;
        }
        return r;
    }

    public Comparator<OWLObject> getComparator() {
        if (comp == null){
            comp = new OWLObjectComparator<OWLObject>(this);
        }
        return comp;
    }

    public URLMapper getURLMapper() {
        if (urlMapper == null){
            urlMapper = new OWLDocURLMapper(this, DEFAULT_BASE);
        }
        return urlMapper;
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

    public MyShortformProvider getNameRenderer() {
        if (shortformProvider == null){
            shortformProvider = new ProtegeShortformProviderWrapper(mngr);
        }
        return shortformProvider;
    }

    public void setNameRenderer(MyShortformProvider sfp) {
        shortformProvider = sfp;
    }

    public OWLDescriptionParser getDescriptionParser() {
        throw new NotImplementedException();
    }

    public void loadOntology(URI ontPhysicalURI) throws OWLOntologyCreationException {
        mngr.getOWLOntologyManager().loadOntologyFromPhysicalURI(ontPhysicalURI);
    }

    public void removeOntology(URI uri) {
        mngr.getOWLOntologyManager().removeOntology(uri);
    }

    public void removeServerListener(OWLServerListener owlServerListener) {
        throw new NotImplementedException();
    }

    public void addServerListener(OWLServerListener owlServerListener) {
        throw new NotImplementedException();
    }

    public void dispose() {
        //@@TODO implement
    }

    class ProtegeOWLEntityRenderer implements MyShortformProvider{
        public String render(OWLNamedObject obj) {
            return mngr.getRendering(obj);
        }
    }
}
