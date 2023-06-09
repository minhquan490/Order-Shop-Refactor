package com.bachlinh.order.web.service.impl;

import lombok.RequiredArgsConstructor;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.ServiceComponent;
import com.bachlinh.order.dto.DtoMapper;
import com.bachlinh.order.entity.EntityFactory;
import com.bachlinh.order.entity.model.District;
import com.bachlinh.order.repository.DistrictRepository;
import com.bachlinh.order.web.dto.form.common.DistrictSearchForm;
import com.bachlinh.order.web.dto.resp.DistrictResp;
import com.bachlinh.order.web.service.business.DistrictSearchService;

import java.util.Collection;

@ServiceComponent
@ActiveReflection
@RequiredArgsConstructor(onConstructor = @__(@ActiveReflection))
public class DistrictServiceImpl implements DistrictSearchService {
    private final DistrictRepository districtRepository;
    private final EntityFactory entityFactory;
    private final DtoMapper dtoMapper;

    @Override
    public Collection<DistrictResp> search(DistrictSearchForm form) {
        var context = entityFactory.getEntityContext(District.class);
        Collection<String> ids = context.search(form.getQuery());
        var districts = districtRepository.getDistricts(ids);
        return dtoMapper.map(districts, DistrictResp.class);
    }
}
