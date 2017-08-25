package org.base.autoclick.integration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class FreeProxyResolver {

    public static final Pattern IP_PATTERN = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");

    public static final Pattern IP_PORT_PATTERN = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)");

    public static final String WEB_SITE = "https://free-proxy-list.com";

    @Autowired
    private RestTemplate template;

    @InboundChannelAdapter(value = "free-fetch", poller = @Poller("downloadTrigger"))
    public ResponseEntity<String> proxyList() {

        try {
            SSLUtil.turnOffSslChecking();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        ResponseEntity<String> proxyContent = template.getForEntity(WEB_SITE, String.class);
        return proxyContent;

    }

    @Splitter(inputChannel = "free-fetch", outputChannel = "free-filter")
    public List<Element> scrape(ResponseEntity<String> payload) {
        String html = payload.getBody();
        final Document htmlDoc = Jsoup.parse(html);
        Elements anchorNodes = htmlDoc.select("div.wrapper > div.container >  div.content-wrapper > div > div > table");

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

    @Filter(inputChannel = "free-filter", outputChannel = "free-transform")
    public boolean filter(Element payload) {
        String href = payload.attr("alt");
        Matcher matcher = IP_PORT_PATTERN.matcher(href);
        return !href.isEmpty() && matcher.matches();
    }

    @Transformer(inputChannel = "free-transform", outputChannel = "log")
    public DumpEntry convert(Element payload) throws ParseException, InterruptedException {
        Elements href = payload.select("a[alt]");

        String title = href.attr("title");

        TimeUnit.MINUTES.sleep(2);

        String[] ipAndPort = title.split(":");

        System.setProperty("http.proxyHost", ipAndPort[0]);

        System.setProperty("http.proxyPort", ipAndPort[1]);

        return new DumpEntry(new Date(), "free", title, "");
    }

}
