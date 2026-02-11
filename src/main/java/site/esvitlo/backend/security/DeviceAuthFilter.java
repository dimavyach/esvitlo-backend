package site.esvitlo.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class DeviceAuthFilter extends OncePerRequestFilter {

    public static final String DEVICE_KEY_ATTR = "deviceKey";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI().equals("/api/v1/ping")) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader("Authorization");

        if (auth == null || !auth.startsWith("Device ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String deviceKey = auth.substring(7).trim();

        if (deviceKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // ❗ НЕ перевіряємо БД тут
        // ❗ ТІЛЬКИ валідність заголовка
        request.setAttribute(DEVICE_KEY_ATTR, deviceKey);
        filterChain.doFilter(request, response);
    }
}

