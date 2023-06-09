package com.bachlinh.order.repository;

import com.bachlinh.order.entity.model.Customer;
import com.bachlinh.order.entity.model.Email;

import java.util.Collection;
import java.util.List;

public interface EmailRepository {
    Email saveEmail(Email email);

    Email getEmailById(String id, Customer owner);

    Collection<Email> getAllEmailByIds(Iterable<String> ids);

    List<Email> getEmailsByFolderId(String folderId, Customer owner);

    void deleteEmails(Collection<String> ids);
}
