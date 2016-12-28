package com.netease.cloud.nos.android.ssl;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;
import javax.net.ssl.SSLEngine;
import com.netease.cloud.nos.android.utils.LogUtil;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import javax.net.ssl.TrustManagerFactory;


public class EasyX509TrustManager
        implements X509TrustManager {

    private X509TrustManager standardTrustManager = null;

    /**
     * Constructor for EasyX509TrustManager.
     */
    public EasyX509TrustManager(KeyStore keystore)
            throws NoSuchAlgorithmException, KeyStoreException {
        super();
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keystore);
        TrustManager[] trustmanagers = factory.getTrustManagers();
        if (trustmanagers.length == 0) {
            throw new NoSuchAlgorithmException("no trust manager found");
        }
        this.standardTrustManager = (X509TrustManager) trustmanagers[0];
    }

    /**
     * @see X509TrustManager#checkClientTrusted(X509Certificate[], String authType)
     */
    @Override
    public void checkClientTrusted(X509Certificate[] certificates, String authType)
            throws CertificateException {
        standardTrustManager.checkClientTrusted(certificates, authType);
    }

    /**
     * @see X509TrustManager#checkServerTrusted(X509Certificate[], String authType)
     */
    @Override
    public void checkServerTrusted(X509Certificate[] certificates, String authType)
            throws CertificateException {
        if ((certificates != null) && (certificates.length == 1)) {
            certificates[0].checkValidity();
        } else {
            standardTrustManager.checkServerTrusted(certificates, authType);
        }
    }

    /**
     * @see X509TrustManager#getAcceptedIssuers()
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.standardTrustManager.getAcceptedIssuers();
    }

}

