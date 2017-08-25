package org.base.autoclick.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.web.client.RestTemplate;

@MessageEndpoint
public class Downloader {
    @Autowired
    private CrawlerConfig config;

    @Autowired
    private RestTemplate template;

    //    @InboundChannelAdapter(value = "fetch", poller = @Poller("downloadTrigger"))
    public ResponseEntity<String> download() {
        String url = config.getUrl();
        try {
            SSLUtil.turnOffSslChecking();
        } catch (Exception e) {

        }
        ResponseEntity<String> entity = template.getForEntity(url, String.class);
        return entity;
    }

}