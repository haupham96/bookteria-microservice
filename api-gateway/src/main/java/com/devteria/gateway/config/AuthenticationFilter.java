package com.devteria.gateway.config;

import com.devteria.gateway.dto.response.ApiResponse;
import com.devteria.gateway.service.IdentityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {

    IdentityService identityService;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${app.api-prefix}")
    String apiPrefix;

    @NonFinal
    static String[] publicEndPoints = {"/identity/auth/.*", "/identity/users/registration"};

    private boolean isPublicEndPoint(ServerHttpRequest request) {
        return Arrays.stream(publicEndPoints)
                .anyMatch(e -> request.getURI().getPath().matches(apiPrefix + e));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter AuthenticationFilter .......r");
        // check public endpoint
        if (isPublicEndPoint(exchange.getRequest())) {
            return chain.filter(exchange);
        }
        // get token from Authorization header
        List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeaders)) {
            return unAuthenticated(exchange.getResponse());
        }
        String token = authHeaders.getFirst().replace("Bearer ", "");

        // verify token : delegate for identity service
        return identityService.introspect(token).flatMap(res -> {
            if (res.getResult().isValid()) {
                return chain.filter(exchange);
            } else {
                return unAuthenticated(exchange.getResponse());
            }
        }).onErrorResume(throwable -> unAuthenticated(exchange.getResponse()));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    Mono<Void> unAuthenticated(ServerHttpResponse response) {
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(1401)
                .message("Unauthenticated")
                .build();
        String body = "";
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }
}
