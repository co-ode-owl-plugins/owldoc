OWLDoc
======

A Protégé Desktop plugin that creates a bundle of static HTML pages for publishing to the Web or distributing to colleagues.

<img src="https://raw.githubusercontent.com/protegeproject/github-wiki-resources/master/owldoc/README/owldoc-html-page.png" alt="HTML static pages created by OWLDoc" width="650px"/>

Installation
------------
By default OWLDoc comes together with Protégé installation bundle. Any new release of the plugin will be automatically updated using Protégé auto-update mechanism. 

The OWLDoc plugin offers two features: (1) exporting HTML pages and (2) window viewing within Protégé. The page rendering will provide all the neccessary links for resource navigation of your ontology.

Export HTML
-----------
<img src="https://raw.githubusercontent.com/protegeproject/github-wiki-resources/master/owldoc/README/owldoc-tool-menu.png" alt="OWLDoc menu item"/>

This feature can be found in menu bar **Tools > Export OWLDoc...**. The plugin will ask a base directory to store all the generated files. Once the exporting is done, a web browser will open to show you the result (as showed by the first image).

OWLDoc View
-----------
This feature is available by selecting **Window > Views > Miscellaneous Views > OWLDoc**. To have a proper placement, select the **Entities** tab first and then select the menu item. Place the pointer next to the class hierarchy tree panel and set the view location there. Select a class entity to see the result.

<img src="https://github.com/protegeproject/github-wiki-resources/blob/master/owldoc/README/owldoc-in-protege.png" alt="OWLDoc as view in Protege" width="650px"/>

## Protégé 6 migration

OWLDoc 4 targets Protégé 6.0.0-alpha-1, OWL API 5.5.1, Java 11, and Ontology Browser 5. Protégé 5 requires OWLDoc 3.

For a paired development build, first run `mvn --batch-mode clean install` in the Ontology Browser 5 migration checkout. Then run `mvn --batch-mode clean verify` in this repository. The plugin tests verify the short-form Set/Stream adapters and a real export through the Protégé model adapter.

Install `target/owldoc-4.0.0-SNAPSHOT.jar` in an assembled Protégé 6 app's plugins directory, removing any older OWLDoc JAR first. It embeds the two required Ontology Browser modules.

For manual verification, open `src/test/resources/smoke-test/owldoc-smoke-test.ofn` with its adjacent import ontology and catalog. Use **Tools > Export OWLDoc…**, choosing an empty output directory, preferably with spaces in its name. Check the generated class, property, and individual pages, imported entities, Unicode filenames, French annotation text, and navigation links. Also add **Window > Views > Miscellaneous Views > OWLDoc**, select entities, and follow its links to verify selection synchronization.

The same fixture is available at `/tmp/owldoc-qa-fixture/owldoc-smoke-test.ofn` in the QA environment. Publication and distribution/registry integration follow successful manual testing. Tracking: https://github.com/protegeproject/protege/issues/1363.
