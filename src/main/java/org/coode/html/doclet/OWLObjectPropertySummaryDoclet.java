package org.coode.html.doclet;

import java.io.PrintWriter;
import java.net.URL;

import org.coode.html.OWLHTMLKit;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Aug 4, 2010<br><br>
 */
public class OWLObjectPropertySummaryDoclet extends AbstractOWLDocDoclet<OWLObjectProperty> {

    public OWLObjectPropertySummaryDoclet(OWLHTMLKit kit) { this(kit, null); }

    public OWLObjectPropertySummaryDoclet(OWLHTMLKit kit, UsageDoclet.PaginationHandler pagination) {
        super(kit);

        addDoclet(new OWLEntityTitleDoclet<OWLObjectProperty>(kit));
        addDoclet(new AnnotationsDoclet<OWLObjectProperty>(kit));
        addDoclet(new PropertyCharacteristicsDoclet<OWLObjectProperty>(kit));
        addDoclet(new DomainsDoclet<OWLObjectProperty>(kit));
        addDoclet(new RangesDoclet<OWLObjectProperty, OWLClassExpression>(kit));
        addDoclet(new InversesDoclet(kit));
        addDoclet(new AssertedSuperpropertiesDoclet<OWLObjectProperty>(kit));
        addDoclet(new AssertedEquivpropertiesDoclet<OWLObjectProperty>(kit));
        addDoclet(new DisjointPropertiesDoclet<OWLObjectProperty>(kit));
        addDoclet(new UsageDoclet<OWLObjectProperty>(kit, pagination));
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
        return "doclet.summary.objectproperty";
    }
}
