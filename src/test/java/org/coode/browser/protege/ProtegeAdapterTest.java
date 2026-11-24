package org.coode.browser.protege;

import org.coode.html.OntologyExporter;
import org.coode.html.impl.OWLHTMLKitImpl;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class ProtegeAdapterTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void preservesSetAndStreamShortFormAPIs() {
        OWLModelManager model = mock(OWLModelManager.class, RETURNS_DEEP_STUBS);
        OWLClass entity = OWLManager.getOWLDataFactory().getOWLClass(IRI.create("urn:owldoc:Entity"));
        when(model.getOWLEntityFinder().getMatchingOWLEntities("Entity")).thenReturn(Collections.singleton(entity));
        when(model.getOWLEntityFinder().getOWLEntityRenderings()).thenReturn(Collections.singleton("Entity"));
        ProtegeBidirectionalShortFormProvider provider = new ProtegeBidirectionalShortFormProvider(model);
        assertEquals(provider.getEntities("Entity"), provider.entities("Entity").collect(Collectors.toSet()));
        assertEquals(provider.getShortForms(), provider.shortForms().collect(Collectors.toSet()));
        assertEquals(entity, provider.getEntity("Entity"));
    }

    @Test public void exportsThroughTheProtegeModelAdapter() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology(IRI.create("http://example.org/ontology"));
        OWLClass entity = df.getOWLClass(IRI.create("http://example.org/ontology#Example"));
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(entity));
        manager.addAxiom(ontology, df.getOWLAnnotationAssertionAxiom(df.getRDFSComment(), entity.getIRI(), df.getOWLLiteral("Adapter export", "en")));
        OWLModelManager model = mock(OWLModelManager.class, RETURNS_DEEP_STUBS);
        when(model.getActiveOntology()).thenReturn(ontology);
        when(model.getActiveOntologies()).thenReturn(Collections.singleton(ontology));
        when(model.getOntologies()).thenReturn(manager.getOntologies());
        when(model.getOWLOntologyManager()).thenReturn(manager);
        when(model.getRendering(any(OWLObject.class))).thenAnswer(call -> {
            OWLObject object = call.getArgument(0);
            return object instanceof OWLEntity ? ((OWLEntity) object).getIRI().getShortForm() : "ontology";
        });
        OWLReasoner reasoner = new StructuralReasonerFactory().createReasoner(ontology);
        when(model.getOWLReasonerManager().getCurrentReasoner()).thenReturn(reasoner);
        ProtegeServerImpl server = new ProtegeServerImpl(model);
        OWLHTMLKitImpl kit = new OWLHTMLKitImpl("adapter", server, new URL("http://example.org/docs/"));
        try {
            java.io.File directory = temp.newFolder("export with spaces");
            assertTrue(new OntologyExporter(kit).export(directory).isFile());
            java.nio.file.Path page = directory.toPath().resolve("classes").resolve(
                    java.net.URI.create(kit.getURLScheme().getURLForOWLObject(entity).getPath()).getPath().replaceFirst("^/docs/classes/", ""));
            assertTrue(page.toString(), Files.exists(page));
            assertTrue(Files.readString(page).contains("Adapter export"));
        } finally {
            kit.dispose();
            reasoner.dispose();
        }
    }
}
