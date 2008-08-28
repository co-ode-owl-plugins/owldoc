package org.coode.browser.protege;

import org.coode.owl.mngr.NamedObjectType;
import org.coode.owl.mngr.OWLNameMapper;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.model.OWLNamedObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Set;

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
 * Date: Jun 29, 2007<br><br>
 * <p/>
 */
public class ProtegeNameMapperWrapper implements OWLNameMapper {

    private OWLModelManager mngr;


    public ProtegeNameMapperWrapper(OWLModelManager mngr) {
        this.mngr = mngr;
    }

//    public Set<OWLEntity> getOWLEntities(String name) {
//        Set<OWLEntity> entities = new HashSet<OWLEntity>();
//        entities.add(mngr.getOWLClass(name));
//        entities.add(mngr.getOWLObjectProperty(name));
//        entities.add(mngr.getOWLDataProperty(name));
//        entities.add(mngr.getOWLIndividual(name));
//        return entities;
//    }
//
//    public Set<OWLDataType> getOWLDatatypes(String string) {
//        return new HashSet<OWLDataType>(mngr.getMatchingOWLDataTypes(string));
//    }
//
//    public Set<OWLOntology> getOWLOntologies(String string) {
//        throw new NotImplementedException();
//    }
//
//    public Set<OWLNamedObject> getOWLNamedObjects(String string, String string1) {
//        throw new NotImplementedException();
//    }
//
//    public Set<OWLNamedObject> getOWLNamedObjects(String string, String string1, String string2, String string3) {
//        throw new NotImplementedException();
//    }
//
//    public Set<OWLNamedObject> getNamedObjects(String uri, String name, String baseURI, String type) {
//        String searchStr = uri;
//        if (searchStr == null){
//            searchStr = name;
//            if (searchStr != null && baseURI != null){
//                searchStr = baseURI + "#" + searchStr;
//            }
//        }
//
//        if (searchStr != null){
//            if (type.equals(NamedObjectType.classes)){
//                return new HashSet<OWLNamedObject>(getOWLClasses(searchStr));
//            }
//            else if (type.equals(NamedObjectType.objectproperties)){
//                return new HashSet<OWLNamedObject>(getOWLObjectProperties(searchStr));
//            }
//            else if (type.equals(NamedObjectType.dataproperties)){
//                return new HashSet<OWLNamedObject>(getOWLDataProperties(searchStr));
//            }
//            else if (type.equals(NamedObjectType.individuals)){
//                return new HashSet<OWLNamedObject>(getOWLIndividuals(searchStr));
//            }
//            else{
//                return new HashSet<OWLNamedObject>(getOWLEntities(searchStr));
//            }
//        }
//        return Collections.EMPTY_SET;
//    }
//
//    public Set<OWLClass> getOWLClasses(String name) {
//        return Collections.singleton(mngr.getOWLClass(name));
//    }
//
//    public Set<OWLObjectProperty> getOWLObjectProperties(String name) {
//        return Collections.singleton(mngr.getOWLObjectProperty(name));
//    }
//
//    public Set<OWLDataProperty> getOWLDataProperties(String name) {
//        return Collections.singleton(mngr.getOWLDataProperty(name));
//    }
//
//    public Set<OWLProperty> getOWLProperties(String name) {
//        OWLProperty prop = mngr.getOWLObjectProperty(name);
//        if (prop == null){
//            prop = mngr.getOWLDataProperty(name);
//        }
//        return Collections.singleton(prop);
//    }
//
//    public Set<OWLIndividual> getOWLIndividuals(String name) {
//        return Collections.singleton(mngr.getOWLIndividual(name));
//    }

    public Set<String> getClassNames() {
        throw new NotImplementedException();
    }

    public Set<String> getObjectPropertyNames() {
        throw new NotImplementedException();
    }

    public Set<String> getDataPropertyNames() {
        throw new NotImplementedException();
    }

    public Set<String> getIndividualNames() {
        throw new NotImplementedException();
    }

    public Set<String> getDatatypeNames() {
        throw new NotImplementedException();
    }

    public Set<String> getOntologyNames() {
        throw new NotImplementedException();
    }

    public Set<String> getEntityNames() {
        throw new NotImplementedException();
    }


    public Set<String> getNames(NamedObjectType type) {
        throw new NotImplementedException();
    }


    public <T extends OWLNamedObject> void get(String string, Set<T> set, NamedObjectType namedObjectType) {
        throw new NotImplementedException();
    }


    public void dispose() {
        //@@TODO implement
    }
}
