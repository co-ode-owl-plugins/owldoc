package org.coode.browser.protege;

import org.apache.log4j.Logger;
import org.coode.html.OWLHTMLKit;
import org.coode.html.OntologyExporter;
import org.coode.html.impl.OWLHTMLKitImpl;
import org.coode.owl.mngr.OWLServer;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ui.progress.BackgroundTask;
import org.protege.editor.core.ui.util.NativeBrowserLauncher;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

import java.awt.event.ActionEvent;
import java.io.File;
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
 * Date: Jun 11, 2007<br><br>
 * <p/>
 */
public class ExportOWLDocAction extends ProtegeOWLAction {

    // as all URLs in links should be relative, this should not matter
    private static URL DEFAULT_BASE;

    static {
        try {
            DEFAULT_BASE = new URL("http://www.co-ode.org/");
        }
        catch (MalformedURLException e) {
            Logger.getLogger(ProtegeServerImpl.class).error(e);
        }
    }

    public void actionPerformed(ActionEvent actionEvent) {
        final File folder = UIUtil.chooseFolder(getOWLWorkspace(), "Select a base for OWLDoc");
        if (folder != null){
            final BackgroundTask exportTask = ProtegeApplication.getBackgroundTaskManager().startTask("Exporting OWLDoc");
            Runnable export = new Runnable(){
                public void run() {
                    try {
                        OWLServer svr = new ProtegeServerImpl(getOWLModelManager());
                        OWLHTMLKit owlhtmlKit = new OWLHTMLKitImpl("owldoc-kit", svr, DEFAULT_BASE);
                        OntologyExporter exporter = new OntologyExporter(owlhtmlKit);
                        File index = exporter.export(folder);
                        ProtegeApplication.getBackgroundTaskManager().endTask(exportTask);
                        NativeBrowserLauncher.openURL("file://" + index.getPath());
                        svr.dispose();
                    }
                    catch (Throwable e) {
                        ProtegeApplication.getErrorLog().handleError(Thread.currentThread(), e);
                    }
                }
            };

            Thread exportThread = new Thread(export, "Export OWLDoc");
            exportThread.start();
        }
    }

    public void initialise() throws Exception {
        // do nothing
    }

    public void dispose() {
        // do nothing
    }
}