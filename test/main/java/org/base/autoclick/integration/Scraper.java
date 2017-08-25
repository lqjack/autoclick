package org.base.autoclick.integration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.annotation.Transformer;
import org.springframework.web.client.RestTemplate;

@MessageEndpoint
public class Scraper {

    @Autowired
    private RestTemplate template;

    @InboundChannelAdapter(value = "gold-fetch", poller = @Poller("downloadTrigger"))
    public ResponseEntity<String> proxyList() {

        try {
            SSLUtil.turnOffSslChecking();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        ResponseEntity<String> proxyContent = template.getForEntity(CrawlerApp.TARGET_LIST, String.class);
        return proxyContent;

    }

    @Splitter(inputChannel = "gold-fetch", outputChannel = "gold-transform")
    public List<Element> scrape(ResponseEntity<String> payload) {
        String html = payload.getBody();
        final Document htmlDoc = Jsoup.parse(html);
        Elements anchorNodes = htmlDoc.select("div > div.w1200 >  div > div > div > div.fr > ul");

        final List<Element> anchorList = new ArrayList<>();
        anchorNodes.traverse(new NodeVisitor() {
            @Override
            public void head(org.jsoup.nodes.Node node, int depth) {
                if (node instanceof org.jsoup.nodes.Element) {
                    Element e = (Element) node;
                    anchorList.add(e);
                }
            }

            @Override
            public void tail(Node node, int depth) {
            }
        });

        return anchorList;
    }

    @Filter(inputChannel = "gold-filter", outputChannel = "gold-item")
    public boolean filter(Element payload) {
        return payload.attr("href").length() > 0;
    }

    @Transformer(inputChannel = "gold-transform", outputChannel = "log")
    public String convert(Element payload) throws ParseException, InterruptedException {
        Elements href = payload.select("a[href]");

        String title = href.attr("title");
        String href1 = href.attr("href");

        DumpEntry entry = new DumpEntry(new Date(), title, href1, "");

        try {
            SSLUtil.turnOffSslChecking();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        TimeUnit.MINUTES.sleep(1);

        return template.getForEntity(entry.getRef(), String.class).getBody();
    }

}