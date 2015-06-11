// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.app.ui.util;

/**
 * Abstract class for controllers that can be used in a {@link ValueDialogView}.
 */
public abstract class ValueDialogCtrl<T> extends DialogCtrl<T> {
    /**
     * Return the current value of the controller.
     * In most cases this should be a non null value.
     *
     * @return The controller value.
     */
    abstract public T getValue();

}
