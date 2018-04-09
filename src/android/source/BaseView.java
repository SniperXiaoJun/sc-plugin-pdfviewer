/*
 * Copyright (c) CFCA 2016.
 */

package com.sc.pdfviewer;

import android.content.Context;

/**
 * Created by wufan on 2016/10/28.
 */

public interface BaseView {
    Context getContext();

    void showError(String e);

    void showToast(String resource);
}

