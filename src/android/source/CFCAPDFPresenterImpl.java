/*
 * Copyright (c) CFCA 2016.
 */

package com.sc.pdfviewer;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.util.Base64;
import android.util.Log;

import com.cfca.mobile.pdfreader.CFCAPDFViewWithSign;
import com.cfca.mobile.pdfreader.signature.CFCACertificate;
import com.cfca.mobile.pdfreader.signature.OnVerifySignatureListener;
import com.cfca.mobile.pdfreader.signature.SealParameter;
import com.cfca.mobile.pdfreader.signature.SignatureFunction;
import com.cfca.mobile.pdfreader.signature.SignatureHelper;
import com.cfca.mobile.pdfreader.signature.VerifySignatureResult;
import com.cfca.mobile.pdfreader.timestamp.CFCATimestamp;
import com.cfca.mobile.pdfreader.util.Callback;
import com.cfca.mobile.pdfreader.util.Cancelable;
import com.cfca.mobile.pdfreader.util.Result;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sc.pdfviewer.ConstantsUtils.RSA_STORENAME;
import static com.sc.pdfviewer.ConstantsUtils.RSA_STOREPASS;
import static com.sc.pdfviewer.ConstantsUtils.SIGNED_DIRECTORY;
import static com.sc.pdfviewer.ConstantsUtils.SM2_STORENAME;
import static com.sc.pdfviewer.ConstantsUtils.SM2_STOREPASS;
import static com.sc.pdfviewer.ConstantsUtils.TIMESTAMP_URL;
import static com.sc.pdfviewer.Utils.readAll;

/**
 * Created by wufan on 2016/10/28.
 * CFCAPDFPresenter implementation
 */
public class CFCAPDFPresenterImpl implements CFCAPDFPresenter, SignatureFunction {

    private static final String TAG = CFCAPDFPresenterImpl.class.getSimpleName();

    private CFCAPDFPresenterView view;
    private CFCAPDFViewWithSign pdfView;
    private SignHandler signHandler;
    private Handler uiHandler;

    private boolean fixedWidget;
    private String path;
    private String signedPath;
    private Rect sealRect;
    private int sealPage;
    private byte[] sealBytes;

    private static class SignHandler extends HandlerThread {
        // Maybe handler is null, see http://stackoverflow.com/questions/25459186/nullpointerexception-in-handlerthread
        private final LinkedBlockingDeque<Runnable> runnables = new LinkedBlockingDeque<Runnable>();
        private Handler handler;

        SignHandler() {
            super("CFCAPDF");
        }

        @Override
        protected void onLooperPrepared() {
            handler = new Handler(getLooper());
            Runnable runnable;
            if ((runnable = runnables.poll()) != null) {
                post(runnable);
            }
        }

        void post(Runnable runnable) {
            if (handler == null) {
                runnables.offer(runnable);
                return;
            }
            handler.post(runnable);
        }
    }

    /**
     * signRunnable会在非UI线程SignHandler中执行，禁止UI操作
     */
    private final Runnable signRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                boolean signVisible = PreferenceManager.getDefaultSharedPreferences(view.getContext())
                      .getBoolean("key_sign_visible", true);
                SealParameter.Builder builder = new SealParameter.Builder();
                @SealParameter.SignType int signType = getSignType();
                byte[] zValue = null;
                if (signType == SealParameter.SM2_WITH_SM3) {
                    CFCACertificate cfcaCertificate = SignatureHelper.sm2PublicCertificate(SM2_STORENAME);
                    zValue = cfcaCertificate.getzValue();
                }
                builder.setPage(sealPage)
                      .setSignArea(sealRect)
                      .setFreeZone(fixedWidget ? SealParameter.IN_FIXED_ZONE : SealParameter.IN_FREE_ZONE)
                      .setSrcFile(path)
                      .setDestFile(signedPath)
                      .setSignType(signType).setSealImage(signVisible ? sealBytes : null)
                      .setzValue(zValue)
                      .setSignHandle(CFCAPDFPresenterImpl.this);
                final long ret = pdfView.seal(builder.create());
                hideLoading();
                if (ret != 0) {
                    signFailedAndReloadPdf(getResource("sign_failure"));
                } else {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            view.showToast(getResource("sign_success"));
                            view.showPDF(signedPath, true);
                        }
                    });
                }
            } catch (final Exception e) {
                hideLoading();
                Log.e(TAG, "seal failed: " + e.getLocalizedMessage(), e);
                signFailedAndReloadPdf(e.getLocalizedMessage());
            }
        }
    };

    private final OnVerifySignatureListener onVerifySignatureListener = new OnVerifySignatureListener() {
        @Override
        public void onVerified(Result<ArrayList<VerifySignatureResult>> result) {
            view.hideLoading();
            if (!result.isSuccess()) {
                view.showError(result.getError().getLocalizedMessage());
            } else {
                view.showVerifySignatureResultView(result.getResult());
            }
        }
    };

    @Override
    public void verifySeal(String crl, String certChains, boolean verifyClickedSeal) {
        try {
            view.showLoading();
            if (verifyClickedSeal) {
                pdfView.verifyClickedWidgetSignature(crl, certChains, onVerifySignatureListener);
            } else {
                pdfView.verifySignature(crl, certChains, onVerifySignatureListener);
            }
        } catch (IllegalStateException e) {
            view.hideLoading();
            view.showError(getResource("err_no_pdf_opened"));
        }
    }

    @Override
    public void prepareSignPdf(String path, int page, Rect rect, byte[] sealBytes) {
        this.path = path;
        this.sealPage = page;
        this.sealRect = rect;
        this.sealBytes = sealBytes;
        this.fixedWidget = false;
    }

    @Override
    public void prepareSignPdfFixedWidget(String path, int page, byte[] sealBytes) {
        this.path = path;
        this.sealPage = page;
        this.sealBytes = sealBytes;
        this.fixedWidget = true;
    }

    private File getSaveAsFile(File file) {
        Pattern pattern = Pattern.compile("(.*)/(.*)\\.pdf");
        Matcher matcher = pattern.matcher(file.getAbsolutePath());
        if (matcher.find()) {
            String realFileName = matcher.group(2);
            for (int i = 0; i < Integer.MAX_VALUE; ++i) {
                File saveAsFile = new File(SIGNED_DIRECTORY + File.separator + realFileName + "_signed" + i + ".pdf");
                if (!saveAsFile.exists()) return saveAsFile;
            }
        }
        throw new IllegalArgumentException(getResource("err_illegal_file_name"));
    }

    @Override
    public void signPdf() {
        view.showLoading();
        signHandler.post(signRunnable);
    }

    @Override
    public void setSignedPath(String signedPath) {
        this.signedPath = signedPath;
    }

    @Override
    public void sharePdf() {
        view.showToast(getResource("unsupported"));
    }

    @Override
    public void bind(CFCAPDFPresenterView view) {
        this.view = view;
        pdfView = view.getPdfView();
        uiHandler = new Handler(Looper.getMainLooper());
        signHandler = new SignHandler();
        signHandler.start();
    }

    @Override
    public void unbind(CFCAPDFPresenterView view) {
        signHandler.quit();
    }

    @Override
    public Cancelable signPkcs7WithHash(byte[] hashValue, int hashType, final Callback<byte[]> signature) {
        final SignAsyncTask signAsyncTask = new SignAsyncTask(hashValue, hashType, signature,
              PreferenceManager.getDefaultSharedPreferences(view.getContext()).getBoolean("key_timestamp", false));
        signAsyncTask.execute();
        return new Cancelable() {
            @Override
            public void cancel() {
                signAsyncTask.cancel(true);
            }
        };
    }

    private void signFailedAndReloadPdf(final String msg) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                // 重新加载PDF, runnable为加载成功后运行的方法
                view.reloadPDF(new Runnable() {
                    @Override
                    public void run() {
                        view.showError(msg);
                    }
                });
            }
        });
    }

    private String getResource(String name) {
        return view.getContext().getString(MyResource.getIdByName(view.getContext(),"string",name));
    }

    private void hideLoading() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                view.hideLoading();
            }
        });
    }

    private int getSignType() {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(view.getContext())
              .getString("key_sign_type", Integer.toString(SealParameter.RSA_WITH_SHA256)));
    }

    private static class SignAsyncTask extends AsyncTask<Void, Void, byte[]> {

        private final byte[] hashValue;
        private final int hashType;
        private final Callback<byte[]> signatureCallback;
        private final String pfxPath;
        private final String pfxPassword;
        private final boolean withTimestamp;

        private SignAsyncTask(byte[] hashValue, int hashType, Callback<byte[]> signatureCallback,
              boolean withTimestamp) {
            this.hashValue = hashValue;
            this.hashType = hashType;
            this.signatureCallback = signatureCallback;
            if (this.hashType == SealParameter.SM2_WITH_SM3) {
                pfxPath = SM2_STORENAME;
                pfxPassword = SM2_STOREPASS;
            } else {
                pfxPath = RSA_STORENAME;
                pfxPassword = RSA_STOREPASS;
            }
            this.withTimestamp = hashType != SealParameter.SM2_WITH_SM3 && withTimestamp;
        }

        @Override
        protected byte[] doInBackground(Void... params) {
            byte[] pkcs7Signature = SignatureHelper.signHashPKCS7WithPFX(pfxPath, pfxPassword, hashValue, hashType);
            if (pkcs7Signature == null) return null;
            if (!withTimestamp) {
                return pkcs7Signature;
            }
            try {
                final CFCATimestamp timestampUtil = CFCATimestamp.getInstance();
                byte[] timestampReq = timestampUtil.generateTimestampReqByHash(hashType, hashValue);
                Log.e(TAG, "TimestampReq: " + Base64.encodeToString(timestampReq, Base64.NO_WRAP));
                byte[] timestampResp = requestTimestampResp(timestampReq);
                Log.e(TAG, "TimestampResp: " + Base64.encodeToString(timestampResp, Base64.NO_WRAP));
                return timestampUtil.updateTimestamp(pkcs7Signature, timestampResp);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            if (bytes == null) {
                signatureCallback.onError(new Exception("Sign failed"));
            } else {
                signatureCallback.onResult(bytes);
            }
        }

        private byte[] requestTimestampResp(byte[] timestampReq) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(TIMESTAMP_URL).openConnection();
            try {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setConnectTimeout(5000); // 5s
                connection.setReadTimeout(5000); // 5s
                connection.addRequestProperty("Content-type", "application/timestamp-query");

                OutputStream outputStream = connection.getOutputStream();
                try {
                    outputStream.write(timestampReq);
                } finally {
                    outputStream.close();
                }

                InputStream in = getInputStream(connection);
                try {
                    return readAll(getInputStream(connection));
                } finally {
                    in.close();
                }
            } finally {
                connection.disconnect();
            }
        }

        private static InputStream getInputStream(final HttpURLConnection connection) {
            try {
                return connection.getInputStream();
            } catch (IOException e) {
                return connection.getErrorStream();
            }
        }
    }
}
