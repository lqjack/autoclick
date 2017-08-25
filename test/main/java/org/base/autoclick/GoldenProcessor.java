package org.base.autoclick;

import java.util.List;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

public class GoldenProcessor implements PageProcessor {

    public static final String TARGET_LIST = "http://cngold.org/";
    private Site site = Site.me()
        .setDomain(TARGET_LIST)
        .setSleepTime(100)
        .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");

    public static void main(String[] args) {
        Spider spider = Spider.create(new GoldenProcessor());
        spider.addUrl(TARGET_LIST);
        spider.addPipeline(new NewsPipeline());
        spider.thread(5);
        spider.setExitWhenComplete(true);
        spider.start();
    }

    @Override
    public void process(Page page) {
        if (page.getUrl().regex(TARGET_LIST).match()) {
            List<Selectable> list = page.getHtml().xpath("//ul[@class='article-TARGET_LIST thumbnails']/li").nodes();
            for (Selectable s : list) {
                String title = s.xpath("//div/h4/a/text()").toString();
                String link = s.xpath("//div/h4").links().toString();
                News news = new News();
                news.setTitle(title);
                news.setInfo(title);
                news.setLink(link);
                news.setSources(new Sources(5));
                page.putField("news" + title, news);
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}
