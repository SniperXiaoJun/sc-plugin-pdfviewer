/*
 * Copyright (c) CFCA 2016.
 */

package com.sc.pdfviewer;

import java.io.File;

/**
 * File traverse interface
 *
 * Created by wufan on 2016/11/27.
 */
public interface FileTraverseListener {
    void visitFile(File file);
}
