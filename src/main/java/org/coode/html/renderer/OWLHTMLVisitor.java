/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.renderer;

import static org.semanticweb.owlapi.search.EntitySearcher.getDataPropertyValues;
import static org.semanticweb.owlapi.search.EntitySearcher.getNegativeDataPropertyValues;
import static org.semanticweb.owlapi.search.EntitySearcher.getNegativeObjectPropertyValues;
import static org.semanticweb.owlapi.search.EntitySearcher.getObjectPropertyValues;
import static org.semanticweb.owlapi.search.EntitySearcher.getTypes;

import org.coode.html.OWLHTMLKit;
import org.coode.html.impl.OWLHTMLConstants;
import org.coode.html.url.OWLObjectURLRenderer;
import org.coode.html.util.URLUtils;
import org.coode.owl.mngr.NamedObjectType;
import org.coode.owl.util.OWLUtils;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLUnaryPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLFacet;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.Optional;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Feb 12, 2008<br><br>
 */
public class OWLHTMLVisitor implements OWLObjectVisitor {

    // These should match the css class names
    private static final String CSS_DEPRECATED = "deprecated";
    private static final String CSS_ACTIVE_ENTITY = "active-entity";
    private static final String CSS_KEYWORD = "keyword";
    private static final String CSS_ONTOLOGY_URI = "ontology-uri";
    private static final String CSS_ACTIVE_ONTOLOGY_URI = "active-ontology-uri";
    private static final String CSS_SOME = "some";
    private static final String CSS_ONLY = "only";
    private static final String CSS_VALUE = "value";
    private static final String CSS_LITERAL = "literal";

    // the subset and equivalence symbols can be encoded in HTML
    private static final boolean USE_SYMBOLS = true;

    private PrintWriter out;

    private URL pageURL = null;

    private OWLObjectURLRenderer urlRenderer;

    private ShortFormProvider sfProvider;

    private OntologyIRIShortFormProvider ontologyIriSFProvider;

    private Set<OWLOntology> ontologies = new HashSet<>();

    private OWLOntology activeOntology = null;

    private int indent = 0;

    private boolean writeStats = false;

    private OWLHTMLKit kit;

    public OWLHTMLVisitor(OWLHTMLKit kit, PrintWriter out) {
        this.kit = kit;
        this.urlRenderer = kit.getURLScheme();
        this.sfProvider = kit.getOWLServer().getShortFormProvider();
        this.ontologyIriSFProvider = kit.getOWLServer().getOntologyShortFormProvider();
        this.out = out;
    }

    /**
     * Sets the current page URL. All links will be rendered relative to this page
     * @param pageURL
     */
    public void setPageURL(URL pageURL){
        this.pageURL = pageURL;
    }

    public void setOntologies(Set<OWLOntology> ontologies){
        this.ontologies = ontologies;
    }

    public void setActiveOntology(OWLOntology activeOnt){
        this.activeOntology = activeOnt;
    }

    private void write(String s) {
        out.write(s);
    }


    ////////// Ontology

    @Override
    public void visit(OWLOntology ontology) {
        final URL urlForOntology = urlRenderer.getURLForOWLObject(ontology);
        String link = urlForOntology.toString();
        String cssClass = CSS_ONTOLOGY_URI;
        if (activeOntology != null && ontology.equals(activeOntology)){
            cssClass = CSS_ACTIVE_ONTOLOGY_URI;
        }

        boolean writeLink = false;

        if (pageURL == null){
            writeLink = true;
        }
        else{
            if (!pageURL.equals(urlForOntology)){
                link = URLUtils.createRelativeURL(pageURL, urlForOntology);
                writeLink = true;
            }
            else{
                write("<span class='" + CSS_ACTIVE_ENTITY + " " + cssClass + "'>");
                write(ontologyIriSFProvider.getShortForm(ontology));
                write("</span>");
            }
        }

        if (writeLink){
            write("<a class='" + cssClass + "'");
            String id = OWLUtils.getOntologyIdString(ontology);
            write(" href=\"" + link + "\" title='" + id + "'");
            writeContentTarget();
            write(">");
            write(ontologyIriSFProvider.getShortForm(ontology));
            write("</a>");
        }

        if (writeStats){
            write(" <span style='color:gray;'>(" +
                  "c:" + ontology.getClassesInSignature().size() +
                  ", op:" + ontology.getObjectPropertiesInSignature().size() +
                  ", dp:" + ontology.getDataPropertiesInSignature().size() +
                  ", i:" + ontology.getIndividualsInSignature().size() +
                  ")</span>");
        }
    }


    ////////// Entities

    @Override
    public void visit(OWLClass desc) {
        writeOWLEntity(desc, NamedObjectType.classes.getSingularRendering());
    }

    @Override
    public void visit(OWLDataProperty property) {
        writeOWLEntity(property, NamedObjectType.dataproperties.getSingularRendering());
    }

    @Override
    public void visit(OWLObjectProperty property) {
        writeOWLEntity(property, NamedObjectType.objectproperties.getSingularRendering());
    }

    @Override
    public void visit(OWLAnnotationProperty property) {
        writeOWLEntity(property, NamedObjectType.annotationproperties.getSingularRendering());
    }

    @Override
    public void visit(OWLNamedIndividual individual) {
        writeOWLEntity(individual, NamedObjectType.individuals.getSingularRendering());
    }

    @Override
    public void visit(OWLDatatype datatype) {
        writeOWLEntity(datatype, NamedObjectType.datatypes.getSingularRendering());
    }

    @Override
    public void visit(IRI iri) {
        writeIRIWithBoldFragment(iri, iri.getRemainder());
        try {
            URL url = iri.toURI().toURL();
            URLUtils.renderURLLinks(url, kit, pageURL, out);
        }
        catch (MalformedURLException e) {
            // An IRI such as a URN need not have a navigable URL protocol.
            // Its text has already been rendered above.
        }
    }

    @Override
    public void visit(OWLAnonymousIndividual individual) {
        writeAnonymousIndividual(individual);
    }

    ///////// Anonymous classes

    @Override
    public void visit(OWLObjectSomeValuesFrom desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.SOME.toString(), CSS_SOME);
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    @Override
    public void visit(OWLObjectAllValuesFrom desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.ONLY.toString(), CSS_ONLY);
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    @Override
    public void visit(OWLObjectHasValue desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.VALUE.toString(), CSS_VALUE);
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    @Override
    public void visit(OWLObjectMinCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.MIN.toString());
    }

    @Override
    public void visit(OWLObjectExactCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.EXACTLY.toString());
    }

    @Override
    public void visit(OWLObjectMaxCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.MAX.toString());
    }

    @Override
    public void visit(OWLObjectComplementOf desc) {
        writeKeyword(ManchesterOWLSyntax.NOT.toString());
        write(" ");
        writeOp(desc.getOperand(), false);
    }

    @Override
    public void visit(OWLObjectHasSelf desc) {
        writeKeyword(ManchesterOWLSyntax.SELF.toString());
    }

    @Override
    public void visit(OWLObjectIntersectionOf desc) {
        writeKeywordOpList(orderOps(desc.getOperands()), ManchesterOWLSyntax.AND.toString(), true);
    }

    @Override
    public void visit(OWLObjectUnionOf desc) {
        writeKeywordOpList(orderOps(desc.getOperands()), ManchesterOWLSyntax.OR.toString(), false);
    }

    @Override
    public void visit(OWLObjectOneOf desc) {
        write("{");
        writeOpList(desc.getIndividuals(), ", ", false);
        write("}");
    }

    @Override
    public void visit(OWLDataOneOf desc) {
        write("{");
        writeOpList(desc.getValues(), ", ", false);
        write("}");
    }


    @Override
    public void visit(OWLDataSomeValuesFrom desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.SOME.toString(), CSS_SOME);
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    @Override
    public void visit(OWLDataAllValuesFrom desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.ONLY.toString(), CSS_ONLY);
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    @Override
    public void visit(OWLDataHasValue desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.VALUE.toString(), CSS_VALUE);
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    @Override
    public void visit(OWLDataMinCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.MIN.toString());
    }

    @Override
    public void visit(OWLDataExactCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.EXACTLY.toString());
    }

    @Override
    public void visit(OWLDataMaxCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.MAX.toString());
    }

    @Override
    public void visit(OWLDatatypeRestriction node) {
        node.getDatatype().accept(this);
        write(" [");
        writeOpList(node.getFacetRestrictions(), ", ", false);
        write("]");
    }


    @Override
    public void visit(OWLFacetRestriction node) {
        writeKeyword(writeFacet(node.getFacet()));
        node.getFacetValue().accept(this);
    }

    @Override
    public void visit(OWLDataComplementOf node) {
        writeKeyword(ManchesterOWLSyntax.NOT.toString());
        write(" ");
        writeOp(node.getDataRange(), true);
    }


    @Override
    public void visit(OWLDataIntersectionOf owlDataIntersectionOf) {
        writeKeywordOpList(owlDataIntersectionOf.getOperands(), ManchesterOWLSyntax.AND.toString(), true);
    }


    @Override
    public void visit(OWLDataUnionOf owlDataUnionOf) {
        writeKeywordOpList(owlDataUnionOf.getOperands(), ManchesterOWLSyntax.OR.toString(), false);
    }

    ////////// Properties

    @Override
    public void visit(OWLObjectInverseOf property) {
        writeKeyword(ManchesterOWLSyntax.INVERSE_OF.toString());
        write(" ");
        writeOp(property.getInverse(), true);
    }

    @Override
    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.FUNCTIONAL.toString());
    }

    @Override
    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.INVERSE_FUNCTIONAL.toString());
    }

    @Override
    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.SYMMETRIC.toString());
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.TRANSITIVE.toString());
    }

    @Override
    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.ASYMMETRIC.toString());
    }

    @Override
    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.REFLEXIVE.toString());
    }

    @Override
    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.IRREFLEXIVE.toString());
    }

    @Override
    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.DOMAIN.toString());
        write(" ");
        writeOp(axiom.getDomain(), true);
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.RANGE.toString());
        write(" ");
        writeOp(axiom.getRange(), true);
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        writeOp(axiom.getFirstProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.INVERSE.toString());
        write(" ");
        writeOp(axiom.getSecondProperty(), true);
        writeAnnotations(axiom);
    }


    @Override
    public void visit(OWLHasKeyAxiom axiom) {
        writeOp(axiom.getClassExpression(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.HAS_KEY.toString());
        write(" ");
        write("(");
        writeOpList(axiom.getPropertyExpressions(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }


    @Override
    public void visit(OWLDatatypeDefinitionAxiom axiom) {
        axiom.getDatatype().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.EQUIVALENT_TO.toString());
        write(" ");
        axiom.getDataRange().accept(this);
        writeAnnotations(axiom);
    }


    @Override
    public void visit(SWRLRule swrlRule) {
        // @@TODO SWRL SUpport
    }


    @Override
    public void visit(SWRLClassAtom swrlClassAtom) {
        // @@TODO SWRL SUpport
    }


    @Override
    public void visit(SWRLDataRangeAtom swrlDataRangeAtom) {
        // @@TODO SWRL SUpport
    }


    @Override
    public void visit(SWRLObjectPropertyAtom swrlObjectPropertyAtom) {
        // @@TODO SWRL SUpport
    }


    @Override
    public void visit(SWRLDataPropertyAtom swrlDataPropertyAtom) {
        // @@TODO SWRL SUpport
    }


    @Override
    public void visit(SWRLBuiltInAtom swrlBuiltInAtom) {
        // @@TODO SWRL SUpport
    }


    @Override
    public void visit(SWRLVariable swrlVariable) {
        // @@TODO SWRL Support
    }


    @Override
    public void visit(SWRLIndividualArgument swrlIndividualArgument) {
        // @@TODO SWRL SUpport
    }


    @Override
    public void visit(SWRLLiteralArgument swrlLiteralArgument) {
        // @@TODO SWRL SUpport
    }


    @Override
    public void visit(SWRLSameIndividualAtom swrlSameIndividualAtom) {
        // @@TODO SWRL SUpport
    }


    @Override
    public void visit(SWRLDifferentIndividualsAtom swrlDifferentIndividualsAtom) {
        // @@TODO SWRL SUpport
    }


    @Override
    public void visit(OWLSubPropertyChainOfAxiom axiom) {
        writeKeywordOpList(axiom.getPropertyChain(), "o", false);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.SUB_PROPERTY_OF.toString());
        write(" ");
        writeOp(axiom.getSuperProperty(), true);
        writeAnnotations(axiom);
    }


    @Override
    public void visit(OWLDataPropertyDomainAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.DOMAIN.toString());
        write(" ");
        writeOp(axiom.getDomain(), true);
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLDataPropertyRangeAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.RANGE.toString());
        write(" ");
        writeOp(axiom.getRange(), true);
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.FUNCTIONAL.toString());
        writeAnnotations(axiom);
    }

    ////////// Annotations

    @Override
    public void visit(OWLAnnotationAssertionAxiom axiom) {
        final OWLAnnotationSubject subject = axiom.getSubject();
        // extract the entities with this IRI
        if (subject instanceof IRI){
            Set<OWLEntity> entities = new HashSet<>();
            // Avoid getEntitiesInSignature(IRI), which scans all entity types,
            // and containsEntityInSignature(entity), whose miss path scans annotations.
            IRI iri = (IRI) subject;
            org.semanticweb.owlapi.model.OWLDataFactory df = kit.getOWLServer().getOWLOntologyManager().getOWLDataFactory();
            for (OWLOntology ont : ontologies) {
                if (ont.containsClassInSignature(iri)) entities.add(df.getOWLClass(iri));
                if (ont.containsObjectPropertyInSignature(iri)) entities.add(df.getOWLObjectProperty(iri));
                if (ont.containsDataPropertyInSignature(iri)) entities.add(df.getOWLDataProperty(iri));
                if (ont.containsIndividualInSignature(iri)) entities.add(df.getOWLNamedIndividual(iri));
                // These two small signatures also include entities used only in ontology annotations.
                if (ont.containsAnnotationPropertyInSignature(iri)
                        || ont.annotationPropertiesInSignature().anyMatch(property -> property.getIRI().equals(iri))) {
                    entities.add(df.getOWLAnnotationProperty(iri));
                }
                if (ont.containsDatatypeInSignature(iri)
                        || ont.datatypesInSignature().anyMatch(datatype -> datatype.getIRI().equals(iri))) {
                    entities.add(df.getOWLDatatype(iri));
                }
            }
            if (!entities.isEmpty()){
                boolean started = false;
                for (OWLEntity entity : entities){
                    if (started){
                        write("&nbsp;");
                    }
                    entity.accept(this);
                    started = true;
                }
            }
            else{
                subject.accept(this);
            }
        }
        else{
            subject.accept(this);
        }
        write("&nbsp;");
        axiom.getAnnotation().accept(this);
        write("&nbsp;");
        writeAnnotations(axiom); // in theory, you could annotate the annotation axioms !!
    }


    @Override
    public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        writeOp(axiom.getSubProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.SUB_PROPERTY_OF.toString());
        write(" ");
        writeOp(axiom.getSuperProperty(), true);
        writeAnnotations(axiom);
    }


    @Override
    public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.RANGE.toString());
        write(" ");
        writeOp(axiom.getDomain(), true);
        writeAnnotations(axiom);
    }


    @Override
    public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.RANGE.toString());
        write(" ");
        writeOp(axiom.getRange(), true);
        writeAnnotations(axiom);
    }


    @Override
    public void visit(OWLAnnotation annotation) {
        annotation.getProperty().accept(this);
        write(" ");
        annotation.getValue().accept(this);
    }

// OWLAPI v3.1
    @Override
    public void visit(OWLLiteral node) {
        write("<span class='" + CSS_LITERAL + "'>");
        final OWLDatatype dt = node.getDatatype();
        if (dt.isInteger() || dt.isFloat()){
            writeLiteralContents(node.getLiteral());
            write("</span>");
        }
        else{
            write("\"");
            writeLiteralContents(node.getLiteral());
            write("\"");
            write("</span>");
            if (node.hasLang()){
                if (node.hasLang()){
                    final String lang = node.getLang();
                    if (!lang.isEmpty()){
                        write(" <span style='color: black;'>@" + lang + "</span>");
                    }
                }
            }
            else{
                write("(");
                dt.accept(this);
                write(")");
            }
        }
    }

    /////////// Axioms

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom) {
        writeEquivalence(orderOps(axiom.getClassExpressions()), axiom);
    }

    @Override
    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        writeEquivalence(axiom.getProperties(), axiom);
    }

    @Override
    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        writeEquivalence(axiom.getProperties(), axiom);
    }

    @Override
    public void visit(OWLSameIndividualAxiom axiom) {
        writeKeywordOpList(axiom.getIndividuals(), ManchesterOWLSyntax.SAME_AS.toString(), false);
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom) {
        axiom.getSubClass().accept(this);
        write(" ");
        writeKeyword(USE_SYMBOLS ? OWLHTMLConstants.SUBCLASS_CHAR : ManchesterOWLSyntax.SUBCLASS_OF.toString());
        write(" ");
        axiom.getSuperClass().accept(this);
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        axiom.getSubProperty().accept(this);
        write(" ");
        writeKeyword(USE_SYMBOLS ? OWLHTMLConstants.SUBCLASS_CHAR : ManchesterOWLSyntax.SUB_PROPERTY_OF.toString());
        write(" ");
        axiom.getSuperProperty().accept(this);
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLSubDataPropertyOfAxiom axiom) {
        axiom.getSubProperty().accept(this);
        write(" ");
        writeKeyword(USE_SYMBOLS ? OWLHTMLConstants.SUBCLASS_CHAR : ManchesterOWLSyntax.SUB_PROPERTY_OF.toString());
        write(" ");
        axiom.getSuperProperty().accept(this);
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLDisjointClassesAxiom axiom) {
        writeKeyword(ManchesterOWLSyntax.DISJOINT_CLASSES.toString());
        write("(");
        writeOpList(axiom.getClassExpressions(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        writeKeyword(ManchesterOWLSyntax.DISJOINT_PROPERTIES.toString());
        write("(");
        writeOpList(axiom.getProperties(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        writeKeyword(ManchesterOWLSyntax.DISJOINT_PROPERTIES.toString());
        write("(");
        writeOpList(axiom.getProperties(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLDifferentIndividualsAxiom axiom) {
        writeKeyword(ManchesterOWLSyntax.DIFFERENT_INDIVIDUALS.toString());
        write("(");
        writeOpList(axiom.getIndividuals(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLDisjointUnionAxiom axiom) {
        writeKeyword(ManchesterOWLSyntax.DISJOINT_UNION_OF.toString());
        write("(");
        writeOpList(axiom.getClassExpressions(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLDeclarationAxiom axiom) {
        final OWLEntity entity = axiom.getEntity();
        if (entity instanceof OWLClass){
            writeKeyword(ManchesterOWLSyntax.CLASS.toString());
            write(": ");
        }
        else if (entity instanceof OWLObjectProperty){
            writeKeyword(ManchesterOWLSyntax.OBJECT_PROPERTY.toString());
            write(": ");
        }
        else if (entity instanceof OWLDataProperty){
            writeKeyword(ManchesterOWLSyntax.DATA_PROPERTY.toString());
            write(": ");
        }
        else if (entity instanceof OWLAnnotationProperty){
            writeKeyword(ManchesterOWLSyntax.ANNOTATION_PROPERTY.toString());
            write(": ");
        }
        else if (entity instanceof OWLNamedIndividual){
            writeKeyword(ManchesterOWLSyntax.INDIVIDUAL.toString());
            write(": ");
        }
        else if (entity instanceof OWLDatatype){
            writeKeyword("Datatype");
            write(": ");
        }
        entity.accept(this);
        writeAnnotations(axiom);
    }

    /////// OWLIndividual assertions

    @Override
    public void visit(OWLClassAssertionAxiom axiom) {
        axiom.getIndividual().accept(this);
        write(": ");
        axiom.getClassExpression().accept(this);
        writeAnnotations(axiom);
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        writeAssertionAxiom(axiom);
    }

    @Override
    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        writeAssertionAxiom(axiom);
    }

    @Override
    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        writeAssertionAxiom(axiom);
    }

    @Override
    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        writeAssertionAxiom(axiom);
    }

    private void writeEquivalence(Collection<? extends OWLObject> objects, OWLAxiom axiom) {
        String equiv = USE_SYMBOLS ? OWLHTMLConstants.EQUIV_CHAR : ManchesterOWLSyntax.EQUIVALENT_TO.toString();
        writeKeywordOpList(objects, equiv, false);
        writeAnnotations(axiom);
    }

    private String getName(OWLEntity entity){
        return sfProvider.getShortForm(entity).replaceAll(" ", "&nbsp;");
    }

    // just make sure a named class is first if there is one
    private static List<OWLClassExpression> orderOps(Set<OWLClassExpression> ops) {
        List<OWLClassExpression> orderedOps = new ArrayList<>(ops);
        Collections.sort(orderedOps, new Comparator<OWLClassExpression>(){
            @Override
            public int compare(OWLClassExpression d1, OWLClassExpression d2) {
                if (d1 instanceof OWLClass){
                    return -1;
                }
                else if (d2 instanceof OWLClass){
                    return 1;
                }
                return 0;
            }
        });
        return orderedOps;
    }

//    private void writeOntologyIRI(OWLOntology ont) {
//        writeIRIWithBoldFragment(ont.getOntologyID().getOntologyIRI(), ontologyIriSFProvider.getShortForm(ont));
//    }

    private void writeIRIWithBoldFragment(IRI iri, Optional<String> shortForm) {
        final String fullURI = iri.toString();
        if (shortForm.isPresent()) {
            String shortFormString = shortForm.get();
            int index = fullURI.lastIndexOf(shortFormString);
            write(fullURI.substring(0, index));
            write("<b>");
            write(shortForm.get());
            write("</b>");
            write(fullURI.substring(index+shortFormString.length()));
        }
        else {
            write(fullURI);
        }
    }

    // add a span to allow for css highlighting
    private void writeKeyword(String keyword) {
        String cssClass = CSS_KEYWORD;
        if (keyword.equalsIgnoreCase("and") || keyword.equalsIgnoreCase("or") || keyword.equalsIgnoreCase("not")) {
            cssClass += " logical-keyword";
        } else if (keyword.equalsIgnoreCase("self")) {
            cssClass += " restriction-keyword";
        }
        writeKeyword(keyword, cssClass);
    }

    // add a span to allow for css highlighting
    private void writeKeyword(String keyword, String cssClass) {
        boolean mathSymbol = OWLHTMLConstants.SUBCLASS_CHAR.equals(keyword) || OWLHTMLConstants.EQUIV_CHAR.equals(keyword);
        write("<span class='" + cssClass + (mathSymbol ? " math-symbol" : "") + "'>" + keyword + "</span>");
    }


    // useful to add brackets around the anonymous operators of unions and intersections and the fillers of restrictions
    private void writeOp(OWLObject op, boolean wrap) {
        if (op instanceof OWLEntity ||
            op instanceof OWLObjectOneOf ||
            op instanceof OWLDataOneOf ||
            op instanceof OWLDatatypeRestriction ||
            op instanceof OWLLiteral){
            op.accept(this);
        }
        else{ // provide brackets for clarity
            write("(");
            if (wrap && op instanceof OWLObjectIntersectionOf){
                indent++;
                write("<br>");
                writeIndent();
            }
            op.accept(this);
            if (wrap && op instanceof OWLObjectIntersectionOf){
                indent--;
            }
            write(")");
        }
    }

    private void writeIndent() {
        for (int i=0; i<indent; i++){
            write("&nbsp;&nbsp;&nbsp;&nbsp;");
        }
    }


    private void writeOWLEntity(OWLEntity entity, String cssClass) {
        final URI uri = entity.getIRI().toURI();

        String name = getName(entity);

        Set<String> cssClasses = new HashSet<>();
        cssClasses.add(cssClass);
        if (OWLUtils.isDeprecated(entity, ontologies)){
            cssClasses.add(CSS_DEPRECATED);
        }

        if (pageURL == null){
            final URL urlForTarget = urlRenderer.getURLForOWLObject(entity);
            write("<a href=\"" + urlForTarget + "\"");
            writeContentTarget();
            writeCSSClasses(cssClasses);
            write(" title=\"" + uri + "\">" + name + "</a>");
        }
        else{
            OWLObject currentTarget = urlRenderer.getOWLObjectForURL(pageURL);
            if (currentTarget != null && currentTarget.equals(entity)){
                cssClasses.add(CSS_ACTIVE_ENTITY);
                write("<span");
                writeCSSClasses(cssClasses);
                write(">" + name + "</span>");
            }
            else{
                final URL urlForTarget = urlRenderer.getURLForOWLObject(entity);
                write("<a href=\"" + URLUtils.createRelativeURL(pageURL, urlForTarget) + "\"");
                writeContentTarget();

                writeCSSClasses(cssClasses);
                write(" title=\"" + uri + "\">");
                write(name);
                write("</a>");
            }
        }
    }

    private void writeContentTarget() {
        String target = kit.getHTMLProperties().get(org.coode.html.impl.OWLHTMLProperty.optionContentWindow);
        if (target != null && !target.isEmpty()) {
            write(" target=\"" + target.replace("&", "&amp;").replace("\"", "&quot;") + "\"");
        }
    }

    private void writeAnonymousIndividual(OWLAnonymousIndividual individual) {
        write("<span class=\"anon\">");
        Collection<OWLClassExpression> types = getTypes(individual, ontologies.stream()).collect(java.util.stream.Collectors.toSet());
        if (!types.isEmpty()){
            writeOpList(types, ", ", false);
        }

        // TODO tidy this up - we shouldn't really group by ontology
        for (OWLOntology ont : ontologies){
            Map<OWLDataPropertyExpression, Collection<OWLLiteral>> dataValues = getDataPropertyValues(
                    individual, ont).asMap();
            if (!dataValues.isEmpty()){
                write("<ul>");
                for (OWLDataPropertyExpression p : dataValues.keySet()){
                    write("<li>");
                    p.accept(this);
                    write("<ul><li>");
                    writeOpList(dataValues.get(p), "</li><li>", false);
                    write("</ul></li>");
                }
                write("</ul>");
            }
            Map<OWLDataPropertyExpression, Collection<OWLLiteral>> negDataValues = getNegativeDataPropertyValues(
                    individual, ont).asMap();
            if (!negDataValues.isEmpty()){
                write("<ul>");

                for (OWLDataPropertyExpression p : negDataValues.keySet()){
                    write("<li>not ");
                    p.accept(this);
                    write("<ul><li>");
                    writeOpList(negDataValues.get(p), "</li><li>", false);
                    write("</ul></li>");
                }
                write("</ul>");
            }

            Map<OWLObjectPropertyExpression, Collection<OWLIndividual>> objValues = getObjectPropertyValues(
                    individual, ont).asMap();
            if (!objValues.isEmpty()){
                write("<ul>");

                for (OWLObjectPropertyExpression p : objValues.keySet()){
                    write("<li>");
                    p.accept(this);
                    write("<ul><li>");
                    writeOpList(objValues.get(p), "</li><li>", false);
                    write("</ul></li>");
                }
                write("</ul>");

            }
            Map<OWLObjectPropertyExpression, Collection<OWLIndividual>> negbjValues = getNegativeObjectPropertyValues(
                    individual, ont).asMap();
            if (!negbjValues.isEmpty()){
                write("<ul>");

                for (OWLObjectPropertyExpression p : negbjValues.keySet()){
                    write("<li>not ");
                    p.accept(this);
                    write("<ul><li>");
                    writeOpList(negbjValues.get(p), "</li><li>", false);
                    write("</ul></li>");
                }
                write("</ul>");
            }
        }
        write("</span>");
    }


    private void writeCardinality(OWLCardinalityRestriction<?> desc, String cardinalityType) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(cardinalityType, cardinalityType);
        write(" ");
        write(Integer.toString(desc.getCardinality()));
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    private void writeUnaryPropertyAxiom(OWLUnaryPropertyAxiom<?> axiom, String keyword) {
        writeKeyword(keyword);
        write(" (");
        writeOp(axiom.getProperty(), true);
        write(")");
        writeAnnotations(axiom);
    }

    private static String writeFacet(OWLFacet facet) {
        // need to make ranges HTML safe
        if (facet.equals(OWLFacet.MIN_INCLUSIVE)) {
            return "&gt;=";
        } else if (facet.equals(OWLFacet.MIN_EXCLUSIVE)) {
            return "&gt;";
        } else if (facet.equals(OWLFacet.MAX_INCLUSIVE)) {
            return "&lt;=";
        } else if (facet.equals(OWLFacet.MAX_EXCLUSIVE)) {
            return "&lt;";
        }
        return facet.getSymbolicForm();
    }

    private void writeLiteralContents(String literal) {
        boolean writtenExternalRef = false;
        try {
            URI uri = new URI(literal);
            if (uri.isAbsolute()){
                write("<a href='" + uri + "' target='ext_ref'>" + uri + "</a>");
                writtenExternalRef = true;
            }
        }
        catch (URISyntaxException e) {
            // do nothing
        }
        finally{
            if (!writtenExternalRef){
                literal = literal.replace("&", "&amp;");
                literal = literal.replace("<", "&lt;");
                literal = literal.replace(">", "&gt;");
                literal = literal.replace("\n", "<br />");
                write(literal);
            }
        }
    }


    private void writeAnnotations(OWLAxiom axiom) {
        final Set<OWLAnnotation> annotations = axiom.getAnnotations();
        if (!annotations.isEmpty()){
            write("<ul>");
            for (OWLAnnotation annot : annotations){
                write("<li>");
                annot.accept(this);
                write("</li>");
            }
            write("</ul>");
        }
    }

    private void writeAssertionAxiom(OWLPropertyAssertionAxiom<?,?> axiom) {
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
        writeAnnotations(axiom);
    }


    private <O extends OWLObject> void writeOpList(Iterable<O> args, String separator, boolean wrap) {
        for (Iterator<O> i = args.iterator(); i.hasNext();) {
            i.next().accept(this);
            if (i.hasNext()){
                write(separator);
                if (wrap && indent > 0){
                    write("<br>"); // cannot use <br /> in java browser
                    writeIndent();
                }
            }
        }
    }

    private <O extends OWLObject> void writeKeywordOpList(Iterable<O> args, String keyword, boolean wrap) {
        for (Iterator<O> i = args.iterator(); i.hasNext();) {
            i.next().accept(this);
            if (i.hasNext()){
                write(" ");
                writeKeyword(keyword);
                write(" ");
                if (wrap && indent > 0){
                    write("<br>"); // cannot use <br /> in java browser
                    writeIndent();
                }
            }
        }
    }
    private void writeCSSClasses(Set<String> cssClasses) {
        if (!cssClasses.isEmpty()){
            boolean started = false;
            write(" class='");
            for (String cls : cssClasses){
                if (started){
                    write(" ");
                }
                write(cls);
                started = true;
            }
            write("'");
        }
    }
}
