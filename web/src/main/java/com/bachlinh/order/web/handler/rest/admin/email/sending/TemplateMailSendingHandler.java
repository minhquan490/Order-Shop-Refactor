package com.bachlinh.order.web.handler.rest.admin.email.sending;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.RouteProvider;
import com.bachlinh.order.core.enums.RequestMethod;
import com.bachlinh.order.core.http.Payload;
import com.bachlinh.order.entity.model.Customer;
import com.bachlinh.order.handler.controller.AbstractController;
import com.bachlinh.order.web.dto.form.admin.email.sending.TemplateMailSendingForm;
import com.bachlinh.order.web.service.business.EmailTemplateSendingService;

import java.util.HashMap;
import java.util.Map;

@RouteProvider
@ActiveReflection
@NoArgsConstructor(onConstructor = @__(@ActiveReflection))
public class TemplateMailSendingHandler extends AbstractController<Map<String, Object>, TemplateMailSendingForm> {
    private String url;
    private EmailTemplateSendingService emailTemplateSendingService;

    @Override
    @ActiveReflection
    protected Map<String, Object> internalHandler(Payload<TemplateMailSendingForm> request) {
        var customer = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        emailTemplateSendingService.processTemplateAndSend(request.data(), customer, getEnvironment());
        var resp = new HashMap<String, Object>(2);
        resp.put("status", HttpStatus.OK.value());
        resp.put("messages", new String[]{"Sending email complete"});
        return resp;
    }

    @Override
    protected void inject() {
        var resolver = getContainerResolver().getDependenciesResolver();
        if (emailTemplateSendingService == null) {
            emailTemplateSendingService = resolver.resolveDependencies(EmailTemplateSendingService.class);
        }
    }

    @Override
    public String getPath() {
        if (url == null) {
            url = getEnvironment().getProperty("shop.url.admin.email-template.sending");
        }
        return url;
    }

    @Override
    public RequestMethod getRequestMethod() {
        return RequestMethod.POST;
    }
}
