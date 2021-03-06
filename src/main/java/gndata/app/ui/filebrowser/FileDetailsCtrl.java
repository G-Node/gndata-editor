// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.app.ui.filebrowser;

import java.net.URL;
import java.util.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import com.google.inject.Inject;
import gndata.app.state.FileNavigationState;
import gndata.lib.srv.LocalFile;
import org.controlsfx.control.*;

/**
 * Controller for FileDetails
 */
public class FileDetailsCtrl implements Initializable{

    // required to set GridView / ListView
    @FXML
    private BorderPane detailsView;

    // must not be declared in the FXML file, leads to a css problem otherwise
    // could be revisited in future development
    // also GridView is not selectable, so not really useful for our purposes
    // since there is no proper alternative, leave it in here and
    // replace in the future with something better / completely different.
    private GridView<LocalFile> fileGrid = new GridView<>();

    private ListView<String> fileDetailsList = new ListView();

    private FileNavigationState navState;
    private ObservableList<LocalFile> listFiles;

    private ObservableList<String> fileDetails;

    @Inject
    public FileDetailsCtrl(FileNavigationState navState) {
        this.navState = navState;
        this.navState.selectedFileProperty().addListener(new SelectedFileListener());

        listFiles = FXCollections.observableList(new ArrayList<>());
        fileDetails = FXCollections.observableList(new ArrayList<>());

        fileGrid.setCellHeight(80);
        fileGrid.setCellWidth(100);

        fileGrid.getItems().addAll(listFiles);
        fileDetailsList.getItems().addAll(fileDetails);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileGrid.setCellFactory(cell -> new FileGridCell());
    }

    /**
     * Listener to react on changes to selected items in FileList.java.
     * If a directory is selected in FileList display the contents of the
     * folder in a GridView. If a file is selected, display the file attributes
     * in a ListView.
     */
    private class SelectedFileListener implements ChangeListener<LocalFile> {

        @Override
        public void changed(ObservableValue<? extends LocalFile> observable, LocalFile oldValue, LocalFile newValue) {
            if (newValue == null || oldValue == newValue)
                return;

            // if selected item in FileList is a directory, display contents
            if (newValue.isDirectory()) {
                detailsView.centerProperty().setValue(fileGrid);

                listFiles.clear();
                newValue.getChildren().stream()
                        .forEach(listFiles::add);

                fileGrid.getItems().clear();
                fileGrid.getItems().addAll(listFiles);
            } else {

                // set ListView as display main element
                detailsView.centerProperty().setValue(fileDetailsList);

                // clear existing data
                fileDetails.clear();
                fileDetailsList.getItems().clear();

                newValue.getFileInfoList().stream()
                        .forEach(fileDetails::add);

                // add list of file attributes to ListView
                fileDetailsList.getItems().addAll(fileDetails);
            }
        }
    }

    /**
     * Layout of the GridCells for displaying the contents
     * of a selected directory.
     */
    private class FileGridCell extends GridCell<LocalFile> {

        protected final ObjectProperty<Image> icon;
        protected final StringProperty title;

        public FileGridCell() {
            Label titleLabel = new Label();
            titleLabel.setPrefWidth(80);
            titleLabel.setWrapText(true);
            titleLabel.setTextAlignment(TextAlignment.CENTER);
            title = titleLabel.textProperty();

            ImageView iconView = new ImageView();
            icon = iconView.imageProperty();

            VBox vbox = new VBox(iconView, titleLabel);
            vbox.setAlignment(Pos.TOP_CENTER);

            setGraphic(vbox);
        }

        @Override
        protected final void updateItem(LocalFile item, boolean empty) {
            super.updateItem(item, empty);
            icon.set(null);
            title.setValue("");
            if ( ! empty ) {
                icon.set(item.getIcon());
                title.setValue(item.getFileName());
            }
        }
    }

}
