package com.bachlinh.order.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.FrameworkServlet;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.DependenciesInitialize;
import com.bachlinh.order.core.scanner.ApplicationScanner;
import com.bachlinh.order.utils.JacksonUtils;
import com.bachlinh.order.web.handler.SpringFrontRequestHandler;

import java.nio.charset.StandardCharsets;

@ActiveReflection
public class WebServlet extends FrameworkServlet {

    private final transient SpringFrontRequestHandler frontRequestHandler;
    private final ObjectMapper objectMapper = JacksonUtils.getSingleton();

    @ActiveReflection
    @DependenciesInitialize
    public WebServlet(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
        new ApplicationScanner().findComponents();
        this.frontRequestHandler = webApplicationContext.getBean(SpringFrontRequestHandler.class);
        setEnableLoggingRequestDetails(true);
    }

    @Override
    protected void doService(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        ResponseEntity<?> responseEntity = frontRequestHandler.handle(request, response);
        response.setStatus(responseEntity.getStatusCode().value());
        responseEntity.getHeaders().forEach((s, strings) -> {
            if (!s.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                strings.forEach(s1 -> response.addHeader(s, s1));
            }
        });
        String json = objectMapper.writeValueAsString(responseEntity.getBody());
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(data.length));
        response.setHeader(HttpHeaders.TRANSFER_ENCODING, "chucked");
        response.setBufferSize(data.length);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(data);
        response.flushBuffer();
    }
}
