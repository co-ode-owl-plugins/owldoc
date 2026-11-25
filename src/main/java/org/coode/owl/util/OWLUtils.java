package org.coode.owl.util;

import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationAssertionAxioms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.coode.owl.mngr.NamedObjectType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Author: Nick Drummond<br>
 * nick.drummond@cs.manchester.ac.uk<br>
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 29, 2007<br><br>
 * <p/>
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)<br>
 * copyright 2006, The University of Manchester<br>
 */
public class OWLUtils {

    public static Set<OWLOntology> getImportRoots(Set<OWLOntology> onts){
        // TODO: handle cyclic imports
        Set<OWLOntology> roots = new HashSet<>(onts);
        for (OWLOntology ont : onts){
            roots.removeAll(ont.getImports());
        }
        return roots;
    }

    public static String getOntologyIdString(OWLOntology ont){
        if (ont.isAnonymous()){
            return ont.getOWLOntologyManager().getOntologyDocumentIRI(ont).toString();
        }
        else{
            return ont.getOntologyID().getDefaultDocumentIRI().get().toString();
        }
    }

    public static boolean isDeprecated(OWLEntity entity, Set<OWLOntology> ontologies) {
        return false; // XXX there is currently no implementation of deprecated in the OWL API
    }


    public static Set<? extends OWLEntity> getOWLEntitiesFromOntology(NamedObjectType type, OWLOntology ont) {
        switch(type){
            case classes:
                Set<OWLClass> clses = new HashSet<>(ont.getClassesInSignature());
                clses.add(ont.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
                clses.add(ont.getOWLOntologyManager().getOWLDataFactory().getOWLNothing());
                return clses;
            case objectproperties:
                Set<OWLObjectProperty> ops = new HashSet<>(ont.getObjectPropertiesInSignature());
                ops.add(ont.getOWLOntologyManager().getOWLDataFactory().getOWLTopObjectProperty());
                ops.add(ont.getOWLOntologyManager().getOWLDataFactory().getOWLBottomObjectProperty());
                return ops;
            case dataproperties:
                Set<OWLDataProperty> dps = new HashSet<>(ont.getDataPropertiesInSignature());
                dps.add(ont.getOWLOntologyManager().getOWLDataFactory().getOWLTopDataProperty());
                dps.add(ont.getOWLOntologyManager().getOWLDataFactory().getOWLBottomDataProperty());
                return dps;
            case annotationproperties: return ont.getAnnotationPropertiesInSignature();
            case individuals: return ont.getIndividualsInSignature();
            case datatypes:
                Set<OWLDatatype> dts = new HashSet<>(ont.getDatatypesInSignature());
                dts.add(ont.getOWLOntologyManager().getOWLDataFactory().getTopDatatype());
                return dts;
            case entities: return ont.getSignature();
            default: throw new RuntimeException("Object type not known: " + type);
        }
    }

    public static boolean entitiesExist(NamedObjectType type, Set<OWLOntology> onts) {
        for (OWLOntology ont : onts){
            switch (type){
                case classes: if (!ont.getClassesInSignature().isEmpty()){
                    return true;
                }
                    break;
                case objectproperties: if (!ont.getObjectPropertiesInSignature().isEmpty()){
                    return true;
                }
                    break;
                case dataproperties: if (!ont.getDataPropertiesInSignature().isEmpty()){
                    return true;
                }
                    break;
                case individuals: if (!ont.getIndividualsInSignature().isEmpty()){
                    return true;
                }
                    break;
                case annotationproperties: if (!ont.getAnnotationPropertiesInSignature().isEmpty()){
                    return true;
                }
                    break;
                case datatypes: if (!ont.getDatatypesInSignature().isEmpty()){
                    return true;
                }
                    break;
                case entities: return true;
                case ontologies: return true;
            }
            // TODO: no more efficient way to ask this?
        }
        return false;
    }

    public static Map<OWLAnnotationProperty, Set<OWLAnnotationValue>> getAnnotationPropertyMap(OWLNamedIndividual individual, Set<OWLOntology> onts) {
        Map<OWLAnnotationProperty, Set<OWLAnnotationValue>> props = new HashMap<>();
        for (OWLOntology ont : onts){
            for (OWLAnnotationAssertionAxiom ax : getAnnotationAssertionAxioms(
                    individual, ont).collect(Collectors.toList())) {
                OWLAnnotationProperty p = ax.getProperty();
                Set<OWLAnnotationValue> objects = props.get(p);
                if (objects == null){
                    objects = new HashSet<>();
                    props.put(p, objects);
                }
                objects.add(ax.getAnnotation().getValue());
            }
        }
        return props;
    }

    public static Map<OWLPropertyExpression, Set<OWLPropertyAssertionObject>> getAssertedPropertyMap(OWLNamedIndividual individual, Set<OWLOntology> onts) {
        Map<OWLPropertyExpression, Set<OWLPropertyAssertionObject>> props = new HashMap<>();
        for (OWLOntology ont : onts){
            for (OWLAxiom ax : ont.getReferencingAxioms(individual)) {
                if (ax instanceof OWLPropertyAssertionAxiom){
                    OWLPropertyAssertionAxiom propAssertion = (OWLPropertyAssertionAxiom)ax;
                    if (propAssertion.getSubject().equals(individual)){
                        OWLPropertyExpression p = propAssertion.getProperty();
                        Set<OWLPropertyAssertionObject> objects = props.get(p);
                        if (objects == null){
                            objects = new HashSet<>();
                            props.put(p, objects);
                        }
                        objects.add(propAssertion.getObject());
                    }
                }
            }
        }
        return props;
    }
//
//    public static Map<OWLPropertyExpression, Set<OWLPropertyAssertionObject>> getInferredPropertyMap(OWLNamedIndividual individual, OWLReasoner r) {
//        Map<OWLPropertyExpression, Set<OWLPropertyAssertionObject>> props = new HashMap<OWLPropertyExpression, Set<OWLPropertyAssertionObject>>();
//
//        for (OWLIndividual same : r.getSameIndividuals(individual)){
//            getAssertedPropertyMap()
//        }
//
//        for (OWLOntology ont : onts){
//            for (OWLAxiom ax : individual.getReferencingAxioms(ont)){
//                if (ax instanceof OWLPropertyAssertionAxiom){
//                    OWLPropertyAssertionAxiom propAssertion = (OWLPropertyAssertionAxiom)ax;
//                    if (propAssertion.getSubject().equals(individual)){
//                        OWLPropertyExpression p = propAssertion.getProperty();
//                        Set<OWLPropertyAssertionObject> objects = props.get(p);
//                        if (objects == null){
//                            objects = new HashSet<OWLPropertyAssertionObject>();
//                            props.put(p, objects);
//                        }
//                        objects.add(propAssertion.getObject());
//                    }
//                }
//            }
//        }
//        return props;
//    }

    public static OWLNamedIndividual getIndividual(IRI iri, Set<OWLOntology> onts) {
        for (OWLOntology ont : onts){
            if (ont.containsIndividualInSignature(iri)){
                return ont.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iri);
            }
        }
        return null;
    }

    public static boolean isStructural(OWLReasoner r) {
        return r!=null && "Structural Reasoner".equals(r.getReasonerName());
    }
}
