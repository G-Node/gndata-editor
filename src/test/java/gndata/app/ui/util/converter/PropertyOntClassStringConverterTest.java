// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.app.ui.util.converter;

import static org.assertj.core.api.Assertions.assertThat;

import gndata.lib.util.FakeRDFModel;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.*;

import com.hp.hpl.jena.ontology.*;

/**
 * Basic tests for the PropertyOntClassStringConverter class.
 */
public class PropertyOntClassStringConverterTest {

    private PropertyOntClassStringConverter conv;
    private OntModel ontology;

    private final static String foaf = "http://xmlns.com/foaf/0.1/";

    @Before
    public void setUp() {
        conv = new PropertyOntClassStringConverter();
        ontology = FakeRDFModel.getFakeSchema();
    }

    @Test
    public void toStringTest() {
        ObjectProperty op = ontology.getObjectProperty(foaf +"knows");
        OntClass oc = ontology.getOntClass(foaf + "Person");
        Pair<ObjectProperty, OntClass> pcPair = Pair.of(op, oc);

        assertThat(conv.toString(pcPair))
                .isEqualTo(pcPair.getValue().getLocalName() + " (link by " + pcPair.getKey().getLocalName() + ")");
    }

    @Test
    public void fromStringTest() {
        assertThat(conv.fromString("test")).isNull();
    }

}
