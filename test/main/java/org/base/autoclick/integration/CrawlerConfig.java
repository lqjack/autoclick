package org.base.autoclick.integration;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class CrawlerConfig {
    private static final String DEFAULT_URL = CrawlerApp.TARGET_LIST;

    private static final long DEFAULT_DOWNLOAD_INTERVAL = TimeUnit.HOURS.toMillis(1);
    private String url = DEFAULT_URL;
    private long downloadInterval = DEFAULT_DOWNLOAD_INTERVAL;

    private List<String> proxies;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDownloadInterval() {
        return downloadInterval;
    }

    public void setDownloadInterval(long downloadInterval) {
        this.downloadInterval = downloadInterval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CrawlerConfig that = (CrawlerConfig) o;

        if (downloadInterval != that.downloadInterval)
            return false;
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (int) (downloadInterval ^ (downloadInterval >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "CrawlerConfig{" +
            "url='" + url + '\'' +
            ", downloadInterval=" + downloadInterval +
            '}';
    }
}