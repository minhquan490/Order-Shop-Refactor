package com.bachlinh.order.web.service.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bachlinh.order.web.dto.form.admin.customer.CustomerCreateForm;
import com.bachlinh.order.web.dto.form.admin.customer.CustomerDeleteForm;
import com.bachlinh.order.web.dto.form.admin.customer.CustomerUpdateForm;
import com.bachlinh.order.web.dto.resp.CustomerInfoResp;
import com.bachlinh.order.web.dto.resp.CustomerResp;
import com.bachlinh.order.web.dto.resp.MyInfoResp;

public interface CustomerService {

    MyInfoResp getMyInfo(String customerId);

    Page<CustomerResp> getFullInformationOfCustomer(Pageable pageable);

    CustomerResp saveCustomer(CustomerCreateForm customerCreateForm);

    CustomerResp updateCustomer(CustomerUpdateForm customerUpdateForm);

    CustomerResp deleteCustomer(CustomerDeleteForm customerDeleteForm);

    CustomerInfoResp getCustomerInfo(String customerId);
}
