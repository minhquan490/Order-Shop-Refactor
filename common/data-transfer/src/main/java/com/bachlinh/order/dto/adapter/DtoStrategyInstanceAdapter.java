package com.bachlinh.order.dto.adapter;

import lombok.RequiredArgsConstructor;
import com.bachlinh.order.core.scanner.ApplicationScanner;
import com.bachlinh.order.dto.strategy.DtoStrategy;
import com.bachlinh.order.environment.Environment;
import com.bachlinh.order.exception.system.common.CriticalException;
import com.bachlinh.order.service.container.DependenciesResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public final class DtoStrategyInstanceAdapter {

    private final ApplicationScanner scanner;
    private final DependenciesResolver resolver;
    private final Environment environment;

    public Map<Class<?>, List<DtoStrategy<?, ?>>> instanceStrategies() {
        var result = new HashMap<Class<?>, List<DtoStrategy<?, ?>>>();
        for (var clazz : scanner.findComponents()) {
            if (DtoStrategy.class.isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked")
                var instance = initStrategy((Class<DtoStrategy<?, ?>>) clazz);
                result.compute(instance.getTargetType(), (aClass, dtoStrategies) -> {
                    if (dtoStrategies == null) {
                        dtoStrategies = new ArrayList<>();
                    }
                    dtoStrategies.add(instance);
                    return dtoStrategies;
                });
            }
        }
        return result;
    }

    private DtoStrategy<?, ?> initStrategy(Class<DtoStrategy<?, ?>> strategyClass) {
        try {
            var constructor = strategyClass.getDeclaredConstructor(DependenciesResolver.class, Environment.class);
            constructor.setAccessible(true);
            return constructor.newInstance(resolver, environment);
        } catch (Exception e) {
            throw new CriticalException(e.getMessage(), e);
        }
    }
}
