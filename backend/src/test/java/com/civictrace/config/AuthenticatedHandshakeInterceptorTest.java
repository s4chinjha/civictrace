package com.civictrace.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuthenticatedHandshakeInterceptorTest {

    @Test
    void rejectsWebSocketHandshakesWithoutAnAuthenticatedPrincipal() throws Exception {
        HandshakeInterceptor interceptor = new AuthenticatedHandshakeInterceptor();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/ws");
        ServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        assertThat(interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), new HashMap<>())).isFalse();
    }
}
