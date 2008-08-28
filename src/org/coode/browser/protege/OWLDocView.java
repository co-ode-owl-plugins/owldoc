package org.coode.browser.protege;

import org.apache.log4j.Logger;
import org.coode.html.OWLHTMLServer;
import org.coode.html.doclet.HTMLDoclet;
import org.coode.html.doclet.HierarchyRootDoclet;
import org.coode.html.hierarchy.OWLClassHierarchyTreeFragment;
import org.coode.html.hierarchy.TreeFragment;
import org.coode.html.impl.OWLHTMLConstants;
import org.coode.html.summary.*;
import org.coode.html.url.ServletURLScheme;
import org.coode.html.url.URLScheme;
import org.coode.html.util.URLUtils;
import org.coode.owl.mngr.NamedObjectType;
import org.protege.editor.core.ui.util.NativeBrowserLauncher;
import org.protege.editor.core.ui.view.DisposableAction;
import org.semanticweb.owl.model.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

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

//    private URL BASE_URL;

//    {
//        try {
//            BASE_URL = new URL("http://www.co-ode.org/ontologyserver/)");
//        }
//        catch (MalformedURLException e) {
//            logger.error(e);
//        }
//    }

    private OWLHTMLServer server;

    private PipedReader r;
    private PrintWriter w;

    private OWLNamedObject entity;

    private URLScheme urlScheme;

    private boolean renderSubs = false;

    private HyperlinkListener linkListener = new HyperlinkListener(){
        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
                final URL linkURL = event.getURL();
                if (!linkURL.equals(server.getURLScheme().getURLForNamedObject(entity))){
                    if (linkURL.toString().endsWith("#")){ //@@TODO tidyup - this is a hack for now
                        renderSubs = true;
                        refresh(server.getURLScheme().getNamedObjectForURL(linkURL));
                    }
                    else{
                        renderSubs = false;
                        updateGlobalSelection(linkURL);
                    }
                }
            }
        }
    };

    private DisposableAction srcAction = new DisposableAction("Show source", null){
        public void dispose() {
            // do nothing
        }

        public void actionPerformed(ActionEvent event) {
            handleShowSrc();
        }
    };


    private void handleShowSrc() {
        HTMLDoclet ren = getRenderer();
        final StringWriter stringWriter = new StringWriter();
        PrintWriter stringRenderer = new PrintWriter(stringWriter);
        ren.renderAll(server.getURLScheme().getURLForNamedObject(OWLDocView.this.entity), stringRenderer);
        stringRenderer.flush();
        JTextArea t = new JTextArea(stringWriter.getBuffer().toString());
        t.setWrapStyleWord(true);
        final JScrollPane scroller = new JScrollPane(t);
        scroller.setPreferredSize(new Dimension(600, 500));
        JOptionPane.showMessageDialog(null, scroller, "Source for " + entity.getURI(), JOptionPane.OK_OPTION);
    }


    protected void initialiseOWLView() throws Exception {
        super.initialiseOWLView();

        getBrowser().setNavigateActive(false);

        server = new ProtegeOntologyServer(getOWLModelManager());
        server.getProperties().set(OWLHTMLConstants.OPTION_DEFAULT_CSS, null);
        server.setURLScheme(new ProtegeURLScheme(server));

        getBrowser().addLinkListener(linkListener);

        refresh(getOWLWorkspace().getOWLSelectionModel().getSelectedEntity());

        addAction(srcAction, "A", "A");
    }

    protected String getCSS() {
        return OWLDOC_CSS;
    }

    protected void disposeOWLView() {
        super.disposeOWLView();
        getBrowser().removeLinkListener(linkListener);
    }

    protected void refresh(OWLNamedObject entity) {

        this.entity = entity;

        if (entity != null){
            Runnable generateHTML = new Runnable(){
                public void run() {
                    try{
                        HTMLDoclet ren = getRenderer();
                        ren.renderAll(server.getURLScheme().getURLForNamedObject(OWLDocView.this.entity), w);
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
                getBrowser().setContent(r, server.getURLScheme().getURLForNamedObject(entity));
            }
            catch (Exception e) {
                logger.error(e);
            }
        }
        else{
            getBrowser().clear();
        }
    }

    private AbstractSummaryHTMLPage getRenderer(){
        AbstractSummaryHTMLPage ren = null;
        if (entity instanceof OWLClass){
            TreeFragment tree = new OWLClassHierarchyTreeFragment(server, server.getClassHierarchyProvider(), "Asserted Class Hierarchy");
            HierarchyRootDoclet<OWLClass> clsHierarchyRen = new HierarchyRootDoclet<OWLClass>(server, tree);
//            clsHierarchyRen.setRenderHiddenSubs(false);
//            clsHierarchyRen.setAutoExpandSubs(renderSubs);
            ren = new OWLClassSummaryHTMLPage(server);
            ((OWLClassSummaryHTMLPage)ren).setOWLHierarchyRenderer(clsHierarchyRen);
        }
        else if (entity instanceof OWLObjectProperty){
            ren = new OWLObjectPropertySummaryHTMLPage(server);
        }
        else if (entity instanceof OWLDataProperty){
            ren = new OWLDataPropertySummaryHTMLPage(server);
        }
        else if (entity instanceof OWLIndividual){
            ren = new OWLIndividualSummaryHTMLPage(server);
        }

        if (ren != null){
            ren.setUserObject(entity);
        }
        return ren;
    }

    private void updateGlobalSelection(URL url) {
        OWLNamedObject tempEntity = server.getURLScheme().getNamedObjectForURL(url);
        if (tempEntity != null){
            entity = tempEntity;
            if (tempEntity instanceof OWLEntity){
                getOWLWorkspace().getOWLSelectionModel().setSelectedEntity((OWLEntity)entity);
            }
        }
        else{
            NativeBrowserLauncher.openURL(url.toString());
        }
    }


    private class ProtegeURLScheme extends ServletURLScheme {

        public ProtegeURLScheme(OWLHTMLServer server) {
            super(server);
        }


        public OWLNamedObject getNamedObjectForURL(URL url) {
            OWLNamedObject object = null;

            Map<String, String> paramMap = URLUtils.getParams(url);
            if (!paramMap.isEmpty()){
                try {
                    String uri = URLDecoder.decode(paramMap.get("uri"), "UTF-8");
                    NamedObjectType type = getType(url);

                    if (uri != null){
                        // we know the entity already exists, so just get it
                        object = type.getExistingObject(URI.create(uri), server);
                    }
                }
                catch (UnsupportedEncodingException e) {
                    logger.error(e);
                }
            }
            return object;
        }
    }

}
