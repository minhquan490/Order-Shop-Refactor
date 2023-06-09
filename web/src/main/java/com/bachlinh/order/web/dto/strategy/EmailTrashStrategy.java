package com.bachlinh.order.web.dto.strategy;

import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.dto.strategy.AbstractDtoStrategy;
import com.bachlinh.order.entity.model.EmailTrash;
import com.bachlinh.order.environment.Environment;
import com.bachlinh.order.service.container.DependenciesResolver;
import com.bachlinh.order.web.dto.resp.EmailTrashResp;

@ActiveReflection
public class EmailTrashStrategy extends AbstractDtoStrategy<EmailTrashResp, EmailTrash> {

    @ActiveReflection
    public EmailTrashStrategy(DependenciesResolver dependenciesResolver, Environment environment) {
        super(dependenciesResolver, environment);
    }

    @Override
    protected void beforeConvert(EmailTrash source, Class<EmailTrashResp> type) {
        // Do nothing
    }

    @Override
    protected EmailTrashResp doConvert(EmailTrash source, Class<EmailTrashResp> type) {
        var resp = new EmailTrashResp();
        resp.setId(source.getId().toString());
        resp.setEmails(source.getEmails()
                .stream()
                .map(email -> {
                    var respEmail = new EmailTrashResp.Email();
                    respEmail.setId(email.getId());
                    respEmail.setTitle(email.getTitle());
                    respEmail.setContent(email.getContent());
                    return respEmail;
                })
                .toList()
                .toArray(new EmailTrashResp.Email[0]));
        return resp;
    }

    @Override
    protected void afterConvert(EmailTrash source, Class<EmailTrashResp> type) {
        // Do nothing
    }

    @Override
    public Class<EmailTrashResp> getTargetType() {
        return EmailTrashResp.class;
    }
}
