package org.coode.html;


import org.apache.commons.io.FileUtils;
import org.coode.html.doclet.OWLOntologySummaryDoclet;
import org.coode.html.impl.OWLHTMLConstants;
import org.coode.html.impl.OWLHTMLKitImpl;
import org.coode.html.impl.OWLHTMLProperty;
import org.coode.html.index.OWLContentsHTMLPage;
import org.coode.html.index.OWLObjectIndexDoclet;
import org.coode.html.page.OWLDocPage;
import org.coode.html.url.URLScheme;
import org.coode.html.util.URLUtils;
import org.coode.owl.mngr.NamedObjectType;
import org.coode.owl.mngr.ServerConstants;
import org.coode.owl.mngr.ServerProperty;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: Nick Drummond<br>
 * nick.drummond@cs.manchester.ac.uk<br>
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 11, 2007<br><br>
 * <p/>
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)<br>
 * copyright 2006, The University of Manchester<br>
 *
 * Class that creates a set of static HTML pages in the directory given.
 * OWL entity subfolders are created for classes/properties/individuals/ontologies
 * Index pages are created for all entity folders (and overall)
 * A contents page is created
 */
public class OntologyExporter {

    private static Logger logger = LoggerFactory
            .getLogger(OntologyExporter.class);

    private OWLHTMLKit kit;

    /** Receives worker-thread updates; cancellation is checked between output pages. */
    public interface ProgressListener {
        void updateProgress(String stage, int completedEntities, int totalEntities);
        default boolean isCancelled() { return false; }
    }

    private ProgressListener progress = (stage, completed, total) -> {};
    private int completedEntities;
    private int totalEntities;

    public void setProgressListener(ProgressListener listener) {
        progress = java.util.Objects.requireNonNull(listener);
    }

    private void reportProgress(String stage) {
        if (progress.isCancelled()) {
            throw new java.util.concurrent.CancellationException("OWLDoc export cancelled");
        }
        progress.updateProgress(stage, completedEntities, totalEntities);
    }

    private File root;
    private StaticSiteIndex siteIndex;
    private final Set<OWLEntity> exportedEntities = new HashSet<>();

    private OWLObjectIndexDoclet<OWLObject> indexAllResourcesRenderer;

    private Map<NamedObjectType, OWLObjectIndexDoclet<OWLObject>> typeIndices = new HashMap<>();


    public static void main(String[] args) {
        try {
            // the default base should never be exposed in the output because all URLs should be relative
            URL defaultBase = new URL("http://www.co-ode.org/ontologies/owldoc/");

            OWLHTMLKit kit = new OWLHTMLKitImpl("kit", defaultBase);

            String out = null;

            for (String arg : args){
                String[] argPair = arg.split("=");
                if (argPair.length == 1){
                    if (argPair[0].equals("-t")){
                        logger.info("Switching mini hierarchies on");
                        kit.getHTMLProperties().setBoolean(OWLHTMLProperty.optionShowMiniHierarchies, true);
                    }
                    else if (argPair[0].equals("-l")){
                        logger.info("Rendering labels");
                        kit.getOWLServer().getProperties().set(ServerProperty.optionRenderer, ServerConstants.RENDERER_LABEL);
                    }
                    else if (argPair[0].equals("-c")){
                        logger.info("Switching ontology summary cloud on");
                        kit.getHTMLProperties().setBoolean(OWLHTMLProperty.optionRenderOntologySummaryCloud, true);
                    }
                    else if (argPair[0].equals("-v")){
                        logger.info("Verbose");
                    }
                    else{
                        // must be an ontology file
                        URI ontLoc = new File(argPair[0]).toURI();
                        logger.info("Loading ontology: " + ontLoc);
                        kit.getOWLServer().loadOntology(ontLoc);
                    }
                }
                else{
                    if (argPair[0].equals("out")){
                        out = argPair[1];
                    }
                }
            }

            if (out == null){
                out = "owldoc";
            }
            File outputDirectory = new File(out);
            if (!outputDirectory.exists()){
                outputDirectory.mkdir();
            }

            if (!outputDirectory.isDirectory()){
                logger.error("The output location specified is not a directory");
                System.exit(1);
            }
            if (kit.getOWLServer().getOntologies().isEmpty()){
                logger.error("No ontologies loaded!");
                System.exit(1);
            }

            OntologyExporter exporter = new OntologyExporter(kit);

            logger.info("Compiling owldoc into directory: " + outputDirectory);

            exporter.export(outputDirectory);

            logger.info("Done!");

            kit.dispose();
        }
        catch (Exception e) {
            logger.error("Problem exporting OWLDoc", e);
        }

        System.exit(0);
    }


    public OntologyExporter(OWLHTMLKit kit) {
        this.kit = kit;
    }

    public File export(File rootFile) throws Exception {
        String previousTarget = kit.getHTMLProperties().get(OWLHTMLProperty.optionContentWindow);
        String previousSite = kit.getHTMLProperties().get(OWLHTMLProperty.optionStaticSite);
        kit.getHTMLProperties().set(OWLHTMLProperty.optionContentWindow, null);
        kit.getHTMLProperties().setBoolean(OWLHTMLProperty.optionStaticSite, true);
        try {
            return exportSite(rootFile);
        }
        finally {
            kit.getHTMLProperties().set(OWLHTMLProperty.optionContentWindow, previousTarget);
            kit.getHTMLProperties().set(OWLHTMLProperty.optionStaticSite, previousSite);
        }
    }

    private File exportSite(File rootFile) throws Exception {
        completedEntities = 0;
        totalEntities = 0;
        reportProgress("Preparing export");
        Set<OWLOntology> visibleOntologies = new HashSet<>(kit.getVisibleOntologies());
        Set<OWLEntity> entities = new HashSet<>();
        for (OWLOntology ontology : visibleOntologies) {
            entities.addAll(ontology.getSignature());
        }
        OWLDataFactory dataFactory = kit.getOWLServer().getOWLOntologyManager().getOWLDataFactory();
        if (!visibleOntologies.isEmpty()) {
            entities.add(dataFactory.getOWLThing());
            entities.add(dataFactory.getOWLNothing());
        }
        totalEntities = entities.size();
        entities.clear();
        reportProgress("Preparing output files");
        if(!rootFile.exists()) {
            boolean done = rootFile.mkdirs();
            if(!done) {
                throw new Exception("Root directory could not be created: " + rootFile);
            }
        }

        assert rootFile.exists();

        if (rootFile.isDirectory()){
            this.root = rootFile;
            this.siteIndex = new StaticSiteIndex(kit);
            typeIndices.clear();
            exportedEntities.clear();

            copyResource(kit.getHTMLProperties().get(OWLHTMLProperty.optionDefaultCSS), rootFile);
            copyResource(OWLHTMLConstants.JS_TREE, rootFile);
            copyResource("js/owldoc.js", rootFile);

            // initialise the all resources index
            indexAllResourcesRenderer = new OWLObjectIndexDoclet<>(kit);
            indexAllResourcesRenderer.setTitle(NamedObjectType.entities.getPluralRendering());

            // TODO: the all resources renderer is no longer a full HTML page

            // step through the ontologies
            for (OWLOntology ont : visibleOntologies){
                reportProgress("Ontology: " + kit.getOWLServer().getOntologyShortFormProvider().getShortForm(ont));
                exportOntology(ont);
            }

            reportProgress("Writing category indexes");
            createIndices();

            reportProgress("Writing resource index");
            createAllResourcesIndex();

            reportProgress("Writing contents");
            createContents();

            reportProgress("Writing home page and search index");
            File home = createHomePage();
            siteIndex.write(rootFile);
            reportProgress("Complete");
            return home;
        }
        else{
            throw new Exception("Root exists but is not a directory: " + rootFile);
        }
    }


    private void copyResource(String resourceName, File rootFile) throws IOException {
        logger.debug("copying... " + resourceName);
        InputStream source = kit.getClass().getClassLoader().getResourceAsStream(resourceName);
        File dest = new File(rootFile, resourceName);
        FileUtils.copyInputStreamToFile(source, dest);
    }


    private void exportOntology(OWLOntology ont) throws IOException {
        siteIndex.add(ont);
        // export the ontology summary
        OWLDocPage<OWLOntology> ontologySummary = new OWLDocPage<>(kit);
        ontologySummary.addDoclet(new OWLOntologySummaryDoclet(kit));
        logger.debug("exporting ontology: " + ont);
        ontologySummary.setUserObject(ont);

        URL pageURL = kit.getURLScheme().getURLForOWLObject(ont);
        String localFilename = URLUtils.createRelativeURL(kit.getBaseURL(), pageURL);
        File ontologySummaryFile = new File(root, java.net.URI.create(localFilename).getPath());
        ensureExists(ontologySummaryFile);

        PrintWriter writer = createPrintWriter(ontologySummaryFile);
        ontologySummary.renderAll(pageURL, writer);

        writer.flush();
        writer.close();

        OWLObjectIndexDoclet<OWLOntology> ontologyIndexRenderer = getTypeIndexRenderer(NamedObjectType.ontologies);
        ontologyIndexRenderer.add(ont);

        exportReferencedEntities(ont);
    }

    private void exportReferencedEntities(OWLOntology ont) throws IOException {

        SummaryPageFactory fac = new SummaryPageFactory(kit, new UsagePagination(kit, root, this::reportProgress));


        final OWLDocPage<OWLClass> clsSummary = fac.getSummaryPage(OWLClass.class);
        final OWLDocPage<OWLObjectProperty> objPropSummary = fac.getSummaryPage(OWLObjectProperty.class);
        final OWLDocPage<OWLDataProperty> dataPropSummary = fac.getSummaryPage(OWLDataProperty.class);
        final OWLDocPage<OWLAnnotationProperty> annotationPropSummary = fac.getSummaryPage(OWLAnnotationProperty.class);
        final OWLDocPage<OWLNamedIndividual> indSummary = fac.getSummaryPage(OWLNamedIndividual.class);
        final OWLDocPage<OWLDatatype> datatypeSummary = fac.getSummaryPage(OWLDatatype.class);

        // export classes
        exportReferencedEntities(NamedObjectType.classes,
                                 clsSummary,
                                 getClassesInSignature(ont),
                                 ont);

        // export object properties
        exportReferencedEntities(NamedObjectType.objectproperties,
                                 objPropSummary,
                                 ont.getObjectPropertiesInSignature(),
                                 ont);

        // export data properties
        exportReferencedEntities(NamedObjectType.dataproperties,
                                 dataPropSummary,
                                 ont.getDataPropertiesInSignature(),
                                 ont);

        // export annotation properties
        exportReferencedEntities(NamedObjectType.annotationproperties,
                                 annotationPropSummary,
                                 ont.getAnnotationPropertiesInSignature(),
                                 ont);

        // export individuals
        exportReferencedEntities(NamedObjectType.individuals,
                                 indSummary,
                                 ont.getIndividualsInSignature(),
                                 ont);

        // export datatypes
        exportReferencedEntities(NamedObjectType.datatypes,
                                 datatypeSummary,
                                 ont.getDatatypesInSignature(),
                                 ont);
    }

    private <O extends OWLEntity> void exportReferencedEntities(NamedObjectType type, OWLDocPage<O> ren,
                                          Set<O>entities, OWLOntology ont) throws IOException {


//            File subDirFile = new File(root, type.toString());
//            if (!subDirFile.exists()){
//                subDirFile.mkdir();
//            }

            // index generation for current ontology
            final URLScheme urlScheme = kit.getURLScheme();

            URL indexBaseURL = urlScheme.getURLForOntologyIndex(ont, type);
            String localFilename = URLUtils.createRelativeURL(kit.getBaseURL(), indexBaseURL);
            File ontologyEntityIndexFile = new File(root, java.net.URI.create(localFilename).getPath());
            ensureExists(ontologyEntityIndexFile);
            PrintWriter indexWriter = createPrintWriter(ontologyEntityIndexFile);
            OWLObjectIndexDoclet<O> ontIndexRenderer = new OWLObjectIndexDoclet<>(kit);
            ontIndexRenderer.setTitle(kit.getOWLServer().getOntologyShortFormProvider().getShortForm(ont) + ": " + type);

            // index generation for all ontologies
            OWLObjectIndexDoclet<O> indexAllRenderer = getTypeIndexRenderer(type);

            for (O entity : entities){

                logger.debug("Rendering " + type.getSingularRendering() + ": " + entity);

                // add to ontology index
                ontIndexRenderer.add(entity);

                if (exportEntity(entity, ren)){

                    // add to all index
                    indexAllRenderer.add(entity);

                    // add to all resources index
                    indexAllResourcesRenderer.add(entity);
                }
            }

            logger.debug("Rendering index: " + type);

            OWLDocPage<O> page = new OWLDocPage<>(kit);
            page.addDoclet(ontIndexRenderer);
            page.renderAll(indexBaseURL, indexWriter);
            indexWriter.flush();
            indexWriter.close();

    }

    private <O extends OWLEntity> boolean exportEntity(O entity, OWLDocPage<O> ren) throws IOException {
        siteIndex.add(entity);

        URL entityURL = kit.getURLScheme().getURLForOWLObject(entity);
        String localFilename = URLUtils.createRelativeURL(kit.getBaseURL(), entityURL);
        File entitySummaryFile = new File(root, java.net.URI.create(localFilename).getPath());

        if (!exportedEntities.add(entity)){ // already generated during this export
            return false;
        }

        reportProgress(NamedObjectType.getType(entity).getSingularRendering() + ": "
                + kit.getOWLServer().getShortFormProvider().getShortForm(entity));
        ensureExists(entitySummaryFile);
        try (PrintWriter writer = createPrintWriter(entitySummaryFile)) {
            ren.setUserObject(entity);
            ren.renderAll(entityURL, writer);
            if (writer.checkError()) throw new IOException("Could not write " + entitySummaryFile);
        }
        completedEntities++;
        reportProgress("Rendered " + kit.getOWLServer().getShortFormProvider().getShortForm(entity));
        return true;
    }

    private static void ensureExists(File f) throws IOException {
        if (!f.getParentFile().exists()){
            f.getParentFile().mkdirs();
        }
        if (!f.exists()){
            f.createNewFile();
        }
    }

    private <O extends OWLObject> OWLObjectIndexDoclet<O> getTypeIndexRenderer(NamedObjectType type) {
        OWLObjectIndexDoclet<O> indexAllRenderer = (OWLObjectIndexDoclet<O>) typeIndices.get(type);
        if (indexAllRenderer == null){
            indexAllRenderer = new OWLObjectIndexDoclet<>(kit);
            indexAllRenderer.setTitle("All " +
                                      type.toString().substring(0, 1).toUpperCase() +
                                      type.toString().substring(1).toLowerCase());
            typeIndices.put(type, (OWLObjectIndexDoclet<OWLObject>) indexAllRenderer);
        }
        return indexAllRenderer;
    }

    private void createIndices() throws IOException {
        for (NamedObjectType type : typeIndices.keySet()){
            PrintWriter writer = createPrintWriter(new File(root, type + "/" + OWLHTMLConstants.INDEX_HTML));
            URL pageURL = kit.getURLScheme().getURLForIndex(type);
            new IndexPage(typeIndices.get(type)).renderAll(pageURL, writer);
            writer.flush();
            writer.close();
        }
    }

    private class IndexPage extends OWLDocPage<OWLObject> {
        IndexPage(OWLObjectIndexDoclet<OWLObject> index) {
            super(OntologyExporter.this.kit);
            addDoclet(index);
        }

        @Override
        protected String getTitle() {
            return "OWLDoc index";
        }
    }

    private void createAllResourcesIndex() throws IOException {
        // by this point we've accumulated the resources into the renderer
        String indexFile = kit.getHTMLProperties().get(OWLHTMLProperty.optionIndexAllURL);
        PrintWriter indexAllWriter = createPrintWriter(new File(root, indexFile));
        URL pageURL = kit.getURLScheme().getURLForRelativePage(indexFile);

        OWLDocPage<OWLObject> page = new OWLDocPage<>(kit);
        page.addDoclet(indexAllResourcesRenderer);
        page.renderAll(pageURL, indexAllWriter);
        indexAllWriter.flush();
        indexAllWriter.close();
    }

    private void createContents() throws IOException {
        OWLContentsHTMLPage contentsRenderer = new OWLContentsHTMLPage(kit);
        PrintWriter contentsWriter = createPrintWriter(new File(root, OWLHTMLConstants.CONTENTS_HTML));
        URL pageURL = kit.getURLScheme().getURLForRelativePage(OWLHTMLConstants.CONTENTS_HTML);
        contentsRenderer.renderAll(pageURL, contentsWriter);
        contentsWriter.flush();
        contentsWriter.close();
    }

    private File createHomePage() throws IOException {
        File home = new File(root, OWLHTMLConstants.INDEX_HTML);
        OWLDocPage<OWLOntology> page = new OWLDocPage<>(kit);
        page.addDoclet(new OWLOntologySummaryDoclet(kit));
        page.setUserObject(kit.getOWLServer().getActiveOntology());
        try (PrintWriter writer = createPrintWriter(home)) {
            page.renderAll(kit.getURLScheme().getURLForRelativePage(OWLHTMLConstants.INDEX_HTML), writer);
        }
        return home;
    }

    // get all classes (including owl:Thing)
    private Set<OWLClass> getClassesInSignature(OWLOntology ont) {
        Set<OWLClass> referencedClasses = new HashSet<>();
        referencedClasses.addAll(ont.getClassesInSignature());
        final OWLDataFactory df = kit.getOWLServer().getOWLOntologyManager().getOWLDataFactory();
        referencedClasses.add(df.getOWLThing());
        referencedClasses.add(df.getOWLNothing());
        return referencedClasses;
    }

    private static PrintWriter createPrintWriter(File file) throws IOException {
        return new PrintWriter(new java.io.OutputStreamWriter(FileUtils.openOutputStream(file), java.nio.charset.StandardCharsets.UTF_8));
    }
}
