// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.app.ui.main;

import gndata.app.state.AppState;
import gndata.app.state.ProjectState;
import gndata.app.ui.dia.ProjectConfigView;
import gndata.app.ui.dia.ProjectListView;
import gndata.lib.config.GlobalConfig;
import gndata.lib.config.ProjectConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the main menu view.
 */
public class MenuCtrl implements Initializable {

    private final AppState appState;
    private final ProjectState projectState;

    @FXML
    private MenuBar menu;
    @FXML
    private Menu projectMenu;

    @Inject
    public MenuCtrl(AppState appState, ProjectState projectState) {
        this.appState = appState;
        this.projectState = projectState;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // set a listener that hides or shows the project menu depending on the state
        this.projectState.addListener((obs, o, n) -> projectMenu.setVisible(n != null));
        
        //use osx system menu
        menu.setUseSystemMenuBar(true);
    }

    /**
     * Open a new project or create one.
     */
    public void createProject() {
        try {
            Optional<File> optDir = showDirectoryChooser();

            if (! optDir.isPresent())
                return;

            ProjectConfig config = ProjectConfig.load(optDir.get().getAbsolutePath());
            Optional<ProjectConfig> optConfig = showConfigDialog(config);

            if (optConfig.isPresent()) {
                config = optConfig.get();

                config.store();
                projectState.setConfig(config);

                appState.getConfig().setProject(config.getProjectPath(), config.getName());
                appState.getConfig().store();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows a directory chooser dialog.
     *
     * @return The selected dialog or null if the selection was canceled.
     */
    protected Optional<File> showDirectoryChooser() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select the project directory");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        return Optional.ofNullable(dirChooser.showDialog(menu.getScene().getWindow()));
    }

    /**
     * Shows a project config dialog.
     *
     * @param config    The configuration to edit in the dialog.
     *
     * @return The edited
     */
    protected Optional<ProjectConfig> showConfigDialog(ProjectConfig config) {
        ProjectConfigView configDialog = new ProjectConfigView(config);
        return configDialog.showAndGet();
    }

    /**
     * Show the project settings dialog and save the project settings
     * if they have changed.
     */
    public void projectSettings() {
        Optional<ProjectConfig> optConfig = showConfigDialog(projectState.getConfig());

        if (optConfig.isPresent()) {
            try {
                ProjectConfig config = optConfig.get();

                config.store();
                projectState.setConfig(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Open a previously opened project.
     */
    public void openProject() {
        Optional<String> optPath = showListDialog(this.appState.getConfig());

        if (optPath.isPresent()) {
            try {
                ProjectConfig config = ProjectConfig.load(optPath.get());
                projectState.setConfig(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Show a project list dialog.
     *
     * @param config The global configuration object.
     * @return A string containing the path to the selected project or null.
     */
    protected Optional<String> showListDialog(GlobalConfig config) {
        ProjectListView listView = new ProjectListView(config.getProjects());
        return listView.showAndGet();
    }

    public void importMetadata() {
        Optional<File> fopt = askForFile("Select the metadata to import",
                                         new FileChooser.ExtensionFilter("RDF data", "*.ttl", "*.rdf"),
                                         new FileChooser.ExtensionFilter("All Files", "*.*"));
        fopt.ifPresent(f -> projectState.getMetadata().importMetadata(f.getPath()));
    }

    protected Optional<File> askForFile(String title,
                                        FileChooser.ExtensionFilter... extensionFilters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        if (extensionFilters != null) {
            fileChooser.getExtensionFilters().addAll(extensionFilters);
        }

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        return Optional.ofNullable(fileChooser.showOpenDialog(menu.getScene().getWindow()));
    }

    /**
     * Set the project state to not running.
     */
    public void exit() {
        appState.setRunning(false);
    }

}
