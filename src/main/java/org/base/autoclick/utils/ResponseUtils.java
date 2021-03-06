package org.base.autoclick.utils;

import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ResponseUtils {

    public static void parseContent(final String responseText) throws Throwable{
        Document document = Jsoup.parse(responseText);
        Elements elments = document.select("div[class=listL w642 borGray card_news] a[href]");
        String urls = elments.stream().map(e -> e.attr("class")).collect(Collectors.joining());

        System.out.print(urls);

    }

}