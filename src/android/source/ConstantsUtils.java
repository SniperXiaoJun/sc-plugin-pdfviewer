/*
 * Copyright (c) CFCA 2016.
 */

package com.sc.pdfviewer;

import android.os.Environment;

import java.io.File;

/**
 * Common constants
 *
 * Created by wufan on 2016/11/25.
 */
public class ConstantsUtils {
    public static final String WORKING_SIMPLE_NAME = "PDFDemo";
    public static final String SIGNED_SIMPLE_NAME = "signed";
    public static final String WORKING_DIRECTORY =
          Environment.getExternalStorageDirectory() + File.separator + WORKING_SIMPLE_NAME;
    public static final String SIGNED_DIRECTORY = WORKING_DIRECTORY + File.separator + SIGNED_SIMPLE_NAME;
    public static final int SECTION_COUNT = 2;
    public static final int SECTION_SIGNED = 0;
    public static final int SECTION_UNSIGNED = 1;

    public static final String RSA_STORENAME = Utils.getExternalPath("pfx/rsa1024_11111111.pfx").getAbsolutePath();
    public static final String RSA_STOREPASS = "11111111";
    public static final String SM2_STORENAME = Utils.getExternalPath("pfx/sm2Encrypt.sm2").getAbsolutePath();
    public static final String SM2_STOREPASS = "111111";

    public static final String CRL_NAME = "Revoked_0.crl";
    public static final String CERTIFICATE_NANE_SUFFIX = ".cer";
    public static final int CERTIFICATE_CHAIN_COUNT = 29;
    public static final String CERTIFICATE_NAME_PREFIX = "CFCA_";
    public static final String CERTIFICATE_CHAIN_DIRECTORY = "certChain";
    public static final String CERTIFICATE_CHAIN_PATHS_SEPARATOR = "|";
    public static final String CRL_DIRECTORY = "crl";
    public static final String SEAL_DIRECTORY = "seal";

    public static final String TIMESTAMP_URL = "http://210.74.41.195/timestamp";
    public enum FileUnit {
        BYTE,
        KB,
        MB,
        GB
    }
}
