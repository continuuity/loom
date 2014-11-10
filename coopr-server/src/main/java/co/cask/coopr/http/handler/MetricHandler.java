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

package co.cask.coopr.http.handler;

import co.cask.coopr.account.Account;
import co.cask.coopr.common.conf.Constants;
import co.cask.coopr.http.util.MetricResponse;
import co.cask.coopr.http.util.MetricUtil;
import co.cask.coopr.scheduler.task.ClusterTask;
import co.cask.coopr.store.cluster.ClusterStore;
import co.cask.coopr.store.cluster.ClusterStoreService;
import co.cask.coopr.store.cluster.ClusterTaskFilter;
import co.cask.coopr.store.tenant.TenantStore;
import co.cask.http.HttpResponder;
import com.google.gson.Gson;
import com.google.inject.Inject;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for performing metric operations.
 */
@Path(Constants.API_BASE + "/metrics")
public class MetricHandler extends AbstractAuthHandler {

  private static final Logger LOG = LoggerFactory.getLogger(MetricHandler.class);

  private final ClusterStore clusterStore;
  private final Gson gson;

  /**
   * Initializes a new instance of a MetricHandler.
   */
  @Inject
  private MetricHandler(TenantStore tenantStore, ClusterStoreService clusterStoreService, Gson gson) {
    super(tenantStore);
    this.clusterStore = clusterStoreService.getSystemView();
    this.gson = gson;
  }

  /**
   * Retrieves statistics of nodes usage for given filter.
   * The start and end times are inclusive.
   *
   * @param request the request for nodes usage
   * @param responder the responder for sending the response
   */
  @GET
  @Path("/nodes/usage")
  public void getNodesUsage(HttpRequest request, HttpResponder responder) {
    Account account = getAndAuthenticateAccount(request, responder);
    if (account == null) {
      return;
    }
    List<String> names = Arrays.asList("tenant", "user", "cluster", "clustertemplate", "start", "end", "groupby");
    Map<String, String> filters = getFilters(request, names);
    String tenant = filters.get("tenant");
    if (!account.isSuperadmin()) {
      if (tenant == null) {
        tenant = account.getTenantId();
      } else if (!tenant.equals(account.getTenantId())) {
        responder.sendString(HttpResponseStatus.METHOD_NOT_ALLOWED,
                             String.format("Getting information about tenant %s not allowed for tenant %s",
                                           tenant, account.getTenantId()));
        return;
      }
    }
    ClusterTaskFilter filter = new ClusterTaskFilter(tenant, filters.get("user"), filters.get("cluster"),
                                                  filters.get("clustertemplate"));
    try {
      List<ClusterTask> tasks = clusterStore.getClusterTasks(filter);
      MetricResponse result = MetricUtil.getNodesUsage(tasks, filters);
      responder.sendJson(HttpResponseStatus.OK, result, MetricResponse.class, gson);
    } catch (IllegalArgumentException e) {
      responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                           String.format("Incorrect value for field groupby: %s", filters.get("groupby")));
    } catch (IOException e) {
      responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Unable to read data from the database");
    }
  }

  /**
   * Retrieves query parameters from {@code request}.
   *
   * @param request the request
   * @param names list of query parameters names
   * @return {@link Map} of query parameters
   */
  private Map<String, String> getFilters(HttpRequest request, List<String> names) {
    Map<String, List<String>> queryParams = new QueryStringDecoder(request.getUri()).getParameters();
    Map<String, String> filters = new HashMap<String, String>();
    for (String name : names) {
      List<String> values = queryParams.get(name);
      if (values != null && !values.isEmpty()) {
        filters.put(name, values.get(0));
      }
    }

    return filters;
  }
}
