/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.owl.mngr.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.coode.owl.mngr.ServerProperties;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 22, 2008<br><br>
 */
public class ServerPropertiesImpl implements ServerProperties {

    private Properties properties = new Properties();

    private List<PropertyChangeListener> listeners = new ArrayList<>();

    private Map<String, List<String>> allowedValues = new HashMap<>();

    private Map<String, String> deprecatedNamesMap = new HashMap<>();


    @Override
    public String get(String key) {
        return properties.getProperty(key);
    }

    @Override
    public URL getURL(String key) throws MalformedURLException {
        return new URL(properties.getProperty(key));
    }


    @Override
    public boolean set(String key, String value) {
        String currentValue = properties.getProperty(key);
        if (currentValue == null){
            if (value != null){
                properties.setProperty(key, value);
                notifyPropertyChanged(key, currentValue, value);
                return true;
            }
        }
        else {
            if (value == null){
                properties.remove(key);
                notifyPropertyChanged(key, currentValue, value);
                return true;
            }
            else if (!currentValue.equals(value)){
                properties.setProperty(key, value);
                notifyPropertyChanged(key, currentValue, value);
                return true;
            }
        }
        return false;
    }


    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        Enumeration<?> it = properties.propertyNames();
        while(it.hasMoreElements()){
            keys.add((String)it.nextElement());
        }
        return keys;
    }


    @Override
    public void remove(String key) {
        String oldValue = properties.getProperty(key);
        if (oldValue != null){
            properties.remove(key);
            notifyPropertyChanged(key, oldValue, null);
        }
    }


    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }


    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }


    @Override
    public void save(OutputStream out) throws IOException {
        properties.store(out, null);
    }


    @Override
    public void load(InputStream in) throws IOException {
        properties.load(in);

        cleanupDeprecatedNames();
    }

    @Override
    public void addDeprecatedNames(Map<String, String> names){
        this.deprecatedNamesMap.putAll(names);
        cleanupDeprecatedNames();
    }


    private void cleanupDeprecatedNames() {
        // to cope with old values
        for (String deprecatedName : deprecatedNamesMap.keySet()){
            if (properties.containsKey(deprecatedName)){
                properties.setProperty(deprecatedNamesMap.get(deprecatedName),
                                       properties.getProperty(deprecatedName));
                properties.remove(deprecatedName);
            }
        }
    }


    @Override
    public boolean isSet(String booleanOption) {
        return Boolean.parseBoolean(properties.getProperty(booleanOption));
    }


    @Override
    public void setAllowedValues(String key, List<String> values) {
        allowedValues.put(key, Collections.unmodifiableList(values));
    }


    @Override
    public List<String> getAllowedValues(String key) {
        List<String> result = allowedValues.get(key);
        return result != null ? new ArrayList<>(result) : new ArrayList<String>();
    }


    private void notifyPropertyChanged(String key, String oldValue, String newValue) {
        for (PropertyChangeListener l : listeners){
            l.propertyChange(new PropertyChangeEvent(this, key, oldValue, newValue));
        }
    }
}
