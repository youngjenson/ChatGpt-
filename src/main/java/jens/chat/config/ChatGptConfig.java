package jens.chat.config;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
@Slf4j
@ConfigurationProperties(prefix = "proxy")
public class ChatGptConfig {

    private String host;
    private Integer port;

    // 注入代理
    @Bean
    public CloseableHttpAsyncClient httpAsyncClient() {
        if(host == null || port == null) {
            return HttpAsyncClients.createHttp2Default();
        }
        log.info("host: {}, port: {}", host, port);
        HttpHost httpHost = new HttpHost(host, port);
        DefaultProxyRoutePlanner proxyRoutePlanner = new DefaultProxyRoutePlanner(httpHost);
        return HttpAsyncClients.custom().setRoutePlanner(proxyRoutePlanner).build();
    }
}
