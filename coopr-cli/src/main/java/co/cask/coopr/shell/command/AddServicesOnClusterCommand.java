/*
 * Copyright © 2012-2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.cask.coopr.shell.command;

import co.cask.common.cli.Arguments;
import co.cask.coopr.client.ClusterClient;
import co.cask.coopr.http.request.AddServicesRequest;
import co.cask.coopr.shell.CLIConfig;
import co.cask.coopr.shell.util.CliUtil;
import com.google.inject.Inject;

import java.io.PrintStream;

import static co.cask.coopr.shell.util.Constants.CLUSTER_ID_KEY;
import static co.cask.coopr.shell.util.Constants.SERVICES_KEY;

/**
 * Adds one or more services to a cluster.
 */
public class AddServicesOnClusterCommand extends AbstractAuthCommand {

  private final ClusterClient clusterClient;

  @Inject
  private AddServicesOnClusterCommand(ClusterClient clusterClient, CLIConfig cliConfig) {
    super(cliConfig);
    this.clusterClient = clusterClient;
  }

  @Override
  public void perform(Arguments arguments, PrintStream printStream) throws Exception {
    String id = arguments.get(CLUSTER_ID_KEY);
    AddServicesRequest addServicesRequest = CliUtil.getObjectFromJson(arguments, SERVICES_KEY,
                                                                           AddServicesRequest.class);
    clusterClient.addServicesOnCluster(id, addServicesRequest);
  }

  @Override
  public String getPattern() {
    return String.format("add services <%s> on cluster <%s>", SERVICES_KEY, CLUSTER_ID_KEY);
  }

  @Override
  public String getDescription() {
    return "Adds one or more services to a cluster";
  }
}
