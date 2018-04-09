/*
 * Copyright (c) CFCA 2016.
 */

package com.sc.pdfviewer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.cfca.mobile.pdfreader.CFCAPDFView;
import com.cfca.mobile.pdfreader.CFCAPDFViewWithSign;
import com.cfca.mobile.pdfreader.exception.PasswordRequiredException;
import com.cfca.mobile.pdfreader.listener.OnErrorListener;
import com.cfca.mobile.pdfreader.listener.OnLoadCompleteListener;
import com.cfca.mobile.pdfreader.listener.OnWidgetClickedListener;
import com.cfca.mobile.pdfreader.scroll.DefaultScrollHandle;
import com.cfca.mobile.pdfreader.signature.VerifySignatureResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.sc.pdfviewer.ConstantsUtils.CRL_NAME;
import static com.sc.pdfviewer.Utils.getCertChainPaths;
import static com.sc.pdfviewer.Utils.getExternalStorage;
import static com.sc.pdfviewer.Utils.getInternalPath;
import static com.sc.pdfviewer.Utils.getPath;
import static com.sc.pdfviewer.Utils.isEmpty;


/**
 * Display pdf file, support signing and verify signature
 *
 * Created by wufan on 2016/11/25.
 */
public class CFCAPDFActivity extends AppCompatActivity
      implements CFCAPDFPresenterView, OnLoadCompleteListener, OnErrorListener, OnWidgetClickedListener{

    private static final String PDF_PATH = getExternalStorage().getPath() + File.separator + "民生银行PIN码加密流程数据示例1.pdf";
    private static final String TAG = CFCAPDFActivity.class.getSimpleName();

    enum PDFState {
        PDF_INIT,
        PDF_LOADED,
    }

    private CFCAPDFPresenter presenter;
    private boolean isFullScreen;
    private Handler handler = new Handler();

    private CFCAPDFViewWithSign pdfView;
    private String pdfPath;

    private AlertDialog loadingDialog;
    private Runnable reloadPdfRunnable;
    private PDFState pdfState = PDFState.PDF_INIT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(MyResource.getIdByName(this,"layout","activity_cfcapdf"));

        pdfView = (CFCAPDFViewWithSign) findViewById(MyResource.getIdByName(this,"id","pdfview"));
        createLoadingDialog();

        presenter = new CFCAPDFPresenterImpl();
        presenter.bind(this);

        String pdfPath = "";
        if (savedInstanceState != null) {
            pdfPath = savedInstanceState.getString("PDF_PATH", "");
        } else {
            Intent intent = getIntent();
            if (intent != null) {
                if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                    pdfPath = getPath(this, intent.getData());
                }
            }
        }
        if (isEmpty(pdfPath)) pdfPath = PDF_PATH;
        showPDF(pdfPath, false);
    }

    private void createLoadingDialog() {
        loadingDialog = new AlertDialog.Builder(this).setView(MyResource.getIdByName(getContext(),"layout","loading")).create();
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unbind(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("PDF_PATH", pdfPath);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showError(String e) {
        Toast.makeText(this, e, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showFullScreen() {
        isFullScreen = true;
    }

    @Override
    public void showNonFullScreen() {
        isFullScreen = false;
    }

    private void toggle() {
        if (isFullScreen) {
            showNonFullScreen();
        } else {
            showFullScreen();
        }
    }

    @Override
    public void showPDF(String pdfPath, boolean samePosition) {
        showPDF(pdfPath, samePosition, "");
    }

    @Override
    public void showPDF(String pdfPath, boolean samePosition, String password) {
        try {
            float lastPositionProportion = 0f;
            if (samePosition) {
                lastPositionProportion = pdfView.getPositionOffsetProportion();
            }
            File file = new File(pdfPath);
            this.pdfPath = pdfPath;
            this.pdfState = PDFState.PDF_INIT;
            CFCAPDFView.fromFile(file)
                  .password(password)
                  .setReadStyle(
                        isVertical() ? CFCAPDFView.READ_VERTICAL_CONTINUOUS : CFCAPDFView.READ_HORIZONTAL_SINGLE)
                  .onSignatureWidgetClicked(this)
                  .onLoad(this)
                  .onError(this)
                  .defaultPositionProportion(lastPositionProportion)
                  .scrollHandle(new DefaultScrollHandle(this))
                  .load(pdfView);
            showFullScreen();
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isVertical() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("key_pdf_layout", true);
    }

    @Override
    public void reloadPDF() {
        showPDF(pdfPath, true);
    }

    @Override
    public void reloadPDF(Runnable runnable) {
        reloadPDF();
        this.reloadPdfRunnable = runnable;
    }

    @Override
    public void showVerifySignatureResultView(ArrayList<VerifySignatureResult> verifySignatureResults) {
        if (verifySignatureResults == null || verifySignatureResults.size() == 0) {
            Toast.makeText(this, getResources().getString(MyResource.getIdByName(this,"string","no_verify_signature_result")), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void showLoading() {
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    @Override
    public void hideLoading() {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public CFCAPDFViewWithSign getPdfView() {
        return pdfView;
    }

    @Override
    public void onLoadCompleted(int pages) {
        showNonFullScreen();
        pdfState = PDFState.PDF_LOADED;
        if (reloadPdfRunnable != null) {
            handler.post(reloadPdfRunnable);
            reloadPdfRunnable = null;
        }
    }

    @Override
    public void onError(Throwable t) {
        showError(t.getLocalizedMessage());
        if (t instanceof PasswordRequiredException) {
            showPDFPasswordDialog();
        }
    }

    @Override
    public void onNormalWidgetClicked(MotionEvent event, int page) {
        toggle();
    }

    @Override
    public void onUnsignedWidgetClicked(MotionEvent event, int page, RectF rectF) {
    }

    @Override
    public void onSignedWidgetClicked(MotionEvent event, int page) {
        presenter.verifySeal(getCrlPath(), getCertChains(), true);
    }

    @Override
    public void onNotSupportedWidgetClicked(MotionEvent event, int page) {
        showToast("Not supported widget clicked");
    }

    private String getCrlPath() {
        return getInternalPath(getContext(), CRL_NAME).getAbsolutePath();
    }

    private String getCertChains() {
        return getCertChainPaths(getContext());
    }

    private void showPDFPasswordDialog() {
        final EditText inputPassword = new EditText(this);
        //inputPassword.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        inputPassword.setHint(getResources().getString(MyResource.getIdByName(this,"string","pdf_password_hint")));
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(getResources().getString(MyResource.getIdByName(this,"string","pdf_needs_password")))
              .setView(inputPassword)
              .setPositiveButton(getResources().getString(MyResource.getIdByName(CFCAPDFActivity.this,"string","confirm")), new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                      showPDF(pdfPath, false, inputPassword.getText().toString());
                  }
              });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow()
                  .clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }
}
