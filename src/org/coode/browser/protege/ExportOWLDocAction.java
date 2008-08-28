package org.coode.browser.protege;

import org.coode.html.OWLHTMLServer;
import org.coode.html.OntologyExporter;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ui.util.NativeBrowserLauncher;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

import java.awt.event.ActionEvent;
import java.io.File;

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
 * Date: Jun 11, 2007<br><br>
 * <p/>
 */
public class ExportOWLDocAction extends ProtegeOWLAction {
    
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            File folder = UIUtil.chooseFolder(getOWLWorkspace(), "Select a base for OWLDoc");
            if (folder != null){
                OWLHTMLServer svr = new ProtegeOntologyServer(getOWLModelManager());
                OntologyExporter exporter = new OntologyExporter(svr);
                File index = exporter.export(folder);
                NativeBrowserLauncher.openURL("file://" + index.getPath());
            }
        }
        catch (Throwable e) {
            ProtegeApplication.getErrorLog().handleError(Thread.currentThread(), e);            
        }
    }

    public void initialise() throws Exception {
    }

    public void dispose() {
        //@@TODO implement
    }
}