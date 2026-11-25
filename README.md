OWLDoc
======

A Protégé Desktop plugin that creates a bundle of static HTML pages for publishing to the Web or distributing to colleagues.

<img src="https://raw.githubusercontent.com/protegeproject/github-wiki-resources/master/owldoc/README/owldoc-html-page.png" alt="HTML static pages created by OWLDoc" width="650px"/>

Installation
------------
By default OWLDoc comes together with Protégé installation bundle. Any new release of the plugin will be automatically updated using Protégé auto-update mechanism. 

Exports use a frameless documentation layout with a searchable, resizable sidebar, category filters, resource icons, and a copyable IRI. Search, category, list position, and sidebar width are preserved during navigation. All assets are local, so opening `index.html` works offline.

The OWLDoc plugin offers two features: (1) exporting HTML pages and (2) window viewing within Protégé. The page rendering will provide all the neccessary links for resource navigation of your ontology.

Export HTML
-----------
<img src="https://raw.githubusercontent.com/protegeproject/github-wiki-resources/master/owldoc/README/owldoc-tool-menu.png" alt="OWLDoc menu item"/>

This feature can be found in menu bar **Tools > Export OWLDoc...**. The plugin will ask a base directory to store all the generated files. The export runs in the background. A compact status-area panel shows entity counts, percentage, elapsed time, and the current page or finishing stage. **Cancel** stops after the current page finishes; partial files remain in the output directory. When complete, choose **Open** to view the documentation. Completion, cancellation, and errors are shown inline without a modal dialog. These controls are implemented in the plugin using the existing workspace status area.

OWLDoc View
-----------
This feature is available by selecting **Window > Views > Miscellaneous Views > OWLDoc**. To have a proper placement, select the **Entities** tab first and then select the menu item. Place the pointer next to the class hierarchy tree panel and set the view location there. Select a class entity to see the result.

<img src="https://github.com/protegeproject/github-wiki-resources/blob/master/owldoc/README/owldoc-in-protege.png" alt="OWLDoc as view in Protege" width="650px"/>

## Protégé 6 migration

OWLDoc 4 targets Protégé 6.0.0-alpha-1, OWL API 5.5.1, Java 11, with its ontology-management and HTML-export code included in this repository. Protégé 5 requires OWLDoc 3.

Run `mvn --batch-mode clean verify` in this repository. No separate Ontology Browser build or artifact is required. The plugin tests verify the short-form Set/Stream adapters and a real export through the Protégé model adapter.

Install `target/owldoc-4.0.0.jar` in an assembled Protégé 6 app's plugins directory, removing any older OWLDoc JAR first. The JAR contains the ontology-management and HTML-export classes directly. See `NOTICE` for their origin and retained license notices.

For manual verification, open `src/test/resources/smoke-test/owldoc-smoke-test.ofn` with its adjacent import ontology and catalog. Use **Tools > Export OWLDoc…**, choosing an empty output directory, preferably with spaces in its name. Check the generated class, property, and individual pages, imported entities, Unicode filenames, French annotation text, and navigation links. Also add **Window > Views > Miscellaneous Views > OWLDoc**, select entities, and follow its links to verify selection synchronization.

The same fixture is available at `/tmp/owldoc-qa-fixture/owldoc-smoke-test.ofn` in the QA environment. Publication and distribution/registry integration follow successful manual testing. Tracking: https://github.com/protegeproject/protege/issues/1363.

Static exports paginate Usage lists larger than 500 references. The entity page contains the first 500 references, with further pages linked by ordinary offline-compatible Previous/Next and numbered links. All references are retained; each extra page keeps the entity sidebar and context. Cancellation is checked between usage pages. The in-app viewer keeps its existing rendering.

## Publishing

Publishing follows the Protégé plugin workflow used by OntoGraf. A numeric `major.minor.patch` tag triggers `.github/workflows/publish.yml`, or an existing tag can be selected with `workflow_dispatch`. The tag must match the POM version. The `release` Maven profile signs artifacts with GPG and publishes through the Maven Central Portal, waiting for publication. The workflow uses the standard `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`, `OSSRH_USERNAME`, and `OSSRH_TOKEN` secrets; secret values are never stored in the repository.

Releases include source and Javadoc artifacts. GitHub release notes and the OWLDoc plugin JAR are published with the matching tag, followed by the Protégé 6 distribution and registry updates.

The former owlmanager and owlhtml code is maintained directly under `src/main/java/org/coode/owl` and `src/main/java/org/coode/html`, with its regression tests and resources. OWLDoc is built and released as one artifact; the servlet-based ontology browser is not included.
