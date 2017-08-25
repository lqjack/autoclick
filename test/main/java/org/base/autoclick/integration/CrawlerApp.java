package org.base.autoclick.integration;

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties
public class CrawlerApp {
    public static final String TARGET_LIST = "http://credit.cngold.org/";
    private static Logger LOG = LoggerFactory.getLogger(CrawlerApp.class);
    @Autowired
    private CrawlerConfig config;

    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = SpringApplication.run(CrawlerApp.class, args);
        System.in.read();
        Runtime.getRuntime().exit(SpringApplication.exit(ctx));
    }

    @PostConstruct
    public void postConstruct() {
        LOG.info("starting crawler with config={}", config);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public PollerMetadata downloadTrigger() {
        PeriodicTrigger trigger = new PeriodicTrigger(config.getDownloadInterval());
        trigger.setFixedRate(true);
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(trigger);
        pollerMetadata.setMaxMessagesPerPoll(1);
        return pollerMetadata;
    }

    @Bean
    public MessageChannel channel1() {
        return new QueueChannel(10);
    }

    @Bean
    public MessageChannel channel2() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel channel3() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel channel4() {
        return new QueueChannel(10);
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        PeriodicTrigger trigger = new PeriodicTrigger(2, TimeUnit.MINUTES);
        trigger.setFixedRate(true);
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(trigger);
        return pollerMetadata;
    }

    @MessageEndpoint
    public static class Endpoint {
        @ServiceActivator(inputChannel = "log")
        public void log(Object payload) {
            LOG.info("entry={}", payload);
        }
    }
}