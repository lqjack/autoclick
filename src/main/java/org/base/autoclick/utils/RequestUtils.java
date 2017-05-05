package org.base.autoclick.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class RequestUtils {

    public static Callable<String> request(URL url)throws Throwable {
        return request(url, 0);
    }

    public static Callable<String> request(URL url, int timeout, final ProxyConfig proxyConfig) throws Throwable {
        return () -> {
            HttpURLConnection conn = null;
            Checks.checkEmpty(url, "url cannot be null");
            try {
                Checks.checkEmpty(proxyConfig, "proxy config cannot bu null");

                if (!proxyConfig.isEnableProxy()) {
                    conn = (HttpURLConnection) url.openConnection(proxyConfig.getProxy());
                }else {
                    conn = (HttpURLConnection) url.openConnection();
                }
                proxyConfig.urlAuthoInfo(conn);
                conn.setDoOutput(true);
                conn.setUseCaches (false);
                conn.setDoOutput(true);
                conn.setAllowUserInteraction(false);
                conn.setReadTimeout(timeout <= 0 ? 1 * 1_000 : timeout * 1_000);
                preResponseCode(conn);
                return convert(conn.getInputStream());
            } finally {
                conn.disconnect();
            }
        };

    }
    public static Callable<String> request(URL url, int timeout) throws Throwable {
        return request(url,timeout,null);
    }

    private static String convert(InputStream stream) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(streamReader);
        return  reader.lines().collect(Collectors.joining());
    }

    private static void preResponseCode(HttpURLConnection conn) throws Exception{
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            //warning output the code to client
        }
        //slient

        //TODO other http head
    }

    private Authenticator buildAuthenticator(final String username, final String password) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        };
//        Authenticator.setDefault(authenticator);
    }

    private Proxy buildProxy(final String host, final int port) {
        return new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(host, port));
    }
}