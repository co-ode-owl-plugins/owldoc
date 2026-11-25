package org.coode.html;

import org.coode.html.doclet.HTMLDoclet;
import org.coode.html.doclet.HierarchyDoclet;
import org.coode.html.doclet.OWLAnnotationPropertySummaryDoclet;
import org.coode.html.doclet.OWLClassSummaryDoclet;
import org.coode.html.doclet.OWLDataPropertySummaryDoclet;
import org.coode.html.doclet.OWLDatatypeSummaryDoclet;
import org.coode.html.doclet.OWLIndividualSummaryDoclet;
import org.coode.html.doclet.OWLObjectPropertySummaryDoclet;
import org.coode.html.doclet.OWLOntologySummaryDoclet;
import org.coode.html.impl.OWLHTMLProperty;
import org.coode.html.page.OWLDocPage;
import org.coode.owl.mngr.HierarchyProvider;
import org.coode.owl.mngr.NamedObjectType;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Aug 4, 2010<br><br>
 */
public class SummaryPageFactory {

    private OWLHTMLKit kit;
    private final org.coode.html.doclet.UsageDoclet.PaginationHandler pagination;

    public SummaryPageFactory(OWLHTMLKit kit) { this(kit, null); }

    public SummaryPageFactory(OWLHTMLKit kit, org.coode.html.doclet.UsageDoclet.PaginationHandler pagination) {
        this.kit = kit;
        this.pagination = pagination;
    }

    public <N extends OWLObject> OWLDocPage<N> getSummaryPage(Class<N> cls) {
        OWLDocPage<N> page = new OWLDocPage<>(kit);

        if (isShowMiniHierarchiesEnabled()){
            page.addDoclet(getHierarchy(cls));
        }
        page.addDoclet(getSummaryDoclet(cls));
        return page;
    }

    @SuppressWarnings("unchecked")
    public <N extends OWLObject> OWLDocPage<N> getSummaryPage(N owlObject) {
        OWLDocPage<N> page = getSummaryPage((Class<N>)owlObject.getClass());

        page.setUserObject(owlObject);

        return page;
    }

    @SuppressWarnings("unchecked")    
    public <N extends OWLObject> HTMLDoclet<N> getSummaryDoclet(N owlObject) {
        HTMLDoclet<N> doclet = getSummaryDoclet((Class<N>)owlObject.getClass());
        doclet.setUserObject(owlObject);
        return doclet;
    }


    @SuppressWarnings("unchecked")
    public <N extends OWLObject> HTMLDoclet<N> getSummaryDoclet(Class<N> cls) {
        if (OWLClass.class.isAssignableFrom(cls)){
            return (HTMLDoclet<N>)new OWLClassSummaryDoclet(kit, pagination);
        }
        else if (OWLObjectProperty.class.isAssignableFrom(cls)){
            return (HTMLDoclet<N>)new OWLObjectPropertySummaryDoclet(kit, pagination);
        }
        else if (OWLDataProperty.class.isAssignableFrom(cls)){
            return (HTMLDoclet<N>)new OWLDataPropertySummaryDoclet(kit, pagination);
        }
        else if (OWLAnnotationProperty.class.isAssignableFrom(cls)){
            return (HTMLDoclet<N>)new OWLAnnotationPropertySummaryDoclet(kit, pagination);
        }
        else if (OWLNamedIndividual.class.isAssignableFrom(cls)){
            return (HTMLDoclet<N>)new OWLIndividualSummaryDoclet(kit, pagination);
        }
        else if (OWLDatatype.class.isAssignableFrom(cls)){
            return (HTMLDoclet<N>)new OWLDatatypeSummaryDoclet(kit, pagination);
        }
        else if (OWLOntology.class.isAssignableFrom(cls)){
            return (HTMLDoclet<N>)new OWLOntologySummaryDoclet(kit);
        }

        throw new RuntimeException("Cannot find a summary for type: " + cls);
    }

    public <N extends OWLObject> HierarchyDoclet<N> getHierarchy(Class<N> cls){
        HierarchyProvider<N> hp = kit.getOWLServer().getHierarchyProvider(cls);
        String title = NamedObjectType.getType(cls).getPluralRendering();

        if (kit.getHTMLProperties().isSet(OWLHTMLProperty.optionShowInferredHierarchies)){
            title += " (Inferred)";
        }

        HierarchyDoclet<N> hierarchyDoclet = new HierarchyDoclet<>(title, kit, hp);
        hierarchyDoclet.setAutoExpandEnabled(true);
        return hierarchyDoclet;
    }

    private boolean isShowMiniHierarchiesEnabled() {
        return kit.getHTMLProperties().isSet(OWLHTMLProperty.optionShowMiniHierarchies);
    }
}
