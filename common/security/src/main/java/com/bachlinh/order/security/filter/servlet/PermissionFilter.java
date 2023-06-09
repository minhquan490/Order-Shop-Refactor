package com.bachlinh.order.security.filter.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.PathMatcher;
import com.bachlinh.order.entity.enums.Role;
import com.bachlinh.order.entity.model.Customer;
import com.bachlinh.order.environment.Environment;
import com.bachlinh.order.exception.http.AccessDeniedException;
import com.bachlinh.order.security.filter.AbstractWebFilter;
import com.bachlinh.order.security.handler.AccessDeniedHandler;
import com.bachlinh.order.service.container.DependenciesContainerResolver;

import java.io.IOException;
import java.util.Collection;

public class PermissionFilter extends AbstractWebFilter {
    private final PathMatcher pathMatcher;
    private final Collection<String> excludePaths;
    private final String adminPath;
    private AccessDeniedHandler accessDeniedHandler;

    public PermissionFilter(DependenciesContainerResolver containerResolver, String profile, Collection<String> excludePaths, PathMatcher pathMatcher) {
        super(containerResolver.getDependenciesResolver());
        Environment environment = Environment.getInstance(profile);
        this.excludePaths = excludePaths;
        this.pathMatcher = pathMatcher;
        this.adminPath = environment.getProperty("shop.url.admin");
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        return excludePaths.stream().anyMatch(path -> pathMatcher.match(path, request.getRequestURI()));
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Customer customer = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (customer.getRole().equalsIgnoreCase(Role.ADMIN.name()) && pathMatcher.match(adminPath, request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!customer.getRole().equalsIgnoreCase(Role.ADMIN.name()) && pathMatcher.match(adminPath, request.getRequestURI())) {
            accessDeniedHandler.handle(response, new AccessDeniedException("Contact to admin for access to this url"));
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected void inject() {
        if (accessDeniedHandler == null) {
            accessDeniedHandler = getDependenciesResolver().resolveDependencies(AccessDeniedHandler.class);
        }
    }
}
