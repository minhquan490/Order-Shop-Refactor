package com.bachlinh.order.web.handler.rest.admin.index;

import lombok.NoArgsConstructor;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.RouteProvider;
import com.bachlinh.order.core.enums.RequestMethod;
import com.bachlinh.order.core.http.Payload;
import com.bachlinh.order.handler.controller.AbstractController;
import com.bachlinh.order.web.dto.resp.AnalyzeCustomerNewInMonthResp;
import com.bachlinh.order.web.service.business.CustomerAnalyzeService;

@ActiveReflection
@RouteProvider
@NoArgsConstructor(onConstructor = @__({@ActiveReflection}))
public class CustomerNewInMonthHandler extends AbstractController<AnalyzeCustomerNewInMonthResp, Void> {
    private CustomerAnalyzeService customerAnalyzeService;
    private String url;

    @Override
    @ActiveReflection
    protected AnalyzeCustomerNewInMonthResp internalHandler(Payload<Void> request) {
        return customerAnalyzeService.analyzeCustomerNewInMonth();
    }

    @Override
    protected void inject() {
        var resolver = getContainerResolver().getDependenciesResolver();
        if (customerAnalyzeService == null) {
            customerAnalyzeService = resolver.resolveDependencies(CustomerAnalyzeService.class);
        }
    }

    @Override
    public String getPath() {
        if (url == null) {
            url = getEnvironment().getProperty("shop.url.admin.customer.customer-in-month-analyze");
        }
        return url;
    }

    @Override
    public RequestMethod getRequestMethod() {
        return RequestMethod.GET;
    }
}
