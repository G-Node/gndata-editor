// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.app.ui.util;

import javafx.event.ActionEvent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.*;

/**
 * Basic tests for the StringDialogCtrl class
 */
public class StringDialogCtrlTest {

    private StringDialogCtrl controller;
    private String title;
    private String value;

    @Before
    public void setUp() throws Exception {
        title = "testTitle";
        value = "testValue";
        controller = new StringDialogCtrl(title, value);
        controller.initialize(null, null);
    }

    @Test
    public void testProperties() throws Exception {
        assertThat(controller.titleValueProperty().get()).isEqualTo(title);
        assertThat(controller.updateValueProperty().get()).isEqualTo(value);
        assertThat(controller.promptValueProperty().get()).isNotEmpty();
    }

    @Test
    public void testGetValue() throws Exception {
        assertThat(controller.get()).isEqualTo(value);
    }

    @Test
    public void testOk() throws Exception {
        // test empty entry
        String promptTest = "testEmpty";
        controller.promptValueProperty().set(promptTest);
        controller.updateValueProperty().set("");
        controller.ok(new ActionEvent());
        assertThat(controller.promptValueProperty().get()).isNotEqualTo(promptTest);

        // TODO implementation of FXML initialization is required
        // before the second part of the OK method can be tested
        // since it requires FXML components.
        /*
        // test valid entry
        controller.updateValueProperty().set("updateTestValue");
        controller.ok(new ActionEvent(new BorderPane(), new Button()));
        assertThat(controller.isCancelled()).isFalse();
        */
    }

}
