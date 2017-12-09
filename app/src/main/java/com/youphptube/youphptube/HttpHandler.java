package com.youphptube.youphptube;

        import android.content.Intent;
        import android.net.Uri;
        import android.provider.SyncStateContract;
        import android.util.Log;

        import java.io.BufferedInputStream;
        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.io.OutputStreamWriter;
        import java.net.DatagramPacket;
        import java.net.DatagramSocket;
        import java.net.HttpURLConnection;
        import java.net.InetAddress;
        import java.net.MalformedURLException;
        import java.net.ProtocolException;
        import java.net.URL;

class HttpHandler {

    private static final String TAG = HttpHandler.class.getSimpleName();

    static String cookie;

    HttpHandler() {
    }

    String GetVideos(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Cookie", cookie);
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    String GetVideo(String reqUrl, String videoID) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Cookie", cookie);
            conn.setRequestMethod("POST");

            // Append parameters to URL
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("id", videoID);
            String query = builder.build().getEncodedQuery();


            // Open connection for sending data
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();



            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }

        return response;
    }


    String LikeVideo(String reqUrl, String videoID, String LikeType) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Cookie", cookie);
            conn.setRequestMethod("POST");

            // Append parameters to URL
            Uri.Builder builder = new Uri.Builder()
                    //.appendQueryParameter("like", LikeType)
                    .appendQueryParameter("videos_id", videoID);


            String query = builder.build().getEncodedQuery();


            // Open connection for sending data
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();



            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }

        return response;
    }



    class LoginInfo{
        String cookie = "";
        String response = "";
    }

    LoginInfo Login(String reqUrl, String UserName, String Password) {
        LoginInfo response = new LoginInfo();
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Append parameters to URL
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("user", UserName)
                    .appendQueryParameter("pass", Password);
            String query = builder.build().getEncodedQuery();
            conn.setRequestProperty("Cookie", "");

            // Open connection for sending data
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());

            response.cookie = conn.getHeaderField("Set-Cookie");
            response.response = convertStreamToString(in);
            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }



    void Teste(String reqUrl, String UserName, String Password) {
        LoginInfo response = new LoginInfo();
        String xmlString = " ‘Content-Type: text/xml; charset=utf-8′ -H ‘SOAPAction: “urn:schemas-upnp-org:service:AVTransport:1#SetAVTransportURI”‘ -d ‘<?xml version=”1.0″ encoding=”utf-8″?><s:Envelope s:encodingStyle=”http://schemas.xmlsoap.org/soap/encoding/” xmlns:s=”http://schemas.xmlsoap.org/soap/envelope/”><s:Body><u:SetAVTransportURI xmlns:u=”urn:schemas-upnp-org:service:AVTransport:1″><InstanceID>0</InstanceID><CurrentURI><![CDATA[http://my.site.com/path/to/my/content.mp4]]></CurrentURI><CurrentURIMetaData></CurrentURIMetaData></u:SetAVTransportURI></s:Body></s:Envelope>’ ";
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Append parameters to URL
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("user", UserName)
                    .appendQueryParameter("pass", Password);
            String query = builder.build().getEncodedQuery();
            conn.setRequestProperty("Cookie", "");

            // Open connection for sending data
            OutputStream os = conn.getOutputStream();
            os.write(xmlString.getBytes("UTF-8"));

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());


            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

}