package org.base.autoclick.utils;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by liu on 2017/5/3.
 */
public class ProxyManager {

    private Set<URL> proxyConfigUrls;

    private final Object lock = new Object();

    public void add(String... url) throws MalformedURLException {
        Set<URL> urls = Stream.of(url)
                .filter(u->u!=null)
                .map(u -> {
                    try {
                        return new URL(u);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                })
                .filter(url1 -> url1 != null)
                .collect(Collectors.toSet());
        synchronized (lock) {
            proxyConfigUrls.addAll(urls);
        }
    }

    public void add(URL... url) {
        Set<URL> urls = Stream.of(url)
                .filter(u->u!=null)
                .collect(Collectors.toSet());
        synchronized (lock) {
            proxyConfigUrls.addAll(urls);
        }
    }


    public Set<Proxy> buildProxy() {
        Set<Proxy> proxies = new HashSet<>();

        //connect to the server to fetch the proxy config (host and port)
        //


        return proxies;

    }



}
