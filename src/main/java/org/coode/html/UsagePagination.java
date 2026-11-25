package org.coode.html;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.coode.html.doclet.AbstractHTMLDoclet;
import org.coode.html.doclet.UsageDoclet;
import org.coode.html.page.OWLDocPage;
import org.coode.html.util.URLUtils;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

/** Writes ordinary static pages; no fetch, web server, or client-side bulk loading. */
final class UsagePagination implements UsageDoclet.PaginationHandler {
    private final OWLHTMLKit kit;
    private final File root;
    private final Consumer<String> progress;

    UsagePagination(OWLHTMLKit kit, File root, Consumer<String> progress) {
        this.kit = kit;
        this.root = root;
        this.progress = progress;
    }

    @Override public void render(UsageDoclet<?> source, List<OWLObject> rows, URL entityPage, PrintWriter out) {
        int pages = (rows.size() + UsageDoclet.PAGE_SIZE - 1) / UsageDoclet.PAGE_SIZE;
        renderSlice(source, rows, entityPage, entityPage, 1, pages, out);
        for (int number = 2; number <= pages; number++) {
            String label = kit.getOWLServer().getShortFormProvider().getShortForm(source.getUserObject());
            progress.accept("Usage: " + label + " · page " + number + " of " + pages);
            try {
                URL url = pageURL(entityPage, number);
                File file = new File(root, URI.create(URLUtils.createRelativeURL(kit.getBaseURL(), url)).getPath());
                Files.createDirectories(file.toPath().getParent());
                final int current = number;
                OWLDocPage<OWLEntity> page = new OWLDocPage<>(kit);
                page.setSectionTitle("Usage · page " + number + " of " + pages);
                page.addDoclet(new AbstractHTMLDoclet<OWLEntity>() {
                    @Override protected void renderHeader(URL pageURL, PrintWriter writer) {
                        writer.println("<h4>Usage (" + rows.size() + ")</h4><div class=\"codebox\">");
                        renderSlice(source, rows, entityPage, pageURL, current, pages, writer);
                    }
                    @Override protected void renderFooter(URL pageURL, PrintWriter writer) { writer.println("</div>"); }
                    @Override public String getID() { return "usage-page"; }
                });
                page.setUserObject(source.getUserObject());
                try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8))) {
                    page.renderAll(url, writer);
                    if (writer.checkError()) throw new IOException("Could not write " + file);
                }
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
    }

    private void renderSlice(UsageDoclet<?> source, List<OWLObject> rows, URL entityPage, URL currentPage,
                             int number, int pages, PrintWriter out) {
        int start = (number - 1) * UsageDoclet.PAGE_SIZE;
        int end = Math.min(start + UsageDoclet.PAGE_SIZE, rows.size());
        out.printf(Locale.ROOT, "<p class=\"usage-range\">Showing %,d–%,d of %,d references</p>%n", start + 1, end, rows.size());
        renderNavigation(source, entityPage, currentPage, number, pages, out);
        source.renderUsageRows(rows.subList(start, end), currentPage, out);
        renderNavigation(source, entityPage, currentPage, number, pages, out);
    }

    private void renderNavigation(UsageDoclet<?> source, URL entityPage, URL currentPage,
                                  int number, int pages, PrintWriter out) {
        out.println("<nav class=\"usage-pagination\" aria-label=\"Usage pages\">");
        if (number > 1) link(source, entityPage, currentPage, number - 1, "Previous", "prev", out);
        else out.println("<span aria-disabled=\"true\">Previous</span>");
        SortedSet<Integer> shown = new TreeSet<>();
        shown.add(1);
        shown.add(pages);
        for (int i = Math.max(1, number - 2); i <= Math.min(pages, number + 2); i++) shown.add(i);
        int previous = 0;
        for (int page : shown) {
            if (page > previous + 1) out.println("<span class=\"page-gap\">…</span>");
            if (page == number) out.println("<span aria-current=\"page\">" + page + "</span>");
            else link(source, entityPage, currentPage, page, Integer.toString(page), null, out);
            previous = page;
        }
        if (number < pages) link(source, entityPage, currentPage, number + 1, "Next", "next", out);
        else out.println("<span aria-disabled=\"true\">Next</span>");
        out.println("</nav>");
    }

    private void link(UsageDoclet<?> source, URL entityPage, URL currentPage, int number,
                      String label, String rel, PrintWriter out) {
        try {
            String target = URLUtils.createRelativeURL(currentPage, pageURL(entityPage, number));
            if (number == 1) target += "#" + source.getID().toLowerCase(Locale.ROOT).replace(" ", "_");
            out.print("<a href=\"" + target.replace("&", "&amp;").replace("\"", "&quot;") + "\"");
            if (rel != null) out.print(" rel=\"" + rel + "\"");
            out.println(">" + label + "</a>");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private URL pageURL(URL entityPage, int number) throws java.net.MalformedURLException {
        if (number == 1) return entityPage;
        String path = entityPage.getPath();
        String filename = path.substring(path.lastIndexOf('/') + 1);
        return new URL(entityPage, filename.substring(0, filename.length() - 5) + "--usage-" + number + ".html");
    }
}
