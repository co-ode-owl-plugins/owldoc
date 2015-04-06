package org.coode.browser.protege;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.coode.browser.MiniBrowser;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

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
public abstract class AbstractBrowserView extends AbstractOWLViewComponent {
    private static final long serialVersionUID = 1L;
    protected MiniBrowser browser;

    private OWLSelectionModelListener selectionListener = new OWLSelectionModelListener(){
        @Override
        public void selectionChanged() {
            refresh(getOWLWorkspace().getOWLSelectionModel().getSelectedEntity());
        }
    };

    private OWLModelManagerListener modelManagerListener = new OWLModelManagerListener(){
        @Override
        public void handleChange(OWLModelManagerChangeEvent event) {
            if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)){
                browser.setURL(null);
            }
        }
    };

    private OWLOntologyChangeListener ontologyModelChangeListener = new OWLOntologyChangeListener(){
        @Override
        public void ontologiesChanged(java.util.List<? extends OWLOntologyChange> changes) {
            refresh(getOWLWorkspace().getOWLSelectionModel().getSelectedEntity());
        }
    };

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout(6, 6));

        browser = new MiniBrowser();

        String css = getCSS();
        InputStream cssStream = getClass().getClassLoader().getResourceAsStream(css);
        if (cssStream != null){
            Reader cssReader = new BufferedReader(new InputStreamReader(cssStream));
            browser.setCSS(cssReader);
        }

        add(browser, BorderLayout.CENTER);

        getOWLModelManager().addListener(modelManagerListener);
        getOWLModelManager().addOntologyChangeListener(ontologyModelChangeListener);
        getOWLWorkspace().getOWLSelectionModel().addListener(selectionListener);
    }

    protected abstract String getCSS();

    @Override
    protected void disposeOWLView() {
        getOWLWorkspace().getOWLSelectionModel().removeListener(selectionListener);
        getOWLModelManager().removeListener(modelManagerListener);
        getOWLModelManager().removeOntologyChangeListener(ontologyModelChangeListener);
    }

    protected final MiniBrowser getBrowser(){
        return browser;
    }

    protected abstract void refresh(OWLEntity entity);
}
