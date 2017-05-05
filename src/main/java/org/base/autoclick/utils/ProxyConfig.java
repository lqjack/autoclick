package org.base.autoclick.utils;


import lombok.Data;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLConnection;
import java.util.Base64;

@Data
public class ProxyConfig {

    public static final ProxyConfig DEFAULT = new ProxyConfig();

    public static final String DEFAUT_SCHEMA = Proxy.Type.DIRECT.name();
    private String userName;
    private String password;
    private String host;
    private int port;
    private String schema;
    private boolean enableProxy;
    private boolean enableAuth;

    public ProxyConfig(String fullServerName, String userName, String password) {
        extractServerInfo(fullServerName);
    }

    public ProxyConfig() {
        this(null, (Integer) null);
        setEnableProxy(false);
    }

    public ProxyConfig(String host, int port) {
        setEnableProxy(true);
        setHost(host);
        setPort(port);
        setEnableAuth(false);
        setDefaultAuthInfo();
    }

    public ProxyConfig(String host, int port, String schema, String userName, String password) {
        setHost(host);
        setPort(port);
        setSchema(schema);
        setSchema(userName);
        setPassword(password);
        setEnableProxy(true);
        setEnableAuth(true);
    }

    private void extractServerInfo(String fullServerName) {
        //TODO regular i.e. https://google.com:80/path,ftp://10.5.2.2:90/path,sock://
    }

    public Proxy getProxy() {
        Proxy proxy = new Proxy(getSchemaType(), new InetSocketAddress(getHost(), getPort()));

        System.setProperty("http.proxyHost", getHost());
        System.setProperty("http.proxyPort", "" + getPort());
        return proxy;
    }

    public void urlAuthoInfo(URLConnection connection) {
        if (isEnableAuth()) {
            String encoded = new String
                    (Base64.getEncoder().encode(new String(getUserName() + ":" + getPassword()).getBytes()));
            connection.setRequestProperty("Proxy-Authorization", "Basic " + encoded);
        }
    }

    private void setDefaultAuthInfo() {
        setUserName("guest");
        setPassword("guest");
    }

    public Proxy.Type getSchemaType() {
        if ((null == getSchema()) || (getSchema().equalsIgnoreCase(DEFAUT_SCHEMA))) {
            return Proxy.Type.DIRECT;
        } else if (getSchema().equalsIgnoreCase(Proxy.Type.HTTP.name())) {
            return Proxy.Type.HTTP;//TODO https enableHttps
        } else if (getSchema().equalsIgnoreCase(Proxy.Type.SOCKS.name())) {
            return Proxy.Type.SOCKS;
        }

        throw new IllegalArgumentException("cannot determine schema type : " + getSchema());
    }
}