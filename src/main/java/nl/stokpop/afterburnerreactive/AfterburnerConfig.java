package nl.stokpop.afterburnerreactive;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.NoSuchElementException;

@Configuration
@Log4j2
public class AfterburnerConfig {

    private final int remotePort;
    private final String remoteHost;

    public AfterburnerConfig(
            @Value("${afterburner.remote.port:8181}") int remotePort,
            @Value("${afterburner.remote.host:http://localhost}") String remoteHost) {
        this.remotePort = remotePort;
        this.remoteHost = remoteHost;
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {

        HttpClient httpClient = HttpClient.create()
            .tcpConfiguration(client ->
                client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500)
                    .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10))
                        .addHandlerLast(new WriteTimeoutHandler(10))));

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient.wiretap(true));

        ExchangeFilterFunction filterFunction = (request, nextFilter) -> {
            log.info("WebClient request filter {}", request.url());
            return nextFilter.exchange(request);
        };

        String host = String.join(":", remoteHost, String.valueOf(remotePort));

        return builder
            .baseUrl(host)
            .clientConnector(connector)
            .filter(logRequest())
            .filter(logResponse())
            .build();
    }

    ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.info("hi request {}", request.url());
            return Mono.just(request).contextWrite(context -> context.put("stokpop-start-time", System.currentTimeMillis()));
        });
    }

    ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.info("hi response {}", response.statusCode());
            return Mono.just(response).contextWrite(context -> {
                try {
                    Long startTime = context.get("stokpop-start-time");
                    log.info("duration millis: {}", System.currentTimeMillis() - startTime);
                } catch (NoSuchElementException e) {
                    log.debug("no stokpop-start-time found in context, cannot calculate duration");
                }
                return context;
            });
        });
    }

}
