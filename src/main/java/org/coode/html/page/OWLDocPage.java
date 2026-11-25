package org.coode.html.page;

import java.io.PrintWriter;
import java.net.URL;

import org.coode.html.OWLHTMLKit;
import org.coode.html.doclet.MessageBoxDoclet;
import org.coode.html.doclet.TabsDoclet;
import org.coode.html.impl.OWLHTMLConstants;
import org.coode.html.impl.OWLHTMLProperty;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Author: Nick Drummond<br>
 * nick.drummond@cs.manchester.ac.uk<br>
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 7, 2007<br><br>
 * <p/>
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)<br>
 * copyright 2006, The University of Manchester<br>
 *
 * A full HTML page for a given OWL Object. Automatically adds menu and tab doclets when appropriate.
 *
 * Subclasses should not overload header and footer except for content that lies outside of the OWLDoc page.
 *
 * Content should be provided as additional doclets.
 */
public class OWLDocPage<O extends OWLObject> extends DefaultHTMLPage<O> {

    private OWLHTMLKit kit;
    private String sectionTitle;

    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }


    public OWLDocPage(OWLHTMLKit kit) {
        this.kit = kit;

        String css = kit.getHTMLProperties().get(OWLHTMLProperty.optionDefaultCSS);

        if (css != null){
            addCSS(kit.getURLScheme().getURLForRelativePage(css));
        }

//        addJavascript(kit.getURLScheme().getURLForRelativePage(OWLHTMLConstants.JQUERY_JS));
//        addJavascript(kit.getURLScheme().getURLForRelativePage(OWLHTMLConstants.JS_DEFAULT));
        if (kit.getHTMLProperties().isSet(OWLHTMLProperty.optionStaticSite)) {
            addJavascript(kit.getURLScheme().getURLForRelativePage("js/entities.js"));
            addJavascript(kit.getURLScheme().getURLForRelativePage("js/owldoc.js"));
        } else if (kit.isActive()){
            addDoclet(new TabsDoclet<O>(kit));
        }

        if (!kit.getOWLServer().getOWLReasoner().isConsistent()){
            kit.addUserError("Inconsistent Ontology! Only the structural reasoner can be used.");
        }

        for (String error : kit.getUserErrors()){
            final MessageBoxDoclet<O> messageDoclet = new MessageBoxDoclet<>(null, error);
            messageDoclet.setClass("error");
            addDoclet(messageDoclet);
        }
        kit.clearUserErrors();
    }

    @Override
    protected void renderHeader(URL pageURL, PrintWriter out) {
        super.renderHeader(pageURL, out);
        if (kit.getHTMLProperties().isSet(OWLHTMLProperty.optionStaticSite)) {
            String root = org.coode.html.util.URLUtils.createRelativeURL(pageURL, kit.getBaseURL());
            if (root.isEmpty() || root.equals(".")) root = "./";
            String context = "";
            if (sectionTitle != null && getUserObject() != null) {
                String entityPath = org.coode.html.util.URLUtils.createRelativeURL(kit.getBaseURL(), kit.getURLScheme().getURLForOWLObject(getUserObject()));
                context = " data-entity-path=\"" + entityPath.replace("&", "&amp;").replace("\"", "&quot;")
                        + "\" data-section=\"" + sectionTitle.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;") + "\"";
            }
            out.println("<div id=\"owldoc-page\" data-root=\"" + root.replace("&", "&amp;").replace("\"", "&quot;") + "\"" + context + ">");
            out.println("<noscript><p>Browse documentation: <a href=\"" + root + "contents.html\">Contents</a></p></noscript>");
        }
    }

    @Override
    protected final void renderFooter(URL pageURL, PrintWriter out) {

        out.println("\n<p class='footer'>");
        out.print("<a href='");
        out.print("https://github.com/protegeproject/owldoc");
        out.println("' target='_blank' rel='noopener'>Generated with OWLDoc</a>");
        out.println("</p>");

        if (kit.getHTMLProperties().isSet(OWLHTMLProperty.optionStaticSite)) out.println("</div>");

        super.renderFooter(pageURL, out);
    }

    protected final OWLHTMLKit getOWLHTMLKit() {
        return kit;
    }
    
    public void setTitle(String title) {
    }
}
