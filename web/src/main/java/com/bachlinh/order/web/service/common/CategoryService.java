package com.bachlinh.order.web.service.common;

import com.bachlinh.order.web.dto.form.admin.category.CategoryCreateForm;
import com.bachlinh.order.web.dto.form.admin.category.CategoryDeleteForm;
import com.bachlinh.order.web.dto.form.admin.category.CategoryUpdateForm;
import com.bachlinh.order.web.dto.resp.CategoryResp;

import java.util.Collection;

public interface CategoryService {
    boolean isExist(String id);

    CategoryResp saveCategory(CategoryCreateForm form);

    CategoryResp updateCategory(CategoryUpdateForm form);

    boolean deleteCategory(CategoryDeleteForm form);

    Collection<CategoryResp> getCategories();
}
