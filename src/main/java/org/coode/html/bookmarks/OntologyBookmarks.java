/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.bookmarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Feb 11, 2008<br><br>
 */
public class OntologyBookmarks {

    private static final String BOOKMARK_PROP = "http://www.co-ode.org/ontologies/meta.owl#bookmark";

    private OWLAnnotationProperty property;

    private Set<OWLEntity> bookmarks = new HashSet<>();

    private OWLOntologyManager mngr;

    private OWLOntology ont;


    public OntologyBookmarks(OWLOntologyManager mngr, OWLOntology ont) {
        this.mngr = mngr;
        this.ont = ont;

        property = mngr.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(BOOKMARK_PROP));
        loadAnnotations();
    }

    public OWLOntology getOntology(){
        return ont;
    }

    public Set<OWLEntity> getBookmarks(){
        return Collections.unmodifiableSet(bookmarks);
    }

    public int getSize() {
        return bookmarks.size();
    }

    public List<OWLOntologyChange> add(OWLEntity obj) {
        bookmarks.add(obj);
        return getUpdateChanges();
    }

    public List<OWLOntologyChange> remove(OWLEntity obj) {
        bookmarks.remove(obj);
        return getUpdateChanges();
    }

    public List<OWLOntologyChange> clear() {
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLAnnotation annot : ont.getAnnotations()){
            if (annot.getProperty().equals(property)){
                changes.add(new RemoveOntologyAnnotation(ont, annot));
            }
        }
        return changes;
    }

    private List<OWLOntologyChange> getUpdateChanges() {
        List<OWLOntologyChange> changes = new ArrayList<>();
        String annotationValue = "";
        for (OWLObject bookmark : bookmarks){
            if (bookmark instanceof OWLEntity){
                annotationValue += ((OWLEntity)bookmark).getIRI() + "\n";
            }
        }
        changes.addAll(clear());

        if (annotationValue.length() > 0){
            OWLLiteral value = mngr.getOWLDataFactory().getOWLLiteral(annotationValue);
            OWLAnnotation annot = mngr.getOWLDataFactory().getOWLAnnotation(property, value);
            changes.add(new AddOntologyAnnotation(ont, annot));
        }

        return changes;
    }

    private void loadAnnotations() {
        // load the bookmark from the ontology annotations
        for (OWLAnnotation annot : ont.getAnnotations()){
            if (annot.getProperty().equals(property)){
                OWLAnnotationValue content = annot.getValue();
                if (content instanceof OWLLiteral){
                    parseAnnotation(((OWLLiteral)content).getLiteral());
                }
            }
        }
    }

    private void parseAnnotation(String s) {
        for (String value : s.split("\n")){
            IRI iri = IRI.create(value);
                OWLEntity e = getEntityFromIRI(iri);
                if (e != null){
                    bookmarks.add(e);
                }
            }
    }

    private OWLEntity getEntityFromIRI(IRI iri) {
        for (OWLOntology ontology : mngr.getOntologies()){
            if (ontology.containsClassInSignature(iri)){
                return mngr.getOWLDataFactory().getOWLClass(iri);
            }

            if (ontology.containsObjectPropertyInSignature(iri)){
                return mngr.getOWLDataFactory().getOWLObjectProperty(iri);
            }

            if (ontology.containsDataPropertyInSignature(iri)){
                return mngr.getOWLDataFactory().getOWLDataProperty(iri);
            }

            if (ontology.containsAnnotationPropertyInSignature(iri)){
                return mngr.getOWLDataFactory().getOWLAnnotationProperty(iri);
            }

            if (ontology.containsIndividualInSignature(iri)){
                return mngr.getOWLDataFactory().getOWLNamedIndividual(iri);
            }

            if (ontology.containsDatatypeInSignature(iri)){
                return mngr.getOWLDataFactory().getOWLDatatype(iri);
            }
        }

        return null;
    }
}
