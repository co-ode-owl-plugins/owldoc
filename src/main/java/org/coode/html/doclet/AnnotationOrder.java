package org.coode.html.doclet;

import java.util.Comparator;
import org.coode.html.OWLHTMLKit;
import org.semanticweb.owlapi.model.OWLAnnotation;

/** Present names and descriptions before administrative and other metadata. */
final class AnnotationOrder {
    private AnnotationOrder() {}

    static Comparator<OWLAnnotation> comparator(OWLHTMLKit kit) {
        return Comparator.comparingInt(AnnotationOrder::priority)
                .thenComparing(kit.getOWLServer().getComparator());
    }

    private static int priority(OWLAnnotation annotation) {
        String iri = annotation.getProperty().getIRI().toString();
        switch (iri) {
            case "http://www.w3.org/2000/01/rdf-schema#label":
            case "http://www.w3.org/2004/02/skos/core#prefLabel":
                return 0;
            case "http://www.w3.org/2004/02/skos/core#definition":
            case "http://purl.obolibrary.org/obo/IAO_0000115":
                return 1;
            case "http://www.w3.org/2004/02/skos/core#altLabel":
            case "http://www.geneontology.org/formats/oboInOwl#hasExactSynonym":
            case "http://www.geneontology.org/formats/oboInOwl#hasBroadSynonym":
            case "http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym":
            case "http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym":
                return 2;
            default:
                return 3;
        }
    }
}
