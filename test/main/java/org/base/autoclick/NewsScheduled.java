package org.base.autoclick;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import us.codecraft.webmagic.Spider;

//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;

//@Component
public class NewsScheduled {
    @Autowired
    private NewsPipeline newsPipeline;

    @Scheduled(cron = "0/20 * * * * ? ")//从0点开始,每2个小时执行一次
    public void goldenProcessor() {
        System.out.println("----开始执行简书定时任务");
        Spider spider = Spider.create(new GoldenProcessor());
        spider.addUrl("http://www.cngold.org");
        spider.addPipeline(newsPipeline);
        spider.thread(5);
        spider.setExitWhenComplete(true);
        spider.start();
        spider.stop();
    }

}