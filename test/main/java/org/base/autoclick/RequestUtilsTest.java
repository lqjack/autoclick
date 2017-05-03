package org.base.autoclick;

import org.junit.Test;

import org.base.autoclick.utils.*;
import sun.misc.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.concurrent.*;
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
}