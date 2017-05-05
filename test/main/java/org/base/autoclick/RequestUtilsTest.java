package org.base.autoclick;

import org.base.autoclick.utils.ProxyConfig;
import org.base.autoclick.utils.ProxyManager;
import org.base.autoclick.utils.RequestUtils;
import org.base.autoclick.utils.ResponseUtils;
import org.junit.Test;

import java.net.URL;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RequestUtilsTest {

    final int THREAD_SIZE = 10;
    final int TIME_OUT = 10;

    @Test
    public void testDownload() throws Throwable {
        String site = "http://credit.cngold.org";

        CompletionService<String> completionService=new ExecutorCompletionService<>(Executors.newFixedThreadPool(THREAD_SIZE));

        completionService.submit(RequestUtils.request(new URL(site),TIME_OUT));

        String response = completionService.take().get();

        ResponseUtils.parseContent(response);

    }

    @Test
    public void testVisitPageUnderDifferProxy() throws InterruptedException {
        ProxyManager pm = new ProxyManager();

        Set<ProxyConfig> proxies = pm.buildProxyConfig();

        URL url = null;
        proxies.parallelStream()
                .map(proxyConfig -> {
                    try {
                        return RequestUtils.request(url, TIME_OUT, proxyConfig);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        return null;
                    }
                })
//                .filter(head == 200)
                .collect(Collectors.counting());


    }
}