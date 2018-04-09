/*
 * Copyright (c) CFCA 2016.
 */

package com.sc.pdfviewer;

/**
 * Created by wufan on 2016/10/28.
 */

public interface BasePresenter<V extends BaseView> {
    void bind(V view);
    void unbind(V view);
}
