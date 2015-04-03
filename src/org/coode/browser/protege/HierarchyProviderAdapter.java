package org.coode.browser.protege;

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

import org.coode.owl.mngr.HierarchyProvider;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Aug 11, 2009<br><br>
 */
public class HierarchyProviderAdapter<E extends OWLEntity> implements HierarchyProvider<E> {

    OWLObjectHierarchyProvider<E> provider;


    protected HierarchyProviderAdapter(OWLObjectHierarchyProvider<E> provider) {
        this.provider = provider;
    }


    @Override
    public Set<E> getRoots() {
        return provider.getRoots();
    }


    public Set<E> getParents(E e) {
        return provider.getParents(e);
    }


    public Set<E> getChildren(E e) {
        return provider.getChildren(e);
    }


    public Set<E> getEquivalents(E e) {
        return provider.getEquivalents(e);
    }


    public Set<E> getDescendants(E e) {
        return provider.getDescendants(e);
    }


    public Set<E> getAncestors(E e) {
        return provider.getAncestors(e);
    }


    public void dispose() {
        this.provider = null;
    }

    @Override
    public Class<? extends E> getNodeClass() {
        return (Class<? extends E>) provider.getRoots().iterator().next()
                .getClass();
    }

    @Override
    public boolean hasAncestor(E node, E ancestor) {
        return provider.getAncestors(node).contains(ancestor);
    }

    @Override
    public boolean isRoot(E node) {
        return provider.getRoots().contains(node);
    }

    @Override
    public boolean isLeaf(E node) {
        return provider.getChildren(node).isEmpty();
    }
}
