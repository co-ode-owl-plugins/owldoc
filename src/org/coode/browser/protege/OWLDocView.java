package org.coode.browser.protege;

import org.apache.log4j.Logger;
import org.coode.browser.NativeBrowserLaunch;
import org.coode.html.AbstractHTMLPageRenderer;
import org.coode.html.OWLHTMLServer;
import org.coode.html.hierarchy.OWLClassHierarchyHTMLPageRenderer;
import org.coode.html.summary.OWLClassSummaryHTMLPageRenderer;
import org.coode.html.summary.OWLDataPropertySummaryHTMLPageRenderer;
import org.coode.html.summary.OWLIndividualSummaryHTMLPageRenderer;
import org.coode.html.summary.OWLObjectPropertySummaryHTMLPageRenderer;
import org.coode.html.url.ServletURLMapper;
import org.coode.html.url.URLMapper;
import org.semanticweb.owl.model.*;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

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
 * Date: Jun 22, 2007<br><br>
 * <p/>
 */
public class OWLDocView extends AbstractBrowserView {

    private static final Logger logger = Logger.getLogger(OWLDocView.class);

    private static final String OWLDOC_CSS = "resources/owldocview.css";

    private URL BASE_URL;

    {
        try {
            BASE_URL = new URL("http://www.co-ode.org/ontologyserver/)");
        }
        catch (MalformedURLException e) {
            logger.error(e);
        }
    }

    private OWLHTMLServer server;

    private PipedReader r;
    private PrintWriter w;

    private OWLEntity entity;

    private URLMapper urlMapper;

    private boolean renderSubs = false;

    private HyperlinkListener linkListener = new HyperlinkListener(){
        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
                final URL linkURL = event.getURL();
                if (!linkURL.equals(server.getURLMapper().getURLForEntity(entity))){
                    if (linkURL.toString().endsWith("#")){ //@@TODO tidyup - this is a hack for now
                        renderSubs = true;
                        refresh(server.getURLMapper().getEntityForURL(linkURL));
                    }
                    else{
                        renderSubs = false;
                        updateGlobalSelection(linkURL);
                    }
                }
            }
        }
    };

    protected void initialiseOWLView() throws Exception {
        super.initialiseOWLView();

        getBrowser().setNavigateActive(false);

        server = new ProtegeOntologyServer(getOWLModelManager()){
            public URLMapper getURLMapper() {
                if (urlMapper == null){
                    urlMapper = new ServletURLMapper(server, BASE_URL);
                }
                return urlMapper;
            }
        };

        getBrowser().addLinkListener(linkListener);

        refresh(getOWLWorkspace().getOWLSelectionModel().getSelectedEntity());
    }

    protected String getCSS() {
        return OWLDOC_CSS;
    }

    protected void disposeOWLView() {
        super.disposeOWLView();
        getBrowser().removeLinkListener(linkListener);
    }

    protected void refresh(OWLEntity entity) {

        this.entity = entity;

        if (entity != null){
            Runnable generateHTML = new Runnable(){
                public void run() {
                    try{
                        AbstractHTMLPageRenderer ren = getRenderer();
                        ren.render(w);
                        w.close();
                    }
                    catch(Throwable e){
                        logger.error(e);
                    }
                }
            };

            try {
                r = new PipedReader();
                w = new PrintWriter(new PipedWriter(r));
                new Thread(generateHTML).start();
                getBrowser().setContent(r, server.getURLMapper().getURLForEntity(entity));
            }
            catch (Exception e) {
                logger.error(e);
            }
        }
        else{
            getBrowser().clear();
        }
    }

    private AbstractHTMLPageRenderer getRenderer(){
        AbstractHTMLPageRenderer ren = null;
        if (entity instanceof OWLClass){
            OWLClassHierarchyHTMLPageRenderer clsHierarchyRen = new OWLClassHierarchyHTMLPageRenderer(server);
            clsHierarchyRen.setRenderHiddenSubs(false);
            clsHierarchyRen.setAutoExpandSubs(renderSubs);
            ren = new OWLClassSummaryHTMLPageRenderer(server);
            ((OWLClassSummaryHTMLPageRenderer)ren).setOWLHierarchyRenderer(clsHierarchyRen);
        }
        else if (entity instanceof OWLObjectProperty){
            ren = new OWLObjectPropertySummaryHTMLPageRenderer(server);
        }
        else if (entity instanceof OWLDataProperty){
            ren = new OWLDataPropertySummaryHTMLPageRenderer(server);
        }
        else if (entity instanceof OWLIndividual){
            ren = new OWLIndividualSummaryHTMLPageRenderer(server);
        }

        if (ren != null){
            ren.setOWLObject(entity);
        }
        return ren;
    }

    private void updateGlobalSelection(URL url) {
        OWLEntity tempEntity = server.getURLMapper().getEntityForURL(url);
        if (tempEntity != null){
            entity = tempEntity;
            getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(entity);
        }
        else{
            NativeBrowserLaunch.openURL(url.toString());
        }
    }
}
