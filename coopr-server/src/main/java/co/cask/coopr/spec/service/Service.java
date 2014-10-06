/*
 * Copyright © 2012-2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.cask.coopr.spec.service;

import co.cask.coopr.spec.NamedIconEntity;
import co.cask.coopr.spec.ProvisionerAction;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * A service defines a set of services it is dependent on, as well as a mapping of
 * {@link co.cask.coopr.spec.ProvisionerAction} to {@link ServiceAction} that provisioners will need to execute
 * when performing cluster operations such as creation and deletion.
 */
public final class Service extends NamedIconEntity {
  private final String description;
  private final ServiceDependencies dependencies;
  private final Map<ProvisionerAction, ServiceAction> provisionerActions;

  private Service(String name, String logolink, String description, ServiceDependencies dependencies,
                  Map<ProvisionerAction, ServiceAction> provisionerActions) {
    super(name, logolink);
    this.description = description;
    this.dependencies = dependencies;
    this.provisionerActions = provisionerActions;
  }

  /**
   * Get the description of this service.
   *
   * @return Description of service.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the {@link ServiceDependencies} of this service.
   *
   * @return Dependencies of this service.
   */
  public ServiceDependencies getDependencies() {
    return dependencies;
  }

  /**
   * Get the mapping of {@link ProvisionerAction} to {@link ServiceAction} for this service.
   *
   * @return Map of action types to {@link ServiceAction} for this service.
   */
  public Map<ProvisionerAction, ServiceAction> getProvisionerActions() {
    return provisionerActions;
  }

  /**
   * Get a builder for creating a service.
   *
   * @return Builder for creating a service.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for creating a service.
   */
  public static class Builder {
    private String name;
    private String icon;
    private String description = "";
    private ServiceDependencies dependencies = ServiceDependencies.EMPTY_SERVICE_DEPENDENCIES;
    private Map<ProvisionerAction, ServiceAction> provisionerActions = ImmutableMap.of();

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setIcon(String icon) {
      this.icon = icon;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setDependencies(ServiceDependencies dependencies) {
      this.dependencies = dependencies;
      return this;
    }

    public Builder setProvisionerActions(Map<ProvisionerAction, ServiceAction> actions) {
      this.provisionerActions = ImmutableMap.copyOf(actions);
      return this;
    }

    public Service build() {
      return new Service(name, icon, description, dependencies, provisionerActions);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Service)) {
      return false;
    }
    Service other = (Service) o;
    return super.equals(other) &&
      Objects.equal(description, other.description) &&
      Objects.equal(dependencies, other.dependencies) &&
      Objects.equal(provisionerActions, other.provisionerActions);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), description, icon, dependencies, provisionerActions);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("description", description)
      .add("dependencies", dependencies)
      .add("provisionerActions", provisionerActions)
      .toString();
  }
}
