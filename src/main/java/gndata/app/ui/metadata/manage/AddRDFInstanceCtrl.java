// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.app.ui.metadata.manage;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.*;
import javafx.beans.property.ObjectProperty;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDF;
import gndata.app.state.*;
import gndata.app.ui.util.*;
import gndata.lib.util.*;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Controller for the {@link AddRDFInstanceView}
 * Provides the logic for adding a new RDF class instance
 */
public class AddRDFInstanceCtrl extends DialogCtrl implements Initializable {

    private final ProjectState projectState;
    private final MetadataNavState navState;
    private final OntologyHelper oh;

    private final StringProperty labelText;

    private final Resource preSelNewClass;
    private final ObservableList<Pair<com.hp.hpl.jena.ontology.ObjectProperty, OntClass>> newClassesList;
    private final ObjectProperty<Pair<com.hp.hpl.jena.ontology.ObjectProperty, OntClass>> selectedNewClass;

    private final ObservableList<DataPropertyTableItem> newPredicatesList;

    private final ObservableList<DatatypeProperty> additionalPredicatesList;
    private final ObjectProperty<DatatypeProperty> selectedPredicate;

    private final ObjectProperty<RDFDatatype> addPredType;
    private final ObservableList<RDFDatatype> addPredicateType;
    private final ObjectProperty<RDFDatatype> selAddPredType;

    private final StringProperty addPredValue;
    private final StringProperty addPredPromptText;


    public AddRDFInstanceCtrl(ProjectState projState, MetadataNavState navigationState, Resource extRes){

        projectState = projState;
        navState = navigationState;
        oh = projectState.getMetadata().ontmanager;
        preSelNewClass = extRes;

        labelText = new SimpleStringProperty();

        newClassesList = FXCollections.observableArrayList();
        selectedNewClass = new SimpleObjectProperty<>();

        newPredicatesList = FXCollections.observableArrayList();

        additionalPredicatesList = FXCollections.observableArrayList();
        selectedPredicate = new SimpleObjectProperty<>();
        addPredValue = new SimpleStringProperty();
        addPredType = new SimpleObjectProperty<>();
        selAddPredType = new SimpleObjectProperty<>();
        addPredicateType = FXCollections.observableArrayList();
        addPredPromptText = new SimpleStringProperty();

        // Update the contents of the DataProperty table, if a new class has been selected
        selectedNewClass.addListener((observable, oldValue, newValue) -> {

            if(selectedNewClass.get().getValue() != null) {

                newPredicatesList.clear();

                oh.listDatatypeProperties(selectedNewClass.get().getValue()).stream()
                        .forEach(c -> {
                            Set<RDFDatatype> dts = oh.getRange(c.asDatatypeProperty());
                            newPredicatesList.add(new DataPropertyTableItem(dts, c.asDatatypeProperty(), dts.iterator().next()));
                        });

                // reset add new DataProperty TextField
                addPredValue.set("");
            }
        });

        // Listen on the selected new predicate and set the corresponding RDF DataType accordingly
        selectedPredicate.addListener((observable, oldValue, newValue) -> {
            addPredPromptText.set("");
            if (observable != null && newValue != null) {
                addPredicateType.clear();
                // Retrieve all available RDF DataTypes of the selected DataProperty
                addPredicateType.setAll(oh.getRange(observable.getValue()));
                // Select the first available DataType
                RDFDatatype dt = addPredicateType.iterator().next();
                addPredType.set(dt);
                addPredPromptText.set("Enter a " + (dt == null ? "String" : dt.getJavaClass().getSimpleName()) + " value");
            }
        });

        // Handle list of additional DataType properties
        newPredicatesList.addListener((ListChangeListener<DataPropertyTableItem>) change -> {
            // Reset list of additional DataType properties
            additionalPredicatesList.clear();
            // Reset list of RDF DataTypes
            addPredicateType.clear();

            // Get all available DataType properties for selected class
            oh.listDatatypeProperties(selectedNewClass.get().getValue()).stream()
                    .forEach(c -> additionalPredicatesList.add(c.asDatatypeProperty()));

            // TODO implement check for DataType Properties which can be added multiple times
            // Remove all DataType properties which already exist in the table
            newPredicatesList.stream().forEach(c -> {
                if (additionalPredicatesList.contains(c.getProperty())) {
                    additionalPredicatesList.remove(c.getProperty());
                }
            });
        });

        // Listen on changes of the selected DataType property in the
        // additional DataProperties line and change the value of addPredType
        // accordingly, since it is required to set the TextFilter of the
        // value TextField.
        selAddPredType.addListener((observable, oldValue, newValue) -> {
            if (observable != null && newValue != null) {
                addPredType.set(newValue);
                addPredPromptText.set("Enter a " + newValue.getJavaClass().getSimpleName() + " value");
            }
        });

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        final String labelInsert =
                preSelNewClass == null ? "resource" : preSelNewClass.getProperty(RDF.type).getResource().getLocalName();

        labelText.set("Add " + labelInsert + " to: " + navState.getSelectedParent().getFileName());

        // Reset observable class list
        newClassesList.clear();
        oh.listRelated(navState.getSelectedParent().getResource())
                .forEach(newClassesList::add);

    }


    // -------------------------------------------
    // FXML binding properties
    // -------------------------------------------

    // Main label text
    public final StringProperty labelTextProperty() { return labelText; }

    // List of available classes for adding a new instance
    public final ObservableList<Pair<com.hp.hpl.jena.ontology.ObjectProperty, OntClass>> getNewClassesList() {
        return newClassesList;
    }
    // Retrieve selected class from the UI element
    public final ObjectProperty<Pair<com.hp.hpl.jena.ontology.ObjectProperty, OntClass>> selectedNewClassProperty() {
        return selectedNewClass;
    }
    // Pre-select new class in the UI element selection model, if class is available
    public final Pair<com.hp.hpl.jena.ontology.ObjectProperty, OntClass> getPreSelNewClass() {

        List<Pair<com.hp.hpl.jena.ontology.ObjectProperty, OntClass>> getRelatedList = new ArrayList<>();
        // Pre-set selection if provided and available
        // This is required, since the object property which should connect the pre-selected
        // ontology class to the parent resource is potentially not unique and
        // not known at this point.
        if(preSelNewClass != null) {
            getRelatedList = oh.listRelated(navState.getSelectedParent().getResource())
                    .stream()
                    .filter(p -> Objects.equals(p.getValue().getURI(), preSelNewClass.getProperty(RDF.type).getObject().asResource().getURI()))
                    .collect(Collectors.toList());
        }

        return  getRelatedList.size() > 0 ? getRelatedList.get(0) : null;
    }

    // List of existing data properties for the currently selected new class
    public final ObservableList<DataPropertyTableItem> getNewPredicatesList() { return newPredicatesList; }
    // Adding additional data properties for the currently selected new class
    public final ObservableList<DatatypeProperty> getAdditionalPredicatesList() { return additionalPredicatesList; }

    // Retrieve selected predicate from the UI element
    public final ObjectProperty<DatatypeProperty> selectedPredicateProperty() { return selectedPredicate; }
    public final ObservableList<RDFDatatype> getAddPredicateTypes() { return addPredicateType; }
    // Retrieve selected RDF DataType from the UI element
    public final ObjectProperty<RDFDatatype> selectedAddPredTypeProperty() { return selAddPredType; }
    // Provide the RDF DataType of a selected predicate to the UI elements
    public final ObjectProperty<RDFDatatype> addPredTypeProperty() { return addPredType; }
    public final StringProperty addPredicateValueProperty() { return addPredValue; }
    public final StringProperty addPredicatePromptTextProperty() { return addPredPromptText; }


    // -----------------------------------------
    // Methods
    // -----------------------------------------

    /**
     * Add a new class instance and all corresponding DataProperties to RDF model
     */
    @Override
    public void ok(ActionEvent event) {
        // Get the parent resource where the new Instance should be connected to
        Resource parentResource = navState.getSelectedParent().getResource();

        Model tmpModel = ModelFactory.createDefaultModel();
        Resource newResource = ResourceFactory.createResource(UUID.randomUUID().toString());

        // Add all provided new DataProperties to the temporary model
        newPredicatesList.iterator().forEachRemaining(
                c -> {
                    if (c.getTextFieldValue() != null && !c.getTextFieldValue().isEmpty()) {
                        RDFNode o = ResourceFactory.createTypedLiteral(c.getTextFieldValue(), c.getDataType());
                        Statement s = ResourceFactory.createStatement(newResource, c.getProperty(), o);
                        tmpModel.add(s);
                    }
                });

        // TODO find better solution to deal with completely empty property values
        if (tmpModel.size() > 0) {
            // Connect the new class instance to the parent resource
            Property connectProperty = ResourceFactory.createProperty(selectedNewClass.get().getKey().getURI());
            Statement connectStatement = ResourceFactory.createStatement(parentResource, connectProperty, newResource);
            tmpModel.add(connectStatement);

            // Add the RDF type of the new class instance
            Statement rdfTypeStatement = ResourceFactory.createStatement(newResource, RDF.type, selectedNewClass.get().getValue());
            tmpModel.add(rdfTypeStatement);

            // Add the content of the temp model to the actual Jena Model
            projectState.getMetadata().add(tmpModel);

            // TODO add resource successfully added check, only hide the window, if true
            super.ok(event);

        } else {
            labelText.set("Please add at least one property value. Only properties containing values will be added");
        }
    }

    /**
     * Method for adding an additional DataProperty to the list of new DataProperties
     */
    public void addDataProperty() {

        if (selectedPredicate.get() != null && addPredValue.get() != null && !addPredValue.get().isEmpty()) {

            Set<RDFDatatype> dts = oh.getRange(selectedPredicate.get());
            RDFDatatype dt = addPredType.get();

            DataPropertyTableItem newDTI = new DataPropertyTableItem(dts, selectedPredicate.get(), dt);
            newDTI.setTextFieldValue(addPredValue.get());
            newPredicatesList.add(newDTI);
        }

        // Cleanup after the new DataProperty has been added
        addPredicateType.clear();
        // Reset add new DataProperty TextField
        addPredValue.set("");
    }

}
