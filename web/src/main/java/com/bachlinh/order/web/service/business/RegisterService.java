package com.bachlinh.order.web.service.business;

import com.bachlinh.order.web.dto.form.RegisterForm;
import com.bachlinh.order.web.dto.resp.RegisterResp;

public interface RegisterService {

    RegisterResp register(RegisterForm registerForm);
}
