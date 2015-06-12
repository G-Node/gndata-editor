// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.app.ui.util;

import java.util.Set;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.scene.control.*;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import gndata.app.ui.util.converter.*;
import gndata.app.ui.util.filter.LiteralFilter;

/**
 * Class that implements rendering of table items with RDF literals.
 */
public final class DataPropertyTableItem {

    private final DatatypeProperty dataProperty;
    private final TextField editValue = new TextField();
    private final ComboBox<RDFDatatype> dataTypeBox = new ComboBox<>();

    public DataPropertyTableItem(Set<RDFDatatype> dts, DatatypeProperty property, RDFDatatype dt) {

        dataProperty = property;

        final ObservableList<RDFDatatype> dataTypeList = FXCollections.observableArrayList();
        dataTypeList.addAll(dts);
        dataTypeBox.setItems(dataTypeList);

        dataTypeBox.setConverter(new DataTypeStringConverter());
        dataTypeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RDFDatatype>() {
            @Override
            public void changed(ObservableValue<? extends RDFDatatype> observable, RDFDatatype oldValue, RDFDatatype newValue) {
                if (newValue != null) {
                    editValue.setTextFormatter(new TextFormatter(new LiteralConverter(newValue), null, new LiteralFilter(newValue)));
                    editValue.setPromptText(newValue.getJavaClass().getSimpleName() + " value");
                }
            }
        });
        dataTypeBox.getSelectionModel().select(dt);

    }


    // -------------------------------------
    // CellValueFactory getter and setter
    // -------------------------------------

    public DatatypeProperty getProperty() {
        return dataProperty;
    }

    public RDFDatatype getDataType() {
        return dataTypeBox.getSelectionModel().getSelectedItem();
    }

    public void setTextFieldValue(String value) {
        editValue.setText(value);
    }

    public String getTextFieldValue() {
        return editValue.getText();
    }

    public String getPredicate() {
        return dataProperty.getLocalName();
    }

    public TextField getTextField() {
        return editValue;
    }

    public ComboBox<RDFDatatype> getType() {
        return dataTypeBox;
    }

}
