// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.app.ui.util.converter;

import javafx.util.StringConverter;

import com.hp.hpl.jena.datatypes.RDFDatatype;

/**
 * Class handling the displayed text of the available RDF DataTypes
 * when an item from the add new DataProperty combo box has been selected
 */
final public class DataTypeStringConverter extends StringConverter<RDFDatatype> {

    public DataTypeStringConverter() {}

    @Override
    public String toString(RDFDatatype type) {
        return type.getJavaClass().getSimpleName();
    }

    @Override
    public RDFDatatype fromString(String str) {
        return null;
    }

}
