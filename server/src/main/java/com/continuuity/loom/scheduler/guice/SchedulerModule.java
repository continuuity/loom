package com.continuuity.loom.scheduler.guice;

import com.continuuity.loom.common.conf.Configuration;
import com.continuuity.loom.common.conf.Constants;
import com.continuuity.loom.scheduler.ClusterScheduler;
import com.continuuity.loom.scheduler.JobScheduler;
import com.continuuity.loom.scheduler.Scheduler;
import com.continuuity.loom.scheduler.SolverScheduler;
import com.continuuity.loom.scheduler.WorkerBalanceScheduler;
import com.continuuity.loom.scheduler.callback.ClusterCallback;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 * Guice module for binding scheduler related classes.
 */
public class SchedulerModule extends AbstractModule {
  private final ListeningExecutorService callbackExecutorService;
  private final ListeningExecutorService solverExecutorService;
  private final String schedulerId;
  private final Class callbackClass;

  public SchedulerModule(Configuration conf,
                         ListeningExecutorService callbackExecutorService,
                         ListeningExecutorService solverExecutorService) throws ClassNotFoundException {
    this.callbackExecutorService = callbackExecutorService;
    this.solverExecutorService = solverExecutorService;
    this.schedulerId = "scheduler-" + conf.get(Constants.HOST);
    this.callbackClass = Class.forName(conf.get(Constants.CALLBACK_CLASS));
  }

  @Override
  protected void configure() {
    bind(ClusterCallback.class).to(callbackClass).in(Scopes.SINGLETON);
    bind(String.class).annotatedWith(Names.named("scheduler.id")).toInstance(schedulerId);

    bind(ListeningExecutorService.class)
      .annotatedWith(Names.named("solver.executor.service"))
      .toInstance(solverExecutorService);
    bind(ListeningExecutorService.class)
      .annotatedWith(Names.named("callback.executor.service"))
      .toInstance(callbackExecutorService);

    bind(JobScheduler.class).in(Scopes.SINGLETON);
    bind(ClusterScheduler.class).in(Scopes.SINGLETON);
    bind(SolverScheduler.class).in(Scopes.SINGLETON);
    bind(Scheduler.class).in(Scopes.SINGLETON);
    bind(WorkerBalanceScheduler.class).in(Scopes.SINGLETON);
  }
}
