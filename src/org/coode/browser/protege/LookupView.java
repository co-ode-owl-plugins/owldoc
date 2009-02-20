package org.coode.browser.protege;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.ui.UIHelper;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLNamedObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
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
public class LookupView extends AbstractBrowserView {

    private static final String PROTEGE_DEFAULT_CSS = "resources/protege-default.css";

    private static final String[] DEFAULT_LABELS = {
            "Wordnet",
            "Wikipedia",
            "Google",
            "GO Terms",
            //Altavista,
            "..."};

    private static final String[] DEFAULT_BASES =
            {"http://wordnet.princeton.edu/perl/webwn?s=",
             "http://en.wikipedia.org/wiki/",
             "http://www.google.com/search?q=",
             "http://amigo.geneontology.org/cgi-bin/amigo/search.cgi?action=new-search&amp;search_query=",
             //"http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?search_constraint=terms&amp;action=query&amp;view=query&amp;query=",
             //"http://www.altavista.com/web/results?q=",
             ""};

    private static final Map<String, String> map = new HashMap<String, String>();

    private JComboBox resourceCombo;

    private ItemListener itemListener = new ItemListener(){
        public void itemStateChanged(ItemEvent itemEvent) {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED){
                if (itemEvent.getItem().equals("...")){
                    try {
                        URI uri = new UIHelper(getOWLEditorKit()).getURI("Lookup Base", "Please add a new base to lookup from");
                        if (uri != null){
                            URL url = uri.toURL();
                            map.put(url.toString(), url.toString());
                            resourceCombo.insertItemAt(url.toString(), DEFAULT_LABELS.length-2);
                            resourceCombo.setSelectedItem(url.toString());
                        }
                    }
                    catch (Exception e) {
                        ProtegeApplication.getErrorLog().handleError(Thread.currentThread(), e);                        
                    }
                }
                refresh(getOWLWorkspace().getOWLSelectionModel().getSelectedEntity());
            }
        }
    };

    protected void initialiseOWLView() throws Exception {
        super.initialiseOWLView();

        // best without the toolbar?
        //getBrowser().showToolBar(true);

        resourceCombo = new JComboBox();

        for (int i=0; i< DEFAULT_LABELS.length; i++) {
            map.put(DEFAULT_LABELS[i], DEFAULT_BASES[i]);
            resourceCombo.addItem(DEFAULT_LABELS[i]);
        }
        resourceCombo.addItemListener(itemListener);

        add(resourceCombo, BorderLayout.NORTH);

        refresh(getOWLWorkspace().getOWLSelectionModel().getSelectedEntity());
    }

    protected String getCSS() {
        return PROTEGE_DEFAULT_CSS;
    }

    protected void disposeOWLView() {
        super.disposeOWLView();
        resourceCombo.removeItemListener(itemListener);
    }

    protected void refresh(OWLNamedObject entity) {
        if (entity != null && entity instanceof OWLEntity){
            String base = map.get(resourceCombo.getSelectedItem());
            final URL query;
            try {
                String entityRendering = getOWLModelManager().getRendering(entity);
                entityRendering = entityRendering.replaceAll(" ", "%20"); // in case the renderer does not do this
                entityRendering = entityRendering.replaceAll("_", "%20"); // in case the renderer does not do this
                // @@TODO separate camel notation based on caps
                query = new URL(base + entityRendering);

                // do the loading by hand (needs to be put into a thread)
                // @@TODO can we filter the content to remove harmful content?
//                InputStream httpStream = query.openStream();
//                URLConnection httpConnection = query.openConnection();
//                System.out.println("httpConnection.getContentLength() = " + httpConnection.getContentLength());

//                PlainDocument doc = new PlainDocument();
//                OutputStream outStream = new PipedOutputStream();
//                doc.dump(new PrintStream(outStream));
//                .....

//                InputStreamReader r = new InputStreamReader(httpStream);
//                InputStream filteredStream = new InputStream(){
//
//                    public int read() throws IOException {
//                        return 0;  //@@TODO implement
//                    }
//                };
//
//                char[] buffer = new char[1000];
//                while(r.ready()){
//                    r.read(buffer);
//                    String str = new String(buffer);
//                    str = str.replaceAll("<script type=\"text/javascript\">", "<script type=\"text/javascript\"><!--");
//
//                }

//                getBrowser().setContent(new StringReader(str), query);

                // let the JEditorPane do the loading (the browser does this in another thread)
                getBrowser().setURL(query);
            }
            catch (Exception e) {
                ProtegeApplication.getErrorLog().handleError(Thread.currentThread(), e);
            }
        }
    }
}
