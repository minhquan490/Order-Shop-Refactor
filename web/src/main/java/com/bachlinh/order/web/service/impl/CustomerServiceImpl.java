package com.bachlinh.order.web.service.impl;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.DependenciesInitialize;
import com.bachlinh.order.annotation.ServiceComponent;
import com.bachlinh.order.core.http.NativeRequest;
import com.bachlinh.order.dto.DtoMapper;
import com.bachlinh.order.entity.EntityFactory;
import com.bachlinh.order.entity.context.spi.FieldUpdated;
import com.bachlinh.order.entity.enums.Gender;
import com.bachlinh.order.entity.model.Cart;
import com.bachlinh.order.entity.model.Customer;
import com.bachlinh.order.entity.model.Customer_;
import com.bachlinh.order.entity.model.EmailTemplate;
import com.bachlinh.order.entity.model.EmailTrash;
import com.bachlinh.order.entity.model.LoginHistory;
import com.bachlinh.order.entity.model.RefreshToken;
import com.bachlinh.order.environment.Environment;
import com.bachlinh.order.exception.http.InvalidTokenException;
import com.bachlinh.order.exception.http.TemporaryTokenExpiredException;
import com.bachlinh.order.exception.http.UnAuthorizationException;
import com.bachlinh.order.exception.system.common.CriticalException;
import com.bachlinh.order.mail.model.GmailMessage;
import com.bachlinh.order.mail.service.GmailSendingService;
import com.bachlinh.order.repository.CustomerInfoChangerHistoryRepository;
import com.bachlinh.order.repository.CustomerRepository;
import com.bachlinh.order.repository.EmailTemplateRepository;
import com.bachlinh.order.repository.EmailTrashRepository;
import com.bachlinh.order.repository.LoginHistoryRepository;
import com.bachlinh.order.repository.query.Join;
import com.bachlinh.order.security.auth.spi.RefreshTokenHolder;
import com.bachlinh.order.security.auth.spi.TemporaryTokenGenerator;
import com.bachlinh.order.security.auth.spi.TokenManager;
import com.bachlinh.order.security.handler.ClientSecretHandler;
import com.bachlinh.order.utils.JacksonUtils;
import com.bachlinh.order.utils.ResourceUtils;
import com.bachlinh.order.web.dto.form.admin.customer.CustomerCreateForm;
import com.bachlinh.order.web.dto.form.admin.customer.CustomerDeleteForm;
import com.bachlinh.order.web.dto.form.admin.customer.CustomerUpdateForm;
import com.bachlinh.order.web.dto.form.common.LoginForm;
import com.bachlinh.order.web.dto.form.customer.RegisterForm;
import com.bachlinh.order.web.dto.resp.AnalyzeCustomerNewInMonthResp;
import com.bachlinh.order.web.dto.resp.CustomerInfoResp;
import com.bachlinh.order.web.dto.resp.CustomerResp;
import com.bachlinh.order.web.dto.resp.LoginResp;
import com.bachlinh.order.web.dto.resp.MyInfoResp;
import com.bachlinh.order.web.dto.resp.RegisterResp;
import com.bachlinh.order.web.dto.resp.RevokeTokenResp;
import com.bachlinh.order.web.service.business.CustomerAnalyzeService;
import com.bachlinh.order.web.service.business.CustomerSearchingService;
import com.bachlinh.order.web.service.business.ForgotPasswordService;
import com.bachlinh.order.web.service.business.LoginService;
import com.bachlinh.order.web.service.business.LogoutService;
import com.bachlinh.order.web.service.business.RegisterService;
import com.bachlinh.order.web.service.business.RevokeAccessTokenService;
import com.bachlinh.order.web.service.common.CustomerService;
import com.bachlinh.order.web.service.common.EmailFolderService;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@ServiceComponent
@ActiveReflection
@RequiredArgsConstructor(onConstructor = @__({@ActiveReflection, @DependenciesInitialize}))
// @formatter:off
public class CustomerServiceImpl implements CustomerService,
                                            LoginService,
                                            RegisterService,
                                            LogoutService,
                                            ForgotPasswordService,
                                            CustomerAnalyzeService,
                                            RevokeAccessTokenService,
                                            CustomerSearchingService { // @formatter:on
    private final Map<String, TempTokenHolder> tempTokenMap = new ConcurrentHashMap<>();

    private final PasswordEncoder passwordEncoder;
    private final EntityFactory entityFactory;
    private final CustomerRepository customerRepository;
    private final TokenManager tokenManager;
    private final LoginHistoryRepository loginHistoryRepository;
    private final ClientSecretHandler clientSecretHandler;
    private final GmailSendingService gmailSendingService;
    private final TemporaryTokenGenerator tokenGenerator;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailTrashRepository emailTrashRepository;
    private final EmailFolderService emailFolderService;
    private final CustomerInfoChangerHistoryRepository customerInfoChangerHistoryRepository;
    private final DtoMapper dtoMapper;
    private final Executor executor;
    private String urlResetPassword;
    private String botEmail;

    @Override
    public MyInfoResp getMyInfo(String customerId) {
        Customer customer = customerRepository.getCustomerById(customerId, false);
        return dtoMapper.map(customer, MyInfoResp.class);
    }

    @Override
    public Page<CustomerResp> getFullInformationOfCustomer(Pageable pageable) {
        return new PageImpl<>(
                customerRepository.getAll(pageable, Sort.by(Customer_.ID))
                        .stream()
                        .map(customer -> dtoMapper.map(customer, CustomerResp.class))
                        .toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public CustomerResp saveCustomer(CustomerCreateForm customerCreateForm) {
        var customer = dtoMapper.map(customerCreateForm, Customer.class);
        customer = customerRepository.saveCustomer(customer);
        emailFolderService.createDefaultFolders(customer);
        return dtoMapper.map(customer, CustomerResp.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public CustomerResp updateCustomer(CustomerUpdateForm customerUpdateForm) {
        var customer = customerRepository.getCustomerById(customerUpdateForm.getId(), false);
        var oldCustomer = customer.clone();
        customer.setFirstName(customerUpdateForm.getFirstName());
        customer.setLastName(customerUpdateForm.getLastName());
        customer.setPhoneNumber(customerUpdateForm.getPhone());
        customer.setEmail(customerUpdateForm.getEmail());
        customer.setGender(Gender.of(customerUpdateForm.getGender()).name());
        customer.setUsername(customerUpdateForm.getUsername());
        customer.setUpdatedFields(findUpdatedFields((Customer) oldCustomer, customer));
        customer = customerRepository.updateCustomer(customer);
        return dtoMapper.map(customer, CustomerResp.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public CustomerResp deleteCustomer(CustomerDeleteForm customerDeleteForm) {
        var customer = customerRepository.getCustomerById(customerDeleteForm.customerId(), true);
        customerRepository.deleteCustomer(customer);
        return dtoMapper.map(customer, CustomerResp.class);
    }

    @Override
    public CustomerInfoResp getCustomerInfo(String customerId) {
        var history = Join.builder().attribute(Customer_.HISTORIES).type(JoinType.INNER).build();
        var voucher = Join.builder().attribute(Customer_.ASSIGNED_VOUCHERS).type(JoinType.INNER).build();
        var order = Join.builder().attribute(Customer_.ORDERS).type(JoinType.INNER).build();
        var address = Join.builder().attribute(Customer_.ADDRESSES).type(JoinType.INNER).build();
        var media = Join.builder().attribute(Customer_.CUSTOMER_MEDIA).type(JoinType.INNER).build();
        var customer = customerRepository.getCustomerUseJoin(customerId, Arrays.asList(history, voucher, order, address, media));
        var histories = loginHistoryRepository.getHistories(customer);
        var changeHistories = customerInfoChangerHistoryRepository.getHistoriesChangeOfCustomer(customer);
        var result = dtoMapper.map(customer, CustomerInfoResp.class);
        result.setLoginHistories(histories.stream()
                .map(loginHistory -> {
                    var historyResp = new CustomerInfoResp.LoginHistory();
                    historyResp.setId(loginHistory.getId().toString());
                    historyResp.setLastLoginTime(loginHistory.getLastLoginTime().toString());
                    historyResp.setLoginIp(loginHistory.getLoginIp());
                    historyResp.setSuccess(loginHistory.getSuccess());
                    return historyResp;
                })
                .toList()
                .toArray(new CustomerInfoResp.LoginHistory[0]));
        result.setInfoChangeHistories(changeHistories.stream()
                .map(customerInfoChangeHistory -> {
                    var infoChangeHistory = new CustomerInfoResp.InfoChangeHistory();
                    infoChangeHistory.setId(customerInfoChangeHistory.getId());
                    infoChangeHistory.setFieldName(customerInfoChangeHistory.getFieldName());
                    infoChangeHistory.setTimeUpdate(customerInfoChangeHistory.getTimeUpdate().toString());
                    infoChangeHistory.setOldValue(customerInfoChangeHistory.getOldValue());
                    return infoChangeHistory;
                })
                .toList()
                .toArray(new CustomerInfoResp.InfoChangeHistory[0]));
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public LoginResp login(LoginForm loginForm, NativeRequest<?> request) {
        Customer customer = customerRepository.getCustomerByUsername(loginForm.username());
        if (customer == null) {
            return new LoginResp(null, null, false);
        }
        if (!passwordEncoder.matches(loginForm.password(), customer.getPassword())) {
            saveHistory(customer, request);
            return new LoginResp(null, null, false);
        }
        tokenManager.encode("customerId", customer.getId());
        tokenManager.encode("username", customer.getUsername());
        String accessToken = tokenManager.getTokenValue();
        RefreshToken refreshToken = tokenManager.getRefreshTokenGenerator().generateToken(customer.getId(), customer.getUsername());
        saveHistory(customer, request);
        return new LoginResp(refreshToken.getRefreshTokenValue(), accessToken, true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public RegisterResp register(RegisterForm registerForm) {
        Customer customer = registerForm.toCustomer(entityFactory, passwordEncoder);
        Cart cart = entityFactory.getEntity(Cart.class);
        cart.setCustomer(customer);
        customer.setCart(cart);
        try {
            customer = customerRepository.saveCustomer(customer);
            emailFolderService.createDefaultFolders(customer);
            var trash = entityFactory.getEntity(EmailTrash.class);
            trash.setCustomer(customer);
            customer.setEmailTrash(trash);
            emailTrashRepository.saveEmailTrash(trash);
            return new RegisterResp("Register success", false);
        } catch (Exception e) {
            return new RegisterResp("Register failure", true);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public boolean logout(Customer customer, String secret) {
        customer = customerRepository.getCustomerById(customer.getId(), false);
        clientSecretHandler.removeClientSecret(customer.getRefreshToken().getRefreshTokenValue(), secret);
        customer.setRefreshToken(null);
        return customerRepository.updateCustomer(customer) != null;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public void saveHistory(Customer customer, NativeRequest<?> request) {
        executor.execute(() -> {
            LoginHistory loginHistory = entityFactory.getEntity(LoginHistory.class);
            loginHistory.setCustomer(customer);
            loginHistory.setLastLoginTime(Timestamp.from(Instant.now()));
            loginHistory.setLoginIp(request.getCustomerIp());
            loginHistory.setSuccess(true);
            loginHistoryRepository.saveHistory(loginHistory);
        });
    }

    @Override
    public void sendEmailResetPassword(String email) {
        String tempToken = tokenGenerator.generateTempToken();
        TempTokenHolder holder = new TempTokenHolder(LocalDateTime.now(), email);
        tempTokenMap.put(tempToken, holder);
        EmailTemplate emailTemplate = emailTemplateRepository.getDefaultEmailTemplate("Reset password");
        if (emailTemplate != null) {
            GmailMessage model = new GmailMessage(botEmail);
            model.setBody(MessageFormat.format(emailTemplate.getContent(), urlResetPassword));
            model.setCharset(StandardCharsets.UTF_8);
            model.setToAddress(email);
            model.setSubject("Reset your password");
            model.setContentType(MediaType.TEXT_PLAIN_VALUE);
            gmailSendingService.send(model);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public void resetPassword(String temporaryToken, String newPassword) {
        TempTokenHolder holder = tempTokenMap.get(temporaryToken);
        if (holder == null) {
            throw new InvalidTokenException("Your token is invalid");
        }
        LocalDateTime expireTime = tempTokenMap.get(temporaryToken).expireTime();
        if (expireTime.isAfter(LocalDateTime.now())) {
            throw new TemporaryTokenExpiredException("Temp token is expiry");
        }
        Customer customer = customerRepository.getCustomerByEmail(holder.email());
        String hashedPassword = passwordEncoder.encode(newPassword);
        customer.setPassword(hashedPassword);
        customerRepository.updateCustomer(customer);
        tempTokenMap.remove(temporaryToken, holder);
    }

    private Collection<FieldUpdated> findUpdatedFields(Customer oldCustomer, Customer newCustomer) {
        var fields = new ArrayList<FieldUpdated>();
        if (!oldCustomer.getPhoneNumber().equals(newCustomer.getPhoneNumber())) {
            fields.add(new FieldUpdated("PHONE_NUMBER", oldCustomer.getPhoneNumber()));
        }
        if (!oldCustomer.getEmail().equals(newCustomer.getEmail())) {
            fields.add(new FieldUpdated("EMAIL", oldCustomer.getEmail()));
        }
        if (!oldCustomer.getRole().equals(newCustomer.getRole())) {
            fields.add(new FieldUpdated("ROLE", oldCustomer.getRole()));
        }
        if (!oldCustomer.getOrderPoint().equals(newCustomer.getOrderPoint())) {
            fields.add(new FieldUpdated("ORDER_POINT", oldCustomer.getOrderPoint().toString()));
        }
        if (oldCustomer.isActivated() != newCustomer.isActivated()) {
            fields.add(new FieldUpdated("ENABLED", String.valueOf(oldCustomer.isActivated())));
        }
        return fields;
    }

    @Override
    public AnalyzeCustomerNewInMonthResp analyzeCustomerNewInMonth() {
        var template = "select t.* from (select count(c.id) as first, ({0}) as second, ({1}) as third, ({2}) as fourth, ({3}) as last from Customer c where c.created_date between :firstStart and :firstEnd) as t";
        var secondStatement = "select count(c.id) from Customer c where c.created_date between :secondStart and :secondEnd";
        var thirdStatement = "select count(c.id) from Customer c where c.created_date between :thirdStart and :thirdEnd";
        var fourthStatement = "select count(c.id) from Customer c where c.created_date between :fourthStart and :fourthEnd";
        var lastStatement = "select count(c.id) from Customer c where c.created_date between :lastStart and :lastEnd";
        var query = MessageFormat.format(template, secondStatement, thirdStatement, fourthStatement, lastStatement);
        var attributes = new HashMap<String, Object>(10);
        var now = LocalDateTime.now();
        var firstParam = Timestamp.valueOf(now.plusWeeks(-5));
        var secondParam = Timestamp.valueOf(now.plusWeeks(-4));
        var thirdParam = Timestamp.valueOf(now.plusWeeks(-3));
        var fourthParam = Timestamp.valueOf(now.plusWeeks(-2));
        var fifthParam = Timestamp.valueOf(now.plusWeeks(-1));
        attributes.put("firstStart", firstParam);
        attributes.put("firstEnd", secondParam);
        attributes.put("secondStart", secondParam);
        attributes.put("secondEnd", thirdParam);
        attributes.put("thirdStart", thirdParam);
        attributes.put("thirdEnd", fourthParam);
        attributes.put("fourthStart", fourthParam);
        attributes.put("fourthEnd", fifthParam);
        attributes.put("lastStart", fifthParam);
        attributes.put("lastEnd", Timestamp.valueOf(now));
        var result = customerRepository.executeNativeQuery(query, attributes, AnalyzeCustomerNewInMonthResp.ResultSet.class).get(0);
        return dtoMapper.map(result, AnalyzeCustomerNewInMonthResp.class);
    }

    @Override
    public RevokeTokenResp revokeToken(String refreshToken) {
        RefreshTokenHolder holder = tokenManager.validateRefreshToken(refreshToken);
        String jwt;
        if (holder.isNonNull()) {
            Customer customer = holder.getValue().getCustomer();
            tokenManager.encode(Customer_.ID, customer.getId());
            tokenManager.encode(Customer_.USERNAME, customer.getUsername());
            jwt = tokenManager.getTokenValue();
        } else {
            throw new UnAuthorizationException("Token is expired", "");
        }
        return new RevokeTokenResp(jwt, refreshToken);
    }

    @Override
    public Collection<CustomerResp> search(String query) {
        var context = entityFactory.getEntityContext(Customer.class);
        var ids = context.search(Customer.class, query);
        var customers = customerRepository.getCustomerByIds(ids);
        return dtoMapper.map(customers, CustomerResp.class);
    }

    private record TempTokenHolder(LocalDateTime expireTime, String email) {
    }

    @DependenciesInitialize
    public void setUrlResetPassword(@Value("${active.profile}") String profile) {
        Environment environment = Environment.getInstance(profile);
        this.urlResetPassword = MessageFormat.format("https://{0}:{1}{2}", environment.getProperty("server.address"), environment.getProperty("server.port"), environment.getProperty("shop.url.customer.reset.password"));
        try {
            var node = JacksonUtils.readJsonFile(ResourceUtils.getURL(environment.getProperty("google.email.credentials")));
            this.botEmail = node.get("client_email").asText();
            if (this.botEmail.isEmpty()) {
                throw new CriticalException("Google credentials json not valid");
            }
        } catch (FileNotFoundException e) {
            throw new CriticalException("Google credentials not found", e);
        }
    }
}
