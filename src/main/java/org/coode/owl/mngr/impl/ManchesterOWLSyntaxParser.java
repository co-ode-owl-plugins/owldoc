/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.owl.mngr.impl;

import org.coode.owl.mngr.OWLClassExpressionParser;
import org.coode.owl.mngr.OWLServer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.text.ParseException;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 11, 2008<br><br>
 */
public class ManchesterOWLSyntaxParser implements OWLClassExpressionParser {

    private OWLServer server;

    
    public ManchesterOWLSyntaxParser(OWLServer server) {
        this.server = server;
    }


    @Override
    public OWLClassExpression parse(String str) throws ParseException {
        try {
            return getParser(str).parseClassExpression();
        }
        catch (ParserException e) {
            throw new ParseException(e.getMessage(), e.getStartPos());
        }
    }


    private org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser getParser(String expression){
        OWLEntityChecker checker = server.getOWLEntityChecker();

        org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();

        parser.setDefaultOntology(server.getActiveOntology());
        parser.setOWLEntityChecker(checker);

        return parser;
    }
}