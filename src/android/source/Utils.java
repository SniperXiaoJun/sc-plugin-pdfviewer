/*
 * Copyright (c) CFCA 2016.
 */

package com.sc.pdfviewer;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by wufan on 2016/11/27.
 */

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static void traverseFile(File workingFile, FileTraverseListener listener) {
        if (listener == null || workingFile == null) return;
        if (workingFile.isDirectory()) {
            File[] files = workingFile.listFiles();
            if (files == null) return;
            for (File file : files) {
                traverseFile(file, listener);
            }
        } else if (workingFile.isFile()) {
            listener.visitFile(workingFile);
        }
    }

    public static boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }

    private static final float PRECISION = 0.00001f;

    public static boolean floatEquals(float a, float b) {
        return Math.abs(a - b) <= PRECISION;
    }

    public static String getCertChainPaths(Context context) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ConstantsUtils.CERTIFICATE_CHAIN_COUNT; i++) {
            String namePath = ConstantsUtils.CERTIFICATE_CHAIN_DIRECTORY
                  + File.separator
                  + ConstantsUtils.CERTIFICATE_NAME_PREFIX
                  + Integer.toString(i)
                  + ConstantsUtils.CERTIFICATE_NANE_SUFFIX;
            builder.append(getInternalPath(context, namePath)).append(ConstantsUtils.CERTIFICATE_CHAIN_PATHS_SEPARATOR);
        }
        return builder.toString();
    }

    public static File getExternalStorage() {
        return Environment.getExternalStorageDirectory();
    }

    public static File getInternalPath(Context context, String name) {
        return new File(context.getFilesDir().getPath() + File.separator + name);
    }

    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri =
                      ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                      split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
              column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean canCreate(File file) {
        try {
            boolean success = file.createNewFile();
            if (success) file.delete();
            return success;
        } catch (IOException e) {
            Log.e(TAG, "Cannot create file: " + file.getAbsolutePath());
            return false;
        }
    }

    public static byte[] bitmapToMemoryBytes(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return out.toByteArray();
    }

    public static int getDP(Context context, int dp) {
        if (dp == 0) return 0;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
              context.getResources().getDisplayMetrics());
    }

    public static void copyAssetFilesToInternalDir(Context context, String assetDir, String dstDir) throws IOException {
        AssetManager assetManager = context.getAssets();
        String fileNames[] = assetManager.list(assetDir);//获取assets目录下的所有文件及目录名
        for (String filename : fileNames) {
            File outFile = getInternalPath(context, dstDir + File.separator + filename);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            if (outFile.exists()) continue;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(assetDir + "/" + filename);
                out = new FileOutputStream(outFile);
                copy(in, out);
                out.flush();
            } finally {
                closeQuietly(in);
                closeQuietly(out);
            }
        }
    }

    public static void copyAssetFilesToExternalDir(Context context, String assetDir, String dstDir) throws IOException {
        AssetManager assetManager = context.getAssets();
        String fileNames[] = assetManager.list(assetDir);//获取assets目录下的所有文件及目录名
        for (String filename : fileNames) {
            File outFile = getExternalPath(dstDir + File.separator + filename);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            if (outFile.exists()) continue;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(assetDir + "/" + filename);
                out = new FileOutputStream(outFile);
                copy(in, out);
                out.flush();
            } finally {
                closeQuietly(in);
                closeQuietly(out);
            }
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
                Log.e(TAG, "close failed: " + ignored.getLocalizedMessage());
            }
        }
    }

    public static File getExternalPath(String name) {
        return new File(getExternalStorage() + File.separator + name);
    }

    private static final int BUFFER_LENGTH = 4096; // 4KB

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_LENGTH];
        int n = 0;
        while ((n = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, n);
        }
    }

    public static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    public static String readAllString(InputStream in) throws IOException {
        return new String(readAll(in));
    }
}
