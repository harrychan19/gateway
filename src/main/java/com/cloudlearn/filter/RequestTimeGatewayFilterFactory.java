package com.cloudlearn.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * @author hushihai
 * @version V1.0, 2019/1/28
 */
@Component
public class RequestTimeGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestTimeGatewayFilterFactory.Config> {


    private static final Log log = LogFactory.getLog(GatewayFilter.class);
    private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";
    private static final String KEY = "withParams";

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList(KEY);
    }

    public RequestTimeGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());
            Mono<Void> mono = null;
            try {
                mono = chain.filter(exchange).then(
                        Mono.fromRunnable(() -> {
                            Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
                            StringBuilder sb = new StringBuilder(exchange.getRequest().getURI().getRawPath())
                                    .append(": ")
                                    .append(System.currentTimeMillis() - startTime)
                                    .append("ms");
                            if (config.isWithParams()) {
                                MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
                                sb.append(" params:").append(queryParams);
                            }
                            log.info(sb.toString());
                        })
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mono;
        };
    }


    public static class Config {

        private boolean withParams;

        public boolean isWithParams() {
            return withParams;
        }

        public void setWithParams(boolean withParams) {
            this.withParams = withParams;
        }

    }
}