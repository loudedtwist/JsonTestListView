package com.htw.warik.jsontesthtwdd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by warik on 16.11.15.
 */
public class HTTPDownloader
{
    private String agent = "HTWDresden Android App";
    public String urlstring;
    public String urlParameters;
    public int ResponseCode;
    public Context context;


    public HTTPDownloader(String urlstring)
    {
        this.urlstring = urlstring;
    }

    public static boolean CheckInternet(Context context)
    {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return wifi.isConnected() || mobile.isConnected();
    }

    public String getStringISO()
    {
        return getString("iso-8859-15");
    }

    public String getString()
    {
        return getString("UTF-8");
    }

    public String getStringWithPost()
    {
        String tmp;
        StringBuilder result = new StringBuilder();

        try
        {
            // create a url object
            URL url = new URL(urlstring);

            // create a urlconnection object
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            if (context != null)
                conn.setSSLSocketFactory(addHTWCA().getSocketFactory());

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("User-Agent", agent);
            conn.connect();

            //Send request
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            // Get the response code
            ResponseCode = conn.getResponseCode();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            while ((tmp = rd.readLine()) != null)
                result.append(tmp);

            rd.close();
            conn.disconnect();

        } catch (Exception e)
        {
            if (ResponseCode == 0)
                ResponseCode = 999;

            return null;
        }

        return result.toString();
    }

    public Bitmap getBitmap()
    {
        try
        {
            // create a url object
            URL url = new URL(urlstring);

            // create a urlconnection object
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", agent);
            conn.connect();

            ResponseCode = conn.getResponseCode();

            if (ResponseCode == 200)
            {
                InputStream is = conn.getInputStream();
                return BitmapFactory.decodeStream(is);
            }
        } catch (Exception e)
        {
            ResponseCode = 999;
        }

        return null;
    }

    protected String getString(String Encoding)
    {
        String tmp;
        StringBuilder result = new StringBuilder();

        try
        {
            // create a url object
            URL url = new URL(urlstring);

            // create a urlconnection object
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Referer", urlstring);
            conn.addRequestProperty("User-Agent", agent);
            conn.connect();

            ResponseCode = conn.getResponseCode();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Encoding));

            while ((tmp = rd.readLine()) != null)
                result.append(tmp);

            rd.close();
            conn.disconnect();

        } catch (Exception e)
        {
            if (ResponseCode == 0)
                ResponseCode = 999;
            return null;
        }

        return result.toString();
    }

    private SSLContext addHTWCA() throws Exception
    {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Load CAs from Ressource
        InputStream caInput = context.getResources().openRawResource(R.raw.ca_htw);
        Certificate ca;

        try
        {
            ca = cf.generateCertificate(caInput);
        } finally
        {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        return context;
    }
}