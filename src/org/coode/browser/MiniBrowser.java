package org.coode.browser;

import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.util.NativeBrowserLauncher;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Nick Drummond<br>
 *
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 7, 2007<br><br>
 * <p/>
 */
public class MiniBrowser extends JComponent{

//    private static final String SPECIAL_LINK = "protege-open-in-browser";

    private JToolBar toolbar;
    private JTextField addressField;
    private JEditorPane docPanel;
    private HTMLEditorKit eKit = new HTMLEditorKit();

    private Stack<URL> history = new Stack<URL>();

//    private List<BrowserPageListener> pageListeners = new ArrayList<BrowserPageListener>();

    private HyperlinkListener linkListener;

    private KeyListener addressListener;

    private Action backAction;

    private Action launchAction;

    private boolean navigateActive = true;

    // used to load pages in background
    private Runnable currentLoader;
    private Runnable waitingLoader;

    public MiniBrowser() {
        super();

        setLayout(new BorderLayout(6, 6));

        docPanel = new JEditorPane();
        docPanel.setEditable(false);
        docPanel.setBackground(Color.WHITE);

        linkListener = new HyperlinkListener(){
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (navigateActive){
                        setURL(event.getURL());
                    }
                }
//                else if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
//                    Element linkElement = event.getSourceElement();
//                    docPanel.setSelectionColor(Color.RED);
//                    docPanel.setSelectionStart(linkElement.getStartOffset());
//                    docPanel.setSelectionEnd(linkElement.getEndOffset());
//                }
            }
        };

        docPanel.addHyperlinkListener(linkListener);

        add(new JScrollPane(docPanel), BorderLayout.CENTER);
    }

    public void showToolBar(boolean show){
        if (show){
            if (toolbar == null){
                setupToolbar();
            }
            else{
                add(toolbar, BorderLayout.NORTH);
            }
        }
        else{
            if (toolbar != null){
                remove(toolbar);
            }
        }
    }


    public void clearHistory(){
        history.clear();
    }

    public void addLinkListener(HyperlinkListener l){
        docPanel.addHyperlinkListener(l);
    }

    public void removeLinkListener(HyperlinkListener l){
        docPanel.removeHyperlinkListener(l);
    }

    public boolean setURL(URL url){
        if (url != null){
            if (!pageAlreadyLoaded(url)){
                history.push(url);
                if (addressField != null){
                    addressField.setText(url.toString());
                }

                // load the pages in another thread so they don't interfere
                waitingLoader = new Runnable(){
                    public void run() {
                        loadPage();

                        currentLoader = waitingLoader;
                        if (currentLoader != null){
                            waitingLoader = null;
                            new Thread(currentLoader).start();
                        }
                    }
                };

                if (currentLoader == null){
                    currentLoader = waitingLoader;
                    waitingLoader = null;
                    new Thread(currentLoader).start();
                }
            }
        }
        else{
            docPanel.setText("");
        }
        return true;
    }

    private boolean pageAlreadyLoaded(URL url) {
        if (history.isEmpty()){
            return false;
        }
        else{
            return url.equals(getURL());
        }
    }

    private void loadPage() {
        final URL loadURL = getURL();
        System.out.print("loading page: " + loadURL);
        try {
            docPanel.setPage(loadURL);
            System.out.print("... DONE!");
        }
        catch (IOException e) {
            docPanel.setContentType("text/html");
            docPanel.setText("<html><body>" +
                             "Sorry, we cannot load page from:<br>" +
                             "<a name='protege-open-in-browser' href='" + loadURL + "'>" + loadURL + "</a>" +
                             "<br>You may be able to use you browser instead</body></html>");
        }
        finally{
            if (backAction!=null){
                backAction.setEnabled(history.size() > 1);
                launchAction.setEnabled(history.size() >= 1);
            }
            System.out.println("");
        }
    }

    public void setContent(Reader r, URL baseURL) throws IOException, BadLocationException {
        HTMLDocument htmlDoc = (HTMLDocument)eKit.createDefaultDocument();
        // see http://www.velocityreviews.com/forums/t132727-htmleditorkit-is-throwing-exception.html
        htmlDoc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        htmlDoc.setBase(baseURL);
        docPanel.setContentType("text/html");
        eKit.read(r, htmlDoc, 0);
        docPanel.setDocument(htmlDoc);
    }

    public void setCSS(Reader r) throws IOException {
        StyleSheet ss = new StyleSheet();
        ss.loadRules(r, null);
        StyleSheet defaultSS = eKit.getStyleSheet();
        ss.addStyleSheet(defaultSS);
        eKit.setStyleSheet(ss);
    }

    public void setNavigateActive(boolean active){
        this.navigateActive = active;
    }

    public URL getURL(){
        return history.peek();
    }

//    public void addWebBrowserListener(BrowserPageListener l){
//        pageListeners.add(l);
//    }
//
//    public void removeWebBrowserListener(BrowserPageListener l){
//        pageListeners.remove(l);
//    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (linkListener != null){
            docPanel.removeHyperlinkListener(linkListener);
        }
        if (addressListener != null){
            addressField.removeKeyListener(addressListener);
        }
    }

    private void setupToolbar() {
        addressListener = new KeyAdapter(){
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
                    String address = addressField.getText();
                    try {
                        if (address.indexOf(":/") == -1){
                            address = "http://" + address;
                            addressField.setText(address);
                        }
                        setURL(new URL(address));
                    }
                    catch (MalformedURLException e) {
                    }
                    keyEvent.consume();
                }
            }
        };

        addressField = new JTextField();
        addressField.addKeyListener(addressListener);

        toolbar = new JToolBar();
        toolbar.setFloatable(false);

        backAction = new AbstractAction("Back", Icons.getIcon("back.gif")){
            public void actionPerformed(ActionEvent actionEvent) {
                if (history.size() > 1){
                    history.pop();
                    setURL(history.pop());
                }
            }
        };
        toolbar.add(new JButton(backAction));

        launchAction = new AbstractAction("Open in browser", Icons.getIcon("object.find.gif")) {
            public void actionPerformed(ActionEvent actionEvent) {
                NativeBrowserLauncher.openURL(history.peek().toString());
            }
        };

        toolbar.add(new JButton(launchAction));
        toolbar.add(addressField);

        backAction.setEnabled(false);
        launchAction.setEnabled(false);

        add(toolbar, BorderLayout.NORTH);
    }

    public static void main(String[] args) {

        MiniBrowser testBrowser = new MiniBrowser();
        testBrowser.showToolBar(true);
        try {
            testBrowser.setURL(new URL("file:/Users/drummond/temp/owldoc/food.html"));
        }
        catch (MalformedURLException e) {
        }
        testBrowser.setPreferredSize(new Dimension(400, 400));
        JOptionPane.showMessageDialog(null, testBrowser);
    }

    public void clear() {
        docPanel.setText("");
    }
}
