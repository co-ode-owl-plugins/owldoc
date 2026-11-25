package org.coode.html.test;

import org.coode.html.OntologyExporter;
import org.coode.html.impl.OWLHTMLKitImpl;
import org.coode.owl.mngr.impl.OWLServerImpl;
import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ExportRegressionTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private OWLOntologyManager manager;
    private OWLDataFactory df;
    private OWLOntology ontology;
    private OWLServerImpl server;
    private OWLHTMLKitImpl kit;
    private final URL base;

    public ExportRegressionTest() throws Exception { base = new URL("http://example.org/docs/"); }

    @Before public void setUp() throws Exception {
        manager = OWLManager.createOWLOntologyManager();
        df = manager.getOWLDataFactory();
        ontology = manager.createOntology(IRI.create("urn:owldoc:test"));
        server = new OWLServerImpl(manager);
        server.setActiveOntology(ontology);
        kit = new OWLHTMLKitImpl("test", server, base);
    }

    @After public void tearDown() { kit.dispose(); }

    @Test public void exportsClassesPropertiesIndividualsAndRestrictions() throws Exception {
        OWLClass person = cls("Person"), pet = cls("Pet");
        OWLObjectProperty owns = df.getOWLObjectProperty(IRI.create("urn:owldoc:owns"));
        OWLDataProperty age = df.getOWLDataProperty(IRI.create("urn:owldoc:age"));
        OWLNamedIndividual alice = df.getOWLNamedIndividual(IRI.create("urn:owldoc:Alice"));
        manager.addAxiom(ontology, df.getOWLSubClassOfAxiom(person, df.getOWLObjectSomeValuesFrom(owns, pet)));
        manager.addAxiom(ontology, df.getOWLObjectPropertyDomainAxiom(owns, person));
        manager.addAxiom(ontology, df.getOWLObjectPropertyRangeAxiom(owns, pet));
        manager.addAxiom(ontology, df.getOWLClassAssertionAxiom(person, alice));
        manager.addAxiom(ontology, df.getOWLDataPropertyAssertionAxiom(age, alice, 42));
        Path output = export();
        for (OWLEntity entity : new OWLEntity[]{person, pet, owns, age, alice}) {
            assertTrue(page(output, entity).toString(), Files.exists(page(output, entity)));
        }
        String html = Files.readString(page(output, person));
        assertTrue(html, html.contains("owns"));
        assertTrue(html, html.contains("Pet"));
        String usage = Files.readString(page(output, pet));
        assertTrue(usage, usage.contains("&#x2291;"));
        assertFalse(usage, usage.contains("&sube;"));
        assertLocalLinksExist(output);
    }

    @Test public void exportsImportedEntitiesAndTheirLinks() throws Exception {
        OWLOntology imported = manager.createOntology(IRI.create("urn:owldoc:imported"));
        OWLClass importedClass = cls("Imported");
        manager.addAxiom(imported, df.getOWLDeclarationAxiom(importedClass));
        manager.applyChange(new AddImport(ontology, df.getOWLImportsDeclaration(imported.getOntologyID().getOntologyIRI().get())));
        manager.addAxiom(ontology, df.getOWLSubClassOfAxiom(cls("Local"), importedClass));
        Path output = export();
        assertTrue(Files.exists(page(output, importedClass)));
        assertLocalLinksExist(output);
    }

    @Test public void unicodeAndReservedCharactersProduceWorkingLinks() throws Exception {
        for (String name : new String[]{"上吐下泻", "café", "A+B", "A%20B"}) {
            manager.addAxiom(ontology, df.getOWLDeclarationAxiom(cls(name)));
        }
        Path output = export();
        for (String name : new String[]{"上吐下泻", "café", "A+B", "A%20B"}) {
            assertTrue(name, Files.exists(page(output, cls(name))));
        }
        assertLocalLinksExist(output);
    }

    @Test public void preservesUnicodeAnnotationsAndEscapesMarkup() throws Exception {
        OWLClass entity = cls("Annotated");
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(entity));
        manager.addAxiom(ontology, df.getOWLAnnotationAssertionAxiom(df.getRDFSComment(), entity.getIRI(),
                df.getOWLLiteral("café 上吐下泻 <tag> & text", "fr")));
        String html = Files.readString(page(export(), entity));
        assertTrue(html, html.contains("café 上吐下泻"));
        assertTrue(html, html.contains("&lt;tag&gt;"));
        assertTrue(html, html.contains("&amp; text"));
        assertTrue(html, html.contains("@fr"));
    }

    @Test public void rendersEquivalentDisjointAndSuperProperties() throws Exception {
        OWLObjectProperty first = df.getOWLObjectProperty(IRI.create("urn:owldoc:first"));
        OWLObjectProperty equivalent = df.getOWLObjectProperty(IRI.create("urn:owldoc:equivalent"));
        OWLObjectProperty disjoint = df.getOWLObjectProperty(IRI.create("urn:owldoc:disjoint"));
        manager.addAxiom(ontology, df.getOWLEquivalentObjectPropertiesAxiom(first, equivalent));
        manager.addAxiom(ontology, df.getOWLDisjointObjectPropertiesAxiom(first, disjoint));
        OWLDataProperty data = df.getOWLDataProperty(IRI.create("urn:owldoc:data"));
        OWLDataProperty dataEquivalent = df.getOWLDataProperty(IRI.create("urn:owldoc:dataEquivalent"));
        manager.addAxiom(ontology, df.getOWLEquivalentDataPropertiesAxiom(data, dataEquivalent));
        manager.addAxiom(ontology, df.getOWLSubAnnotationPropertyOfAxiom(df.getRDFSLabel(), df.getRDFSComment()));
        Path output = export();
        String objectPage = Files.readString(page(output, first));
        assertTrue(objectPage, objectPage.contains("Equivalent Properties"));
        assertTrue(objectPage, objectPage.contains("Disjoint Properties"));
        String dataPage = Files.readString(page(output, data));
        assertTrue(dataPage, dataPage.contains("dataEquivalent"));
        assertLocalLinksExist(output);
    }

    @Test public void staticExternalLinksHaveAnAccessibleVectorIconAndNoLoadControl() throws Exception {
        OWLClass entity = cls("Depicted");
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(entity));
        manager.addAxiom(ontology, df.getOWLAnnotationAssertionAxiom(df.getRDFSSeeAlso(), entity.getIRI(),
                IRI.create("https://example.org/image.jpg?a=1&b=2")));
        String html = Files.readString(page(export(), entity));
        assertFalse(html.contains("Attempt to load"));
        assertFalse(html.contains("action=load"));
        assertFalse(html.contains("external.png"));
        assertTrue(html.contains("class=\"external-link\""));
        assertTrue(html.contains("aria-label=\"Open external link in a new tab\""));
        assertTrue(html.contains("<svg"));
        assertTrue(html.contains("image.jpg?a=1&amp;b=2"));
    }

    @Test public void exportsInlineImagePreviewsAndUsesImgursDirectImageHost() throws Exception {
        OWLClass entity = cls("Depicted");
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(entity));
        manager.addAxiom(ontology, df.getOWLAnnotationAssertionAxiom(df.getRDFSSeeAlso(), entity.getIRI(),
                IRI.create("https://imgur.com/rKr2e6v.jpg")));
        manager.addAxiom(ontology, df.getOWLAnnotationAssertionAxiom(df.getRDFSSeeAlso(), entity.getIRI(),
                IRI.create("https://example.org/photo.WEBP?size=large&v=2")));
        String html = Files.readString(page(export(), entity));
        assertTrue(html.contains("src=\"https://i.imgur.com/rKr2e6v.jpg\""));
        assertTrue(html.contains("src=\"https://example.org/photo.WEBP?size=large&amp;v=2\""));
        assertTrue(html.contains("loading=\"lazy\""));
        assertTrue(html.contains("Open full-size image in a new tab"));
        assertTrue(html.contains("class=\"image-status\" hidden"));
    }

    @Test public void prioritizesLabelsDefinitionsAndSynonymsForEntitiesAndOntologies() throws Exception {
        OWLClass entity = cls("AnnotatedOrder");
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(entity));
        String[] properties = {"http://purl.org/dc/terms/creator", "http://www.w3.org/2004/02/skos/core#altLabel",
                "http://www.w3.org/2004/02/skos/core#definition", "http://www.w3.org/2000/01/rdf-schema#label"};
        String[] values = {"Metadata last", "Synonym third", "Definition second", "Label first"};
        for (int i = 0; i < properties.length; i++) {
            OWLAnnotation annotation = df.getOWLAnnotation(df.getOWLAnnotationProperty(IRI.create(properties[i])), df.getOWLLiteral(values[i]));
            manager.addAxiom(ontology, df.getOWLAnnotationAssertionAxiom(entity.getIRI(), annotation));
            manager.applyChange(new AddOntologyAnnotation(ontology, annotation));
        }
        Path output = export();
        for (OWLObject object : new OWLObject[]{entity, ontology}) {
            String html = Files.readString(page(output, object));
            int label = html.indexOf("Label first"), definition = html.indexOf("Definition second");
            int synonym = html.indexOf("Synonym third"), metadata = html.indexOf("Metadata last");
            assertTrue(html, label >= 0 && definition > label && synonym > definition && metadata > synonym);
        }
    }

    @Test public void reportsMonotonicProgressForUniqueEntitiesAndFinishingStages() throws Exception {
        OWLOntology imported = manager.createOntology(IRI.create("urn:progress:imported"));
        OWLClass shared = cls("Shared");
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(shared));
        manager.addAxiom(imported, df.getOWLDeclarationAxiom(shared));
        manager.applyChange(new AddImport(ontology, df.getOWLImportsDeclaration(imported.getOntologyID().getOntologyIRI().get())));
        java.util.List<String> stages = new java.util.ArrayList<>();
        java.util.List<Integer> completed = new java.util.ArrayList<>();
        int[] total = {0};
        OntologyExporter exporter = new OntologyExporter(kit);
        exporter.setProgressListener((stage, done, count) -> {
            stages.add(stage); completed.add(done); total[0] = count;
            assertTrue(stage, done >= 0 && done <= count);
        });
        Path output = temp.newFolder().toPath();
        exporter.export(output.toFile());
        for (int i = 1; i < completed.size(); i++) assertTrue(completed.get(i) >= completed.get(i - 1));
        assertEquals(3, total[0]); // Shared, owl:Thing, owl:Nothing; shared is counted once.
        assertEquals(Integer.valueOf(total[0]), completed.get(completed.size() - 1));
        assertTrue(stages.contains("Writing category indexes"));
        assertEquals("Complete", stages.get(stages.size() - 1));
    }

    @Test public void cancellationStopsBetweenPagesAndRestoresRenderingConfiguration() throws Exception {
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(cls("Cancelled")));
        OntologyExporter exporter = new OntologyExporter(kit);
        exporter.setProgressListener(new OntologyExporter.ProgressListener() {
            private boolean cancelled;
            public void updateProgress(String stage, int done, int total) { if (done >= 1) cancelled = true; }
            public boolean isCancelled() { return cancelled; }
        });
        Path output = temp.newFolder().toPath();
        try {
            exporter.export(output.toFile());
            fail("Expected cancellation");
        } catch (java.util.concurrent.CancellationException expected) {
            assertFalse(Files.exists(output.resolve("index.html")));
            assertNull(kit.getHTMLProperties().get(org.coode.html.impl.OWLHTMLProperty.optionStaticSite));
            assertNull(kit.getHTMLProperties().get(org.coode.html.impl.OWLHTMLProperty.optionContentWindow));
        }
    }

    @Test public void annotationSubjectsUseIndexedLookupsAndPreservePunnedEntities() throws Exception {
        IRI subject = IRI.create("urn:owldoc:Punned");
        OWLClass cls = df.getOWLClass(subject);
        OWLNamedIndividual individual = df.getOWLNamedIndividual(subject);
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(cls));
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(individual));
        int[] membershipChecks = {0};
        OWLOntology instrumented = (OWLOntology) java.lang.reflect.Proxy.newProxyInstance(
                OWLOntology.class.getClassLoader(), new Class<?>[]{OWLOntology.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getEntitiesInSignature")) throw new AssertionError("Full signature scan");
                    if (method.getName().equals("containsEntityInSignature")) throw new AssertionError("Generic lookup fallback");
                    if (method.getName().startsWith("contains") && method.getName().endsWith("InSignature")) membershipChecks[0]++;
                    try { return method.invoke(ontology, args); }
                    catch (java.lang.reflect.InvocationTargetException exception) { throw exception.getCause(); }
                });
        java.io.StringWriter text = new java.io.StringWriter();
        org.coode.html.renderer.OWLHTMLVisitor visitor = new org.coode.html.renderer.OWLHTMLVisitor(kit, new java.io.PrintWriter(text));
        visitor.setOntologies(java.util.Collections.singleton(instrumented));
        visitor.setActiveOntology(ontology);
        visitor.setPageURL(base);
        df.getOWLAnnotationAssertionAxiom(df.getRDFSLabel(), subject, df.getOWLLiteral("Punned label")).accept(visitor);
        String html = text.toString();
        assertTrue(html, html.contains("classes/Punned"));
        assertTrue(html, html.contains("individuals/Punned"));
        assertEquals(6, membershipChecks[0]);
    }

    @Test public void paginatesUsageWithoutLosingOrDuplicatingReferencesAndPreservesEncodedLinks() throws Exception {
        OWLDatatype datatype = df.getOWLDatatype(IRI.create("urn:owldoc:datatype-café+A"));
        addUsageReferences(datatype, 1003);
        Path output = export();
        Path main = page(output, datatype);
        String filename = main.getFileName().toString().replace(".html", "");
        java.util.Set<Integer> references = new java.util.HashSet<>();
        for (int number = 1; number <= 3; number++) {
            Path current = number == 1 ? main : main.resolveSibling(filename + "--usage-" + number + ".html");
            String html = Files.readString(current);
            Matcher matches = Pattern.compile("REFERENCE-(\\d+)").matcher(html);
            int count = 0;
            while (matches.find()) { assertTrue("Duplicate reference", references.add(Integer.parseInt(matches.group(1)))); count++; }
            assertEquals(number == 3 ? 3 : 500, count);
            assertTrue(html.contains("aria-current=\"page\">" + number));
            if (number > 1) {
                assertTrue(html.contains("data-entity-path=\"datatypes/"));
                assertTrue(html.contains("data-section=\"Usage · page " + number + " of 3"));
                assertTrue(html.contains("#usage_(1003)"));
            }
        }
        assertEquals(1003, references.size());
        assertFalse(Files.exists(main.resolveSibling(filename + "--usage-4.html")));
        assertLocalLinksExist(output);
    }

    @Test public void smallUsageListsAndInAppRenderingRemainUnpaginated() throws Exception {
        OWLDatatype datatype = df.getStringOWLDatatype();
        addUsageReferences(datatype, 500);
        Path output = export();
        String html = Files.readString(page(output, datatype));
        assertFalse(html.contains("usage-pagination"));
        assertFalse(html.contains("--usage-2.html"));
        addUsageReferences(datatype, 1); // Duplicate values do not add duplicate axioms.
        manager.addAxiom(ontology, df.getOWLAnnotationAssertionAxiom(df.getRDFSLabel(), IRI.create("urn:extra"), df.getOWLLiteral("EXTRA", datatype)));
        org.coode.html.doclet.UsageDoclet<OWLEntity> doclet = new org.coode.html.doclet.UsageDoclet<>(kit);
        doclet.setUserObject(datatype);
        java.io.StringWriter content = new java.io.StringWriter();
        doclet.renderAll(base, new java.io.PrintWriter(content));
        assertFalse(content.toString().contains("usage-pagination"));
        assertTrue(content.toString().contains("EXTRA"));
    }

    @Test public void cancellationDuringUsagePaginationPropagatesAndRestoresExportState() throws Exception {
        addUsageReferences(df.getStringOWLDatatype(), 1003);
        OntologyExporter exporter = new OntologyExporter(kit);
        exporter.setProgressListener(new OntologyExporter.ProgressListener() {
            boolean cancelled;
            public void updateProgress(String stage, int done, int total) {
                if (stage.startsWith("Usage:") && stage.endsWith("page 2 of 3")) cancelled = true;
            }
            public boolean isCancelled() { return cancelled; }
        });
        Path output = temp.newFolder().toPath();
        try { exporter.export(output.toFile()); fail("Expected cancellation between usage pages"); }
        catch (java.util.concurrent.CancellationException expected) {
            assertFalse(Files.exists(output.resolve("index.html")));
            assertNull(kit.getHTMLProperties().get(org.coode.html.impl.OWLHTMLProperty.optionStaticSite));
        }
    }

    private void addUsageReferences(OWLDatatype datatype, int count) {
        for (int i = 0; i < count; i++) {
            manager.addAxiom(ontology, df.getOWLAnnotationAssertionAxiom(df.getRDFSLabel(), IRI.create("urn:reference:" + i),
                    df.getOWLLiteral("REFERENCE-" + i, datatype)));
        }
    }

    @Test public void exportsAnonymousOntology() throws Exception {
        server.setActiveOntology(manager.createOntology());
        manager.addAxiom(server.getActiveOntology(), df.getOWLDeclarationAxiom(cls("AnonymousRoot")));
        assertLocalLinksExist(export());
    }

    @Test public void exportsASelfContainedFramelessSite() throws Exception {
        manager.addAxiom(ontology, df.getOWLSubClassOfAxiom(cls("Child"), cls("Parent")));
        Path output = export();
        String home = Files.readString(output.resolve("index.html"));
        assertFalse(home.contains("<frameset"));
        assertFalse(home.contains("<iframe"));
        assertTrue(home.contains("id=\"owldoc-page\""));
        assertTrue(home.contains("js/entities.js"));
        assertTrue(home.contains("js/owldoc.js"));
        String child = Files.readString(page(output, cls("Child")));
        assertTrue(child.contains("data-root=\"../\""));
        assertFalse(child.contains("target=\"content\""));
        assertTrue(Files.exists(output.resolve("js/entities.js")));
        assertTrue(Files.exists(output.resolve("js/owldoc.js")));
        String data = Files.readString(output.resolve("js/entities.js"));
        com.fasterxml.jackson.databind.JsonNode index = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(data.substring(data.indexOf('{'), data.lastIndexOf('}') + 1));
        boolean foundChild = false;
        for (com.fasterxml.jackson.databind.JsonNode entry : index.get("entities")) {
            Path local = output.resolve(java.net.URI.create(entry.get("path").asText()).getPath());
            assertTrue(local.toString(), Files.exists(local));
            if (entry.get("iri").asText().equals(cls("Child").getIRI().toString())) {
                foundChild = true;
                assertEquals("classes", entry.get("type").asText());
                assertEquals("Child", entry.get("label").asText());
            }
        }
        assertTrue(foundChild);
        assertNull(kit.getHTMLProperties().get(org.coode.html.impl.OWLHTMLProperty.optionContentWindow));
        assertNull(kit.getHTMLProperties().get(org.coode.html.impl.OWLHTMLProperty.optionStaticSite));
        assertLocalLinksExist(output);
    }

    @Test public void exportingAgainRefreshesExistingPagesAndSearchIndex() throws Exception {
        OWLClass child = cls("Child"), parent = cls("Parent");
        OWLAxiom old = df.getOWLSubClassOfAxiom(child, parent);
        manager.addAxiom(ontology, old);
        Path output = export();
        String before = Files.readString(page(output, child));
        manager.removeAxiom(ontology, old);
        manager.addAxiom(ontology, df.getOWLSubClassOfAxiom(child, cls("NewParent")));
        new OntologyExporter(kit).export(output.toFile());
        String after = Files.readString(page(output, child));
        assertNotEquals(before, after);
        assertTrue(after.contains("NewParent"));
        assertLocalLinksExist(output);
    }

    @Test public void failedExportRestoresTheRenderingConfiguration() throws Exception {
        try {
            new OntologyExporter(kit).export(temp.newFile());
            fail("Expected export into a file to fail");
        } catch (Exception expected) {
            assertNull(kit.getHTMLProperties().get(org.coode.html.impl.OWLHTMLProperty.optionContentWindow));
            assertNull(kit.getHTMLProperties().get(org.coode.html.impl.OWLHTMLProperty.optionStaticSite));
        }
    }

    private OWLClass cls(String name) { return df.getOWLClass(IRI.create("urn:owldoc:" + name)); }
    private Path export() throws Exception {
        Path output = temp.newFolder().toPath();
        assertTrue(new OntologyExporter(kit).export(output.toFile()).isFile());
        return output;
    }
    private Path page(Path output, OWLObject entity) throws Exception {
        String relative = base.toURI().relativize(kit.getURLScheme().getURLForOWLObject(entity).toURI()).getPath();
        return output.resolve(relative);
    }
    private void assertLocalLinksExist(Path output) throws Exception {
        List<Path> pages;
        try (java.util.stream.Stream<Path> paths = Files.walk(output)) {
            pages = paths.filter(path -> path.toString().endsWith(".html")).collect(Collectors.toList());
        }
        assertFalse(pages.isEmpty());
        Pattern links = Pattern.compile("(?:href|src)=[\"']([^\"']+)[\"']");
        for (Path page : pages) {
            Matcher matcher = links.matcher(Files.readString(page));
            while (matcher.find()) {
                String target = matcher.group(1).replace("&amp;", "&");
                if (target.startsWith("#") || target.matches("[a-zA-Z]+:.*")) continue;
                java.net.URI resolved = page.toUri().resolve(target);
                Path local = Path.of(new java.net.URI(resolved.getScheme(), resolved.getAuthority(), resolved.getPath(), null, null));
                assertTrue(page + " -> " + target, Files.exists(local));
            }
        }
    }
}
