package org.coode.html.doclet;

import java.io.PrintWriter;
import java.net.URL;

import org.coode.html.OWLHTMLKit;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Aug 4, 2010<br><br>
 */
public class OWLClassSummaryDoclet extends AbstractOWLDocDoclet<OWLClass> {

    public OWLClassSummaryDoclet(OWLHTMLKit kit) { this(kit, null); }

    public OWLClassSummaryDoclet(OWLHTMLKit kit, UsageDoclet.PaginationHandler pagination) {
        super(kit);
        
        addDoclet(new OWLEntityTitleDoclet<OWLClass>(kit));
        addDoclet(new AnnotationsDoclet<OWLClass>(kit));
        addDoclet(new AssertedEquivalentsDoclet(kit));
        addDoclet(new SuperclassesDoclet(kit));
        addDoclet(new DisjointsDoclet(kit));
        addDoclet(new MembersDoclet(kit));
        addDoclet(new UsageDoclet<OWLClass>(kit, pagination));
    }

    @Override
    protected void renderHeader(URL pageURL, PrintWriter out) {
        out.write("<div class='summary'>");
    }

    @Override
    protected void renderFooter(URL pageURL, PrintWriter out) {
        out.write("</div> <!-- summary -->");
    }

    @Override
    public String getID() {
        return "doclet.summary.class";
    }
}
