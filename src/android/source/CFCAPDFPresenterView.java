/*
 * Copyright (c) CFCA 2016.
 */

package com.sc.pdfviewer;

import com.cfca.mobile.pdfreader.CFCAPDFViewWithSign;
import com.cfca.mobile.pdfreader.signature.VerifySignatureResult;

import java.util.ArrayList;

/**
 * Created by wufan on 2016/10/28.
 */

public interface CFCAPDFPresenterView extends BaseView {
    void showFullScreen();

    void showNonFullScreen();

    void showPDF(String pdfPath, boolean samePosition);

    void showPDF(String pdfPath, boolean samePosition, String password);

    void reloadPDF();

    void reloadPDF(Runnable runnable);

    void showVerifySignatureResultView(ArrayList<VerifySignatureResult> signatureResults);

    void showLoading();

    void hideLoading();

    CFCAPDFViewWithSign getPdfView();

}
