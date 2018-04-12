package com.sc.pdfviewer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by lizhen on 2017/11/7. 银行卡OCR
 */

public class PDFPlugin extends CordovaPlugin {
    private Context context;

    /**
     *
     */
    @Override
    public boolean execute(String action, String rawArgs, CallbackContext callbackContext) throws JSONException {
         this.context = cordova.getActivity();
            if ("viewpdf".equals(action)) {
                if (!TextUtils.isEmpty(args.get(0).toString())) {
                    File file = base64ToFile(args.get(0).toString());
                    if (file != null) {
                        Uri uri = Uri.fromFile(file);
                        Intent intent = new Intent(context, CFCAPDFActivity.class);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        context.startActivity(intent);
                    }
                }
            return true;
        }
        return super.execute(action, rawArgs, callbackContext);
    }


    public File base64ToFile(String base64) {
        File file = null;
        String fileName = "/protocal.pdf";
        FileOutputStream out = null;
        try {
            // 解码，然后将字节转换为文件
            file = new File(context.getCacheDir(), fileName);
            if (!file.exists())
                file.createNewFile();
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);// 将字符串转换为byte数组
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            byte[] buffer = new byte[1024];
            out = new FileOutputStream(file);
            int bytesum = 0;
            int byteread = 0;
            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread); // 文件写操作
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        } finally {
            try {
                if (out!= null) {
                    out.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }




}
