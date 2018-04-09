/*
 * Copyright (c) CFCA 2016.
 */

package com.sc.pdfviewer;

import android.graphics.Rect;

/**
 * Created by wufan on 2016/10/28.
 */

public interface CFCAPDFPresenter extends BasePresenter<CFCAPDFPresenterView> {
    void verifySeal(String crl, String certChains, boolean verifyClickedSeal);

    void prepareSignPdf(String path, int page, Rect rect, byte[] sealBytes);

    void prepareSignPdfFixedWidget(String path, int page, byte[] sealBytes);

    void signPdf();

    void sharePdf();

    void setSignedPath(String path);
}
