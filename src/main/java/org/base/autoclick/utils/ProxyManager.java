package org.base.autoclick.utils;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by liu on 2017/5/3.
 */
public class ProxyManager {

    private final Object lock = new Object();
    private Set<URL> proxyConfigUrls;

    /**
     * @param url i.e http://www.google.com/path
     * @throws MalformedURLException
     */
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


    public Set<ProxyConfig> buildProxyConfig() throws InterruptedException {
        Set<ProxyConfig> proxiesConfigs = new ConcurrentSkipListSet<>();

        CountDownLatch latch = new CountDownLatch(proxyConfigUrls.size());

        CompletionService<String> completionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));

        proxyConfigUrls
                .parallelStream()
                .map(url -> {
                    Future<String> future = null;
                    try {
                        future = completionService.submit(RequestUtils.request(url, 1_000));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        latch.countDown();
                        future.cancel(true);
                    }
                    return future;
                }).close();

        latch.await();

        while (latch.getCount() > 0) {
            Future<String> responseContent = completionService.poll();
            if (responseContent == null) {
                continue;
            }
            parseResponse(responseContent, proxiesConfigs);
            latch.countDown();
        }

        return proxiesConfigs;

    }

    private void parseResponse(Future<String> responseContent, Set<ProxyConfig> proxiesConfigs) {


    }

}
