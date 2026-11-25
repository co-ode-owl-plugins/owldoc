package org.coode.owl.util;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;

import java.util.Optional;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 5, 2010<br><br>
 *
 * The OWL API SimpleShortFormProvider does not work if there is no fragment
 */
public class MySimpleShortFormProvider implements ShortFormProvider {

    @Override
    public String getShortForm(OWLEntity owlEntity) {
        IRI iri = owlEntity.getIRI();
        Optional<String> name = iri.getRemainder();
        if (!name.isPresent()){
            String newName = iri.toString();
            if (newName.endsWith("/")){
                newName = newName.substring(0, newName.length()-1);
            }
            int index = newName.lastIndexOf("/");
            if (index >= 0){
                newName = newName.substring(index+1, newName.length());
            }
            return newName;
        }
        return name.get();
    }

    @Override
    public void dispose() {
        // do nothing
    }
}
