/* Offline documentation navigation: ordinary links, local index, no server. */
(function () {
    "use strict";
    function start() {
        var page = document.getElementById("owldoc-page");
        var data = window.OWLDOC_DATA;
        if (!page || !data) return;
        var root = new URL(page.dataset.root, window.location.href);
        var here = new URL(window.location.href);
        var clean = function (url) { var value = new URL(url); value.search = ""; value.hash = ""; return value.href; };
        var current = data.entities.find(function (entry) { return clean(new URL(entry.path, root)) === clean(here); });
        if (!current && page.dataset.entityPath) {
            current = data.entities.find(function (entry) { return entry.path === page.dataset.entityPath; });
        }
        if (!current && clean(here) === clean(new URL("index.html", root))) {
            current = data.entities.find(function (entry) { return entry.path === data.home; });
        }
        var types = data.types;
        var counts = {};
        data.entities.forEach(function (entry) { counts[entry.type] = (counts[entry.type] || 0) + 1; });
        var params = new URLSearchParams(here.search);
        var category = params.get("browse") || (current && current.type !== "ontologies" ? current.type : "all");
        if (category !== "all" && !types.some(function (type) { return type.id === category; })) category = "all";
        var query = params.get("q") || "";
        var limit = 200;
        var storageKey = "owldoc:" + root.href;
        function readState() { try { return JSON.parse(sessionStorage.getItem(storageKey)) || {}; } catch (ignored) { return {}; } }
        function saveState() {
            try { sessionStorage.setItem(storageKey, JSON.stringify({category:category, query:query, limit:limit, scroll:list.scrollTop, width:sidebarWidth})); } catch (ignored) { /* Storage can be disabled for local files. */ }
        }
        var saved = readState();
        var sidebarWidth = Number(saved.width) || 288;
        if (saved.category === category && saved.query === query) limit = Number(saved.limit) || 200;
        var singular = {ontologies:"Ontology",classes:"Class",objectproperties:"Object property",dataproperties:"Data property",annotationproperties:"Annotation property",individuals:"Individual",datatypes:"Datatype"};
        function element(tag, name, text) {
            var node = document.createElement(tag);
            if (name) node.className = name;
            if (text != null) node.textContent = text;
            return node;
        }
        function icon(type) {
            // Protégé entity shapes: circles, property bars, and individual diamonds.
            var paths = {
                ontologies: ['M12 8v3m-6 5v-5h12v5','M9 2h6v6H9z','M3 16h6v6H3z','M15 16h6v6h-6z'],
                classes: ['M21 12a9 9 0 1 0-18 0 9 9 0 1 0 18 0'],
                objectproperties: ['M2 6h20v12H2z'],
                dataproperties: ['M2 6h20v12H2z'],
                annotationproperties: ['M2 6h20v12H2z'],
                individuals: ['M12 2 22 12 12 22 2 12 12 2Z'],
                datatypes: ['M21 12a9 9 0 1 0-18 0 9 9 0 1 0 18 0']
            };
            var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
            svg.setAttribute("viewBox", "0 0 24 24"); svg.setAttribute("class", "resource-icon icon-" + type);
            svg.setAttribute("aria-hidden", "true"); svg.setAttribute("fill", "currentColor"); svg.setAttribute("fill-opacity", "0.12");
            svg.setAttribute("stroke", "currentColor"); svg.setAttribute("stroke-width", "1.7");
            svg.setAttribute("stroke-linecap", "round"); svg.setAttribute("stroke-linejoin", "round");
            (paths[type] || paths.ontologies).forEach(function (path) { var node = document.createElementNS(svg.namespaceURI, "path"); node.setAttribute("d", path); svg.append(node); });
            return svg;
        }
        function destination(entry) {
            var url = new URL(entry.path, root);
            url.searchParams.set("browse", category);
            if (query) url.searchParams.set("q", query);
            return url.href;
        }
        var header = element("header", "site-header");
        var toggle = element("button", "sidebar-toggle", "Browse");
        toggle.type = "button";
        toggle.setAttribute("aria-controls", "entity-sidebar");
        toggle.setAttribute("aria-expanded", "false");
        var brand = element("a", "site-brand", "OWLDoc");
        brand.href = new URL("index.html", root).href;
        var title = element("span", "site-title", data.title);
        header.append(toggle, brand, element("span", "header-divider", "/"), title);
        var total = element("span", "site-total", data.entities.length.toLocaleString() + " documented resources");
        header.append(total);
        var sidebar = element("aside", "site-sidebar");
        sidebar.id = "entity-sidebar";
        sidebar.setAttribute("aria-label", "Browse ontology resources");
        var browseLabel = element("label", "field-label", "Explore ontology");
        browseLabel.htmlFor = "entity-category";
        var select = element("select", "category-select");
        select.id = "entity-category";
        var all = element("option", "", "All resources (" + data.entities.length + ")");
        all.value = "all"; select.append(all);
        types.forEach(function (type) {
            if (!counts[type.id]) return;
            var option = element("option", "", type.label + " (" + counts[type.id] + ")");
            option.value = type.id; select.append(option);
        });
        select.value = category;
        var searchLabel = element("label", "sr-only", "Search by name or IRI");
        searchLabel.htmlFor = "entity-search";
        var searchBox = element("div", "entity-search-box");
        var search = element("input", "entity-search");
        search.id = "entity-search"; search.type = "search";
        search.placeholder = "Search names or IRIs…"; search.value = query;
        search.autocomplete = "off"; search.spellcheck = false;
        var shortcut = element("kbd", "search-shortcut", "/");
        searchBox.append(search, shortcut);
        var status = element("div", "results-status");
        status.setAttribute("role", "status"); status.setAttribute("aria-live", "polite");
        var list = element("nav", "entity-list");
        list.setAttribute("aria-label", "Documentation pages");
        var more = element("button", "show-more", "Show more results");
        more.type = "button";
        var controls = element("div", "sidebar-controls");
        controls.append(browseLabel, select, searchLabel, searchBox, status);
        sidebar.append(controls, list, more);
        var main = element("main", "site-main");
        main.id = "documentation"; main.tabIndex = -1;
        var skip = element("a", "skip-link", "Skip to documentation");
        skip.href = "#documentation";
        var docHeader = element("header", "document-header");
        var kind = current ? singular[current.type] : "Documentation index";
        if (page.dataset.section) kind += " · " + page.dataset.section;
        var name = current ? current.label : (types.find(function (type) { return here.pathname.indexOf("/" + type.id + "/") >= 0; }) || {label:"All resources"}).label;
        var kindLabel = element("div", "document-kind");
        if (current) kindLabel.append(icon(current.type));
        kindLabel.append(document.createTextNode(kind));
        docHeader.append(kindLabel, element("h1", "document-title", name));
        if (current && current.iri) {
            var iriRow = element("div", "iri-row");
            var iriText = element("code", "entity-iri", current.iri);
            var copy = element("button", "copy-iri", "Copy IRI"); copy.type = "button";
            copy.addEventListener("click", function () {
                function copied() { copy.textContent = "Copied"; window.setTimeout(function () { copy.textContent = "Copy IRI"; }, 1500); }
                function selectIRI() {
                    var range = document.createRange(); range.selectNodeContents(iriText);
                    var selection = window.getSelection(); selection.removeAllRanges(); selection.addRange(range);
                    copy.textContent = "Press ⌘C / Ctrl+C";
                }
                if (navigator.clipboard && navigator.clipboard.writeText) navigator.clipboard.writeText(current.iri).then(copied, selectIRI);
                else selectIRI();
            });
            iriRow.append(iriText, copy); docHeader.append(iriRow);
        }
        var firstTitle = page.querySelector("h2");
        if (firstTitle) firstTitle.classList.add("legacy-title");
        var article = element("article", "document-content");
        while (page.firstChild) article.append(page.firstChild);
        article.querySelectorAll(".annotation-image img").forEach(function (image) {
            function unavailable() { image.parentElement.hidden = true; image.parentElement.nextElementSibling.hidden = false; }
            image.addEventListener("error", unavailable);
            if (image.complete && image.naturalWidth === 0) unavailable();
        });
        main.append(docHeader, article);
        var layout = element("div", "site-layout"); var divider = element("div", "sidebar-divider");
        divider.tabIndex = 0; divider.setAttribute("role", "separator");
        divider.setAttribute("aria-label", "Resize navigation sidebar"); divider.setAttribute("aria-orientation", "vertical");
        divider.setAttribute("aria-controls", "entity-sidebar");
        function resize(width) {
            sidebarWidth = Math.round(Math.max(220, Math.min(width, Math.min(640, window.innerWidth - 320))));
            layout.style.setProperty("--sidebar-width", sidebarWidth + "px");
            divider.setAttribute("aria-valuemin", "220"); divider.setAttribute("aria-valuemax", String(Math.min(640, window.innerWidth - 320)));
            divider.setAttribute("aria-valuenow", String(sidebarWidth));
        }
        divider.addEventListener("pointerdown", function (event) { divider.focus(); divider.setPointerCapture(event.pointerId); document.body.classList.add("resizing-sidebar"); event.preventDefault(); });
        divider.addEventListener("pointermove", function (event) { if (divider.hasPointerCapture(event.pointerId)) resize(event.clientX - layout.getBoundingClientRect().left); });
        function endResize() { document.body.classList.remove("resizing-sidebar"); saveState(); }
        divider.addEventListener("pointerup", endResize); divider.addEventListener("pointercancel", endResize);
        divider.addEventListener("lostpointercapture", endResize);
        divider.addEventListener("keydown", function (event) {
            if (event.key === "ArrowLeft" || event.key === "ArrowRight") { event.preventDefault(); resize(sidebarWidth + (event.key === "ArrowLeft" ? -20 : 20)); saveState(); }
        });
        window.addEventListener("resize", function () { if (window.innerWidth > 720) resize(sidebarWidth); });
        layout.append(sidebar, divider, main); resize(sidebarWidth);
        page.append(skip, header, layout);
        document.body.classList.add("site-ready");
        document.title = name + (page.dataset.section ? " · " + page.dataset.section : "") + " · " + data.title + " · OWLDoc";
        function remember() {
            var url = new URL(window.location.href);
            url.searchParams.set("browse", category);
            if (query) url.searchParams.set("q", query); else url.searchParams.delete("q");
            try { window.history.replaceState(null, "", url.href); } catch (ignored) { /* Some local-file browsers restrict history updates. */ }
        }
        function render() {
            var needle = query.trim().toLocaleLowerCase();
            var entries = data.entities.filter(function (entry) {
                return (category === "all" || entry.type === category) && (!needle || (entry.label + " " + entry.iri).toLocaleLowerCase().indexOf(needle) >= 0);
            });
            list.replaceChildren();
            entries.slice(0, limit).forEach(function (entry) {
                var link = element("a", "entity-link");
                link.append(icon(entry.type));
                var label = element("span", "entity-label", entry.label); link.append(label);
                link.href = destination(entry); link.title = singular[entry.type] + ": " + (entry.iri || entry.label);
                link.setAttribute("aria-label", entry.label + ", " + singular[entry.type]);
                if (current && current.path === entry.path) { link.classList.add("is-current"); link.setAttribute("aria-current", "page"); }
                list.append(link);
            });
            if (!entries.length) list.append(element("p", "empty-results", "No matching resources. Try another name or IRI."));
            status.textContent = entries.length.toLocaleString() + (entries.length === 1 ? " resource" : " resources") + (entries.length > limit ? " · showing " + limit : "");
            more.hidden = entries.length <= limit;
            remember();
            article.querySelectorAll("a[href]").forEach(function (link) {
                var url = new URL(link.href);
                if (url.href.startsWith(root.href) && /\.html$/.test(url.pathname) && !url.searchParams.has("action")) {
                    url.searchParams.set("browse", category);
                    if (query) url.searchParams.set("q", query); else url.searchParams.delete("q");
                    link.href = url.href;
                }
            });
        }
        select.addEventListener("change", function () { category = select.value; limit = 200; render(); list.scrollTop = 0; saveState(); });
        search.addEventListener("input", function () { query = search.value; limit = 200; render(); list.scrollTop = 0; saveState(); });
        search.addEventListener("keydown", function (event) { if (event.key === "Escape") { search.value = ""; query = ""; render(); } });
        more.addEventListener("click", function () { var scroll = list.scrollTop; limit += 200; render(); list.scrollTop = scroll; saveState(); });
        toggle.addEventListener("click", function () {
            var opened = document.body.classList.toggle("sidebar-open"); toggle.setAttribute("aria-expanded", String(opened));
        });
        document.addEventListener("keydown", function (event) {
            if (event.key === "/" && !/^(INPUT|TEXTAREA|SELECT)$/.test(event.target.tagName) && !event.target.isContentEditable) { event.preventDefault(); search.focus(); }
        });
        render();
        if (saved.category === category && saved.query === query) list.scrollTop = Number(saved.scroll) || 0;
        list.addEventListener("scroll", saveState, {passive:true});
        window.addEventListener("pagehide", saveState);
        window.addEventListener("pageshow", function () {
            // Safari restores form values after DOMContentLoaded, including an empty search field.
            search.value = query; select.value = category;
        });
    }
    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", start);
    else start();
}());
