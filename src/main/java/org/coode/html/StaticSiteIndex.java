package org.coode.html;

import org.coode.html.util.URLUtils;
import org.coode.owl.mngr.NamedObjectType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A shared search index loaded as a local script, including on file: URLs. */
final class StaticSiteIndex {
    private final OWLHTMLKit kit;
    private final Map<OWLObject, Entry> entries = new LinkedHashMap<>();

    StaticSiteIndex(OWLHTMLKit kit) { this.kit = kit; }

    void add(OWLObject object) {
        String label;
        String iri;
        if (object instanceof OWLEntity) {
            OWLEntity entity = (OWLEntity) object;
            label = kit.getOWLServer().getShortFormProvider().getShortForm(entity);
            iri = entity.getIRI().toString();
        } else {
            OWLOntology ontology = (OWLOntology) object;
            label = kit.getOWLServer().getOntologyShortFormProvider().getShortForm(ontology);
            iri = ontology.getOntologyID().getOntologyIRI().map(Object::toString).orElse("");
        }
        String path = URLUtils.createRelativeURL(kit.getBaseURL(), kit.getURLScheme().getURLForOWLObject(object));
        entries.put(object, new Entry(label, iri, NamedObjectType.getType(object).name(), path));
    }

    void write(File root) throws IOException {
        List<Entry> sorted = new ArrayList<>(entries.values());
        sorted.sort(Comparator.comparing((Entry entry) -> entry.label, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entry -> entry.path));
        StringBuilder data = new StringBuilder("window.OWLDOC_DATA = {\"title\":");
        data.append(json(kit.getOWLServer().getOntologyShortFormProvider().getShortForm(kit.getOWLServer().getActiveOntology())));
        data.append(",\"home\":").append(json(URLUtils.createRelativeURL(kit.getBaseURL(), kit.getURLScheme().getURLForOWLObject(kit.getOWLServer().getActiveOntology()))));
        data.append(",\"types\":[");
        boolean first = true;
        for (NamedObjectType type : NamedObjectType.values()) {
            if (type == NamedObjectType.entities) continue;
            if (!first) data.append(',');
            first = false;
            data.append("{\"id\":").append(json(type.name())).append(",\"label\":")
                    .append(json(type.getPluralRendering())).append('}');
        }
        data.append("],\"entities\":[");
        first = true;
        for (Entry entry : sorted) {
            if (!first) data.append(',');
            first = false;
            data.append("{\"label\":").append(json(entry.label)).append(",\"iri\":")
                    .append(json(entry.iri)).append(",\"type\":").append(json(entry.type))
                    .append(",\"path\":").append(json(entry.path)).append('}');
        }
        data.append("]};\n");
        File file = new File(root, "js/entities.js");
        Files.createDirectories(file.toPath().getParent());
        Files.writeString(file.toPath(), data, StandardCharsets.UTF_8);
    }

    private static String json(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char character : value.toCharArray()) {
            switch (character) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (character < 32 || character == '<' || character == '>' || character == '&'
                            || character == '\u2028' || character == '\u2029') {
                        out.append(String.format("\\u%04x", (int) character));
                    } else out.append(character);
            }
        }
        return out.append('"').toString();
    }

    private static final class Entry {
        final String label, iri, type, path;
        Entry(String label, String iri, String type, String path) {
            this.label = label; this.iri = iri; this.type = type; this.path = path;
        }
    }
}
