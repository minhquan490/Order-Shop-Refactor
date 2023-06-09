package com.bachlinh.order.batch.job;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.bachlinh.order.batch.Report;
import com.bachlinh.order.environment.Environment;
import com.bachlinh.order.service.container.DependenciesResolver;

import java.time.LocalDateTime;

public abstract non-sealed class AbstractJob implements Job {
    private final String name;
    private Report report;
    private final Environment environment;
    private final DependenciesResolver dependenciesResolver;
    private boolean excuted = false;

    protected AbstractJob(String name, String activeProfile, DependenciesResolver dependenciesResolver) {
        this.name = name;
        this.report = new Report(name);
        this.environment = Environment.getInstance(activeProfile);
        this.dependenciesResolver = dependenciesResolver;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute() {
        inject();
        try {
            doExecuteInternal();
        } catch (Exception e) {
            addException(e);
        } finally {
            this.excuted = true;
        }
    }

    @Override
    public final Report getJobReport() {
        try {
            return report;
        } finally {
            report = new Report(name);
        }
    }

    @Override
    public final LocalDateTime getNextExecutionTime() {
        if (!excuted) {
            return doGetNextExecutionTime();
        }
        return getPreviousExecutionTime();
    }

    @Override
    public final LocalDateTime getPreviousExecutionTime() {
        return doGetPreviousExecutionTime();
    }

    protected void addException(Exception exception) {
        this.report.addError(exception);
    }

    protected Environment getEnvironment() {
        return environment;
    }

    protected DependenciesResolver getDependenciesResolver() {
        return dependenciesResolver;
    }

    protected abstract void inject();

    protected abstract void doExecuteInternal() throws Exception;

    protected abstract LocalDateTime doGetNextExecutionTime();

    protected abstract LocalDateTime doGetPreviousExecutionTime();
}
