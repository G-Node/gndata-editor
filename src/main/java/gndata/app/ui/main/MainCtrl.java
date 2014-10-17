package gndata.app.ui.main;

import gndata.app.ui.tree.metadata.MetadataTreeView;
import gndata.app.ui.tree.files.FileTreeView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainCtrl implements Initializable {

    @FXML
    private SplitPane splitPane;

    @FXML
    public BorderPane view;

    @Inject
    private MenuView menuView;

    @Inject
    private MetadataTreeView metadataView;

    @Inject
    private FileTreeView fileView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // top Menu bar
            view.setTop(menuView.getScene());

            // split pane with metadata tree
            splitPane.getItems().add(metadataView.getScene());
            splitPane.getItems().add(fileView.getScene());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
