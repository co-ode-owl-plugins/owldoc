package org.coode.owl.mngr.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.coode.owl.mngr.NamedObjectType;
import org.coode.owl.mngr.OWLEntityFinder;
import org.coode.owl.mngr.OWLServer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;

/**
 * Author: Nick Drummond<br>
 * nick.drummond@cs.manchester.ac.uk<br>
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 20, 2007<br><br>
 * <p/>
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)<br>
 * copyright 2006, The University of Manchester<br>
 *
 * Implementation of finder that takes a partial string and does a regexp search based on the renderings
 * in the name mapper provided.
 * - Stars (*) are replaced with .* for wildcard searching
 * - Searches are not case sensitive
 * - Search string must be valid java regexp (apart from variations described above)
 */
public class OWLEntityFinderImpl implements OWLEntityFinder {

    private BidirectionalShortFormProvider cache;
    private OWLServer server;


    public OWLEntityFinderImpl(BidirectionalShortFormProvider cache, OWLServer server) {
        this.cache = cache;
        this.server = server;
    }

    @Override
    public Set<OWLClass> getOWLClasses (String str){
        Set<OWLClass> results = new HashSet<>();
        getMatches(str, results, NamedObjectType.classes);
        return results;
    }

    @Override
    public Set<OWLObjectProperty> getOWLObjectProperties(String str) {
        Set<OWLObjectProperty> results = new HashSet<>();
        getMatches(str, results, NamedObjectType.objectproperties);
        return results;
    }

    @Override
    public Set<OWLDataProperty> getOWLDataProperties(String str) {
        Set<OWLDataProperty> results = new HashSet<>();
        getMatches(str, results, NamedObjectType.dataproperties);
        return results;
    }


    @Override
    public Set<OWLAnnotationProperty> getOWLAnnotationProperties(String str) {
        Set<OWLAnnotationProperty> results = new HashSet<>();
        getMatches(str, results, NamedObjectType.annotationproperties);
        return results;
    }


    @Override
    public Set<OWLNamedIndividual> getOWLIndividuals(String str) {
        Set<OWLNamedIndividual> results = new HashSet<>();
        getMatches(str, results, NamedObjectType.individuals);
        return results;
    }


    @Override
    public Set<OWLDatatype> getOWLDatatypes(String str) {
        Set<OWLDatatype> results = new HashSet<>();
        getMatches(str, results, NamedObjectType.datatypes);
        return results;
    }


    @Override
    public Set<OWLProperty> getOWLProperties(String str) {
        Set<OWLProperty> results = new HashSet<>();
        results.addAll(getOWLObjectProperties(str));
        results.addAll(getOWLDataProperties(str));
        return results;
    }

    @Override
    public Set<OWLEntity> getOWLEntities(String str) {
        Set<OWLEntity> results = new HashSet<>();
        for (NamedObjectType subType : NamedObjectType.entitySubtypes()){
            getMatches(str, results, subType);
        }
        return results;
    }

    @Override
    public Set<OWLEntity> getOWLEntities(String str, NamedObjectType type) {
        Set<OWLEntity> results = new HashSet<>();
        getMatches(str, results, type);
        return results;
    }

    @Override
    public Set<OWLEntity> getOWLEntities(String str, NamedObjectType type, OWLOntology ont) {
        final Set<OWLEntity> results = getOWLEntities(str, type);
        if (ont != null){
            for (OWLEntity result : results){
                if (!ont.containsEntityInSignature(result.getIRI())){
                    results.remove(result);
                }
            }
        }
        return results;
    }

    @Override
    public Set<OWLEntity> getOWLEntities(IRI iri, NamedObjectType type) {
        if (!iri.isAbsolute()){
            throw new IllegalArgumentException("URI must be absolute");
        }

        switch(type){
            case entities:
                Set<OWLEntity> results = new HashSet<>();
                for (NamedObjectType subType : NamedObjectType.entitySubtypes()){
                    OWLEntity entity = subType.getOWLEntity(iri, server.getOWLOntologyManager().getOWLDataFactory());
                    for (OWLOntology ont : server.getActiveOntologies()){
                        if (ont.containsEntityInSignature(entity)){
                            results.add(entity);
                        }
                    }
                }
                return results;
            case ontologies:
                throw new RuntimeException("Cannot get entities of type ontology");
            default:
                OWLEntity entity = type.getOWLEntity(iri, server.getOWLOntologyManager().getOWLDataFactory());
                for (OWLOntology ont : server.getActiveOntologies()){
                    if (ont.containsEntityInSignature(entity)){
                        return Collections.singleton(entity);
                    }
                }
        }

        return Collections.emptySet();
    }

    @Override
    public Set<OWLEntity> getOWLEntities(IRI iri, NamedObjectType type, OWLOntology ont) {
        final Set<OWLEntity> results = getOWLEntities(iri, type);
        if (ont != null){
            for (OWLEntity result : results){
                if (!ont.containsEntityInSignature(result)){
                    results.remove(result);
                }
            }
        }
        return results;
    }

    @Override
    public void dispose() {
        cache.dispose();
        cache = null;
        server = null;
    }

    private <T extends OWLEntity> void getMatches(String str, Set<T> results, NamedObjectType type){
        try{
            Pattern pattern = Pattern.compile(str.toLowerCase());
            for (String rendering : cache.getShortForms()) { // not very efficient looking at all entity types
                Matcher m = pattern.matcher(rendering.toLowerCase());
                if (m.matches()) {
                    for (OWLEntity entity : cache.getEntities(rendering)){
                        if (type.getCls().isAssignableFrom(entity.getClass())){
                            results.add((T)entity);
                        }
                    }
                }
            }
        }
        catch(PatternSyntaxException e){
            e.printStackTrace();
        }
    }
}
