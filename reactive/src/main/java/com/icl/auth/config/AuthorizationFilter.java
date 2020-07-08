package com.icl.auth.config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

@Component
public class AuthorizationFilter implements WebFilter {
    private Set<String> allowedURIList = new HashSet<>();

    public AuthorizationFilter() {
        this
                .addToWhitelist("/login")
                .addToWhitelist("/register")
                .addToWhitelist("/logout");
    }

    /**
     * Process the Web request and (optionally) delegate to the next.
     * Checks whether request path is in whitelist or not, if not calls checkAuthorization() method.
     * If there is no authorized user in session and request is not in whitelist, then breaks WebFilterChain.
     * {@code WebFilter} through the given {@link WebFilterChain}
     *
     * @param exchange the current server exchange
     * @param chain    provides a way to delegate to the next filter
     * @return {@code Mono<Void>} to indicate when request processing is complete
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!allowedURIList.contains(exchange.getRequest().getURI().getPath())) {

            return checkAuthorization(exchange.getSession())
                    .doOnError(error -> {
                    })
                    .then(chain.filter(exchange));
        }
        return chain.filter(exchange);
    }

    /**
     * Looks up for user attribute in session.
     *
     * @param sessionMono current {@link WebSession} session
     * @return {@link ServerResponse} with {@link HttpStatus} OK if user attribute is found in session,
     * or Mono.error if user is not authorized.
     */
    private Mono<ServerResponse> checkAuthorization(Mono<WebSession> sessionMono) {
        return sessionMono.flatMap(session -> {
            if (session.getAttribute("user") == null) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            } else {
                return ServerResponse.status(HttpStatus.OK).build();
            }
        });
    }

    /**
     * Convenient method for uri whitelist creation
     *
     * @param uri which is permitted for all users without authorization
     * @return this object
     */
    public AuthorizationFilter addToWhitelist(String uri) {
        allowedURIList.add(uri);
        return this;
    }
}
