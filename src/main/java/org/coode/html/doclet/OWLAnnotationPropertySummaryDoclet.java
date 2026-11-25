package org.coode.html.doclet;

import java.io.PrintWriter;
import java.net.URL;

import org.coode.html.OWLHTMLKit;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Aug 4, 2010<br><br>
 */
public class OWLAnnotationPropertySummaryDoclet extends AbstractOWLDocDoclet<OWLAnnotationProperty> {

    public OWLAnnotationPropertySummaryDoclet(OWLHTMLKit kit) { this(kit, null); }

    public OWLAnnotationPropertySummaryDoclet(OWLHTMLKit kit, UsageDoclet.PaginationHandler pagination) {
        super(kit);

        addDoclet(new OWLEntityTitleDoclet<OWLAnnotationProperty>(kit));
        addDoclet(new AnnotationsDoclet<OWLAnnotationProperty>(kit));
        addDoclet(new AnnotationPropertyDomainsDoclet(kit));
        addDoclet(new AnnotationPropertyRangesDoclet(kit));
        addDoclet(new AnnotationPropertySuperPropertiesDoclet(kit));
        addDoclet(new UsageDoclet<OWLAnnotationProperty>(kit, pagination));
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
        return "doclet.summary.annotationproperty";
    }
}
