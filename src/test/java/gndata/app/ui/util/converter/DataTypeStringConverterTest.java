// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.app.ui.util.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import org.junit.*;

/**
 * Basic tests for the DataTypeStringConverter class.
 */
public class DataTypeStringConverterTest {

    private DataTypeStringConverter conv;
    private RDFDatatype dt;

    @Before
    public void setUp() {
        conv = new DataTypeStringConverter();
        dt = XSDDatatype.XSDstring;
    }

    @Test
    public void toStringTest() {
        assertThat(conv.toString(dt)).isEqualTo(dt.getJavaClass().getSimpleName());
    }

    @Test
    public void fromStringTest() {
        assertThat(conv.fromString("test")).isNull();
    }

}
