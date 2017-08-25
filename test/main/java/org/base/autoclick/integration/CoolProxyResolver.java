package org.base.autoclick.integration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
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
public class CoolProxyResolver {

    public static final String WEB_SITE = "http://www.cool-proxy.net/proxies/http_proxy_list/sort:score/direction:desc";

    @Autowired
    private RestTemplate template;

    public static void main(String[] args) {
        String ip = "81.128.165.5";

        byte[] en = Base64.getEncoder().encode(ip.getBytes());

        String targetIp = new String(Base64.getDecoder().decode(en));

        System.out.println(targetIp);

        System.out.println(new String(Base64.getDecoder().decode("ODEuMTI4LjE2NS41")));

        System.out.println((int) 'a');
        System.out.println((int) 'A');
    }

    @InboundChannelAdapter(value = "cool-fetch", poller = @Poller("downloadTrigger"))
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

    @Splitter(inputChannel = "cool-fetch", outputChannel = "cool-filter")
    public List<Element> scrape(ResponseEntity<String> payload) {
        String html = payload.getBody();
        final Document htmlDoc = Jsoup.parse(html);
        Elements anchorNodes = htmlDoc.select("div#container > div#main > table");

        anchorNodes = anchorNodes.select("td[style=text-align:left; font-weight:bold;]");

        final List<Element> anchorList = new ArrayList<>();
        anchorNodes.traverse(new NodeVisitor() {
            @Override
            public void head(org.jsoup.nodes.Node node, int depth) {
                if (node instanceof org.jsoup.nodes.Element) {
                    Element e = (Element) node;
                    StringBuilder raw = new StringBuilder();
                    for (Element it : e.select("script")) {
                        for (DataNode dn : it.dataNodes()) {
                            String data = dn.getWholeData();
                            raw.append(data);
                        }
                    }
                    String decode = decode(raw.toString());
                    e.select("script").remove();
                    Element sibling = e.nextElementSibling();
                    String port = sibling.text();
                    e.appendText(decode + ":" + port);
                    anchorList.add(e);
                }
            }

            @Override
            public void tail(Node node, int depth) {
            }
        });

        return anchorList;
    }

    private String decode(String source) {
        source = source.substring(source.indexOf("\"") + 1, source.lastIndexOf("\""));
        StringBuilder charBuilder = new StringBuilder();
        for (char c : source.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {

                char next = c;
                if (c >= 'A' && c <= 'Z' && (char) (c + 32) <= 'z') {
                    next = (char) (c + 32);
                }
                if (next < 'n') {
                    c += 13;
                } else {
                    c -= 13;
                }

            }
            charBuilder.append(c);
        }

        source = charBuilder.toString();
        byte[] decode = Base64.getDecoder().decode(source.getBytes());
        String content = new String(decode);
        return content;
    }

    @Filter(inputChannel = "cool-filter", outputChannel = "cool-transform")
    public boolean filter(Element payload) {
        return true;
    }

    @Transformer(inputChannel = "cool-transform", outputChannel = "log")
    public DumpEntry convert(Element payload) throws ParseException, InterruptedException {

        String title = payload.text();

        String[] ipAndPort = title.split(":");

        TimeUnit.MINUTES.sleep(1);

        System.setProperty("http.proxyHost", ipAndPort[0]);

        System.setProperty("http.proxyPort", ipAndPort[1]);

        return new DumpEntry(new Date(), "cool", title, "");
    }

}
