// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.app.ui.util.converter;

import javafx.util.StringConverter;

import com.hp.hpl.jena.ontology.*;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Class handling the displayed text when a Jena Resource item in a
 * ComboBox has been selected
 */
public class PropertyOntClassStringConverter extends StringConverter<Pair<ObjectProperty, OntClass>> {

    public PropertyOntClassStringConverter() {}

    @Override
    public String toString(Pair<ObjectProperty, OntClass> pcPair) {
        return pcPair.getValue().getLocalName() +" (link by "+pcPair.getKey().getLocalName() +")";
    }

    @Override
    public Pair<ObjectProperty, OntClass> fromString(String str) {
        return null;
    }

}
