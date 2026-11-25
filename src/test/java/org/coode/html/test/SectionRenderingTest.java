package org.coode.html.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.coode.html.doclet.AbstractOWLElementsDoclet;
import org.coode.html.doclet.ElementsDoclet;
import org.coode.html.impl.OWLHTMLKitImpl;
import org.coode.html.impl.OWLHTMLProperty;
import org.coode.owl.mngr.impl.OWLServerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import static org.junit.Assert.*;

public class SectionRenderingTest {
    private OWLOntologyManager manager;
    private OWLHTMLKitImpl kit;
    private OWLOntology ontology;
    private CountingSection section;
    private final URL base;

    public SectionRenderingTest() throws Exception { base = new URL("http://example.org/docs/"); }

    @Before public void setUp() throws Exception {
        manager = OWLManager.createOWLOntologyManager();
        ontology = manager.createOntology(IRI.create("urn:sections"));
        kit = new OWLHTMLKitImpl("test", new OWLServerImpl(manager), base);
        kit.getHTMLProperties().setBoolean(OWLHTMLProperty.optionShowInferences, true);
        section = new CountingSection();
        section.setUserObject(cls("Subject"));
    }

    @After public void tearDown() { kit.dispose(); }

    @Test public void queriesEachCollectionOnceRegardlessOfRowCount() {
        for (int i = 0; i < 2000; i++) section.asserted.add(cls("Row" + i));
        section.inferred.add(cls("Inferred"));
        section.inferred.add(cls("Row0")); // Asserted and inferred overlap is rendered once.
        String html = render();
        assertEquals(1, section.assertedQueries);
        assertEquals(1, section.inferredQueries);
        assertEquals(2000, occurrences(html, "class=\"asserted\""));
        assertEquals(1, occurrences(html, "class=\"inferred\""));
        assertTrue(html.contains("Section (2001)"));
    }

    @Test public void refreshesBetweenRendersAndWhenInferenceSettingChanges() {
        section.asserted.add(cls("Before"));
        assertTrue(render().contains(">Before</a>"));
        section.asserted.clear();
        section.asserted.add(cls("After"));
        section.inferred.add(cls("Inferred"));
        kit.getHTMLProperties().setBoolean(OWLHTMLProperty.optionShowInferences, false);
        String html = render();
        assertTrue(html.contains(">After</a>"));
        assertFalse(html.contains(">Before</a>"));
        assertFalse(html.contains(">Inferred</a>"));
        assertEquals(2, section.assertedQueries);
        assertEquals(1, section.inferredQueries);
        section.asserted.clear();
        assertEquals("Section (0)", section.getID()); // No snapshot survives outside rendering.
        section.setUserObject(cls("AnotherSubject"));
        assertEquals("", render());
    }

    @Test public void usesConfiguredOntologiesForRowsAndTheirAssertedStatus() throws Exception {
        OWLOntology selected = manager.createOntology(IRI.create("urn:selected"));
        section.setOntologies(Set.of(selected));
        section.asserted.add(cls("Selected"));
        String html = render();
        assertEquals(Set.of(selected), section.lastOntologies);
        assertTrue(html.contains("class=\"asserted\""));
        assertFalse(html.contains("class=\"inferred\""));
        assertEquals(1, section.assertedQueries);
    }

    private String render() {
        StringWriter out = new StringWriter();
        section.renderAll(base, new PrintWriter(out));
        return out.toString();
    }

    private OWLClass cls(String name) { return manager.getOWLDataFactory().getOWLClass(IRI.create("urn:sections:" + name)); }
    private int occurrences(String text, String value) { return (text.length() - text.replace(value, "").length()) / value.length(); }

    private class CountingSection extends AbstractOWLElementsDoclet<OWLClass, OWLClass> {
        final Set<OWLClass> asserted = new HashSet<>(), inferred = new HashSet<>();
        int assertedQueries, inferredQueries;
        Set<OWLOntology> lastOntologies;
        CountingSection() { super("Section", ElementsDoclet.Format.list, SectionRenderingTest.this.kit); }
        @Override protected Collection<OWLClass> getAssertedElements(Set<OWLOntology> onts) {
            assertedQueries++;
            lastOntologies = onts;
            return asserted;
        }
        @Override protected Collection<OWLClass> getInferredElements(Set<OWLOntology> onts) {
            inferredQueries++;
            return inferred;
        }
    }
}
