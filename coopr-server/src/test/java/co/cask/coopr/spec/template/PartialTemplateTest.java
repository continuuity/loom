package co.cask.coopr.spec.template;

import co.cask.coopr.BaseTest;
import co.cask.coopr.account.Account;
import co.cask.coopr.cluster.ClusterService;
import co.cask.coopr.common.conf.Constants;
import co.cask.coopr.provisioner.Provisioner;
import co.cask.coopr.provisioner.TenantProvisionerService;
import co.cask.coopr.spec.Tenant;
import co.cask.coopr.spec.TenantSpecification;
import co.cask.coopr.store.entity.EntityStoreView;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PartialTemplateTest extends BaseTest {

  private static ClusterTemplate insecureTemplate;
  private static ClusterTemplate secureTemplate;
  private static ClusterTemplate distributedTemplate;
  private static PartialTemplate sensuPartial;
  private static PartialTemplate ldapPartial;
  private static PartialTemplate partialWithOverrides;
  private static PartialTemplate mandatoryPartial1;
  private static PartialTemplate mandatoryPartial2;
  private static ClusterTemplate templateWithOverridesInBody;
  private static ClusterTemplate templateWithOverridesInPartial;

  private static EntityStoreView entityStoreView;
  private static ClusterService clusterService;
  private static Account account;

  private static final Set<String> defaultsServices =
    Sets.newHashSet("cdap­security", "mysql­server", "sensu­monitoring", "bob", "hive-metastore-database",
                    "hive-metastore", "zookeeper­server", "cdap", "hive-server2", "ldap­internal",
                    "mandatory-first-service", "mandatory-second-service");
  private static final Set<String> compatibilitiesHardwaretypes = Sets.newHashSet("standard­large", "standard-xlarge");
  private static final Set<String> compatibilitiesImagetypes = Sets.newHashSet("centos6", "ubuntu12");
  private static final Set<String> compatibilitiesServices =
    Sets.newHashSet("mysql­server", "sensu­monitoring", "kerberos­client", "zookeeper­server", "cdap",
                    "ldap­internal", "mandatory-first-service", "mandatory-second-service");
  private static final Map<String, ServiceConstraint> serviceConstraints = Maps.newHashMap();

  private static final ClassLoader classLoader = PartialTemplateTest.class.getClassLoader();

  @Override
  protected boolean shouldClearDataBetweenTests() {
    return false;
  }

  @BeforeClass
  public static void setupClusterServiceTests() throws Exception {
    clusterService = injector.getInstance(ClusterService.class);
    TenantProvisionerService tenantProvisionerService = injector.getInstance(TenantProvisionerService.class);
    // setup data
    tenantProvisionerService.writeProvisioner(new Provisioner("p1", "host", 50056, 100, null, null));
    tenantProvisionerService.writeTenantSpecification(new TenantSpecification("tenantX", 10, 1, 10));
    Tenant tenant = tenantStore.getTenantByName("tenantX");
    account = new Account(Constants.ADMIN_USER, tenant.getId());
    entityStoreView = entityStoreService.getView(account);
    serviceConstraints.put("mysql-server", new ServiceConstraint(null, null, 1, 1));
    serviceConstraints.put("zookeeper-server", new ServiceConstraint(null, null, 1, 1));
  }

  @Test
  public void test_01_parse() throws Exception {
    //load json templates
    InputStream insecureIn = classLoader.getResourceAsStream("partials/cdap-distributed-insecure.json");
    InputStream distributedIn = classLoader.getResourceAsStream("partials/cdap-distributed.json");
    InputStream secureIn = classLoader.getResourceAsStream("partials/cdap-distributed-secure-hadoop.json");
    InputStream sensuIn = classLoader.getResourceAsStream("partials/sensu-partial.json");
    InputStream ldapIn = classLoader.getResourceAsStream("partials/ldap-partial.json");
    InputStream templateWithOverridesInBodyIn =
      classLoader.getResourceAsStream("partials/template-with-overrides-in-body.json");
    InputStream templateWithOverridesInPartialIn =
      classLoader.getResourceAsStream("partials/template-with-overrides-in-partial.json");
    InputStream partialWithOverridesIn = classLoader.getResourceAsStream("partials/partial-immutable-overrides.json");
    InputStream mandatoryPartial1In = classLoader.getResourceAsStream("partials/first-mandatory.json");
    InputStream mandatoryPartial2In = classLoader.getResourceAsStream("partials/second-mandatory.json");

    insecureTemplate = gson.fromJson(IOUtils.toString(insecureIn), ClusterTemplate.class);
    distributedTemplate = gson.fromJson(IOUtils.toString(distributedIn), ClusterTemplate.class);
    secureTemplate = gson.fromJson(IOUtils.toString(secureIn), ClusterTemplate.class);
    templateWithOverridesInBody =
      gson.fromJson(IOUtils.toString(templateWithOverridesInBodyIn), ClusterTemplate.class);
    templateWithOverridesInPartial =
      gson.fromJson(IOUtils.toString(templateWithOverridesInPartialIn), ClusterTemplate.class);
    sensuPartial = gson.fromJson(IOUtils.toString(sensuIn), PartialTemplate.class);
    ldapPartial = gson.fromJson(IOUtils.toString(ldapIn), PartialTemplate.class);
    partialWithOverrides = gson.fromJson(IOUtils.toString(partialWithOverridesIn), PartialTemplate.class);
    mandatoryPartial1 = gson.fromJson(IOUtils.toString(mandatoryPartial1In), PartialTemplate.class);
    mandatoryPartial2 = gson.fromJson(IOUtils.toString(mandatoryPartial2In), PartialTemplate.class);

    Assert.assertNotNull(insecureTemplate);
    Assert.assertNotNull(distributedTemplate);
    Assert.assertNotNull(secureTemplate);
    Assert.assertNotNull(sensuPartial);
    Assert.assertNotNull(ldapPartial);
    Assert.assertNotNull(partialWithOverrides);
    Assert.assertNotNull(mandatoryPartial1);
    Assert.assertNotNull(templateWithOverridesInBody);
    Assert.assertNotNull(templateWithOverridesInPartial);
  }

  @Test
  public void test_02_persist() throws Exception {
    int beforePartialsCount = entityStoreView.getAllPartialTemplates(false).size();
    int beforeMandatoryPartialsCount = entityStoreView.getAllPartialTemplates(true).size();
    int beforeClusterTemplatesCount = entityStoreView.getAllClusterTemplates().size();

    entityStoreView.writeClusterTemplate(insecureTemplate);
    entityStoreView.writeClusterTemplate(distributedTemplate);
    entityStoreView.writeClusterTemplate(secureTemplate);
    entityStoreView.writeClusterTemplate(templateWithOverridesInBody);
    entityStoreView.writeClusterTemplate(templateWithOverridesInPartial);

    entityStoreView.writePartialTemplate(sensuPartial, false);
    entityStoreView.writePartialTemplate(ldapPartial, false);
    entityStoreView.writePartialTemplate(partialWithOverrides, false);
    entityStoreView.writePartialTemplate(mandatoryPartial1, true);
    entityStoreView.writePartialTemplate(mandatoryPartial2, true);

    int afterPartialsCount = entityStoreView.getAllPartialTemplates(false).size();
    int afterMandatoryPartialsCount = entityStoreView.getAllPartialTemplates(true).size();
    int afterClusterTemplatesCount = entityStoreView.getAllClusterTemplates().size();

    Assert.assertEquals(afterPartialsCount, beforePartialsCount + 3);
    Assert.assertEquals(afterClusterTemplatesCount, beforeClusterTemplatesCount + 5);
    Assert.assertEquals(afterMandatoryPartialsCount, beforeMandatoryPartialsCount + 2);
  }

  @Test
  public void test_03_persistentTemplatesConsistence() throws Exception {
    PartialTemplate ldapInternal = entityStoreView.getPartialTemplate("LDAP-internal", false);
    PartialTemplate sensuInternal = entityStoreView.getPartialTemplate("sensu-internal", false);

    ClusterTemplate cdapDistributedSecureHadoop = entityStoreView.getClusterTemplate("cdap-distributed-secure-hadoop");
    ClusterTemplate cdapDistributedInsecure = entityStoreView.getClusterTemplate("cdap-distributed-insecure");
    ClusterTemplate cdapDistributed = entityStoreView.getClusterTemplate("cdap-distributed");

    Assert.assertNotNull(ldapInternal);
    Assert.assertNotNull(sensuInternal);

    Assert.assertNotNull(cdapDistributedSecureHadoop);
    Assert.assertNotNull(cdapDistributedInsecure);
    Assert.assertNotNull(cdapDistributed);

    Assert.assertEquals("Configure Example, Inc. LDAP services", ldapInternal.getDescription());
    Assert.assertEquals(true, sensuInternal.isImmutable());
    Assert.assertEquals("ldap-internal", ldapInternal.clusterDefaults.getServices().iterator().next());
    Assert.assertEquals("ldap-internal", ldapInternal.compatibilities.getServices().iterator().next());
    Assert.assertNotNull(ldapInternal.clusterDefaults.getConfig().get("ldap"));
    Assert.assertEquals("ldap.wrong.com", ldapInternal.clusterDefaults.getConfig().get("ldap")
      .getAsJsonObject().get("endpoint").getAsString());

    Assert.assertEquals("Cask DAP (CDAP) with Security and Secure Hadoop cluster with single master",
                        cdapDistributedSecureHadoop.getDescription());
    Assert.assertNotNull(cdapDistributedSecureHadoop.getParent());
    Assert.assertEquals("cdap-distributed", cdapDistributedSecureHadoop.getParent().getName());
    Assert.assertNotNull(cdapDistributedSecureHadoop.getIncludes());
    Assert.assertEquals("LDAP-internal", cdapDistributedSecureHadoop.getIncludes().iterator().next().getName());
    Assert.assertEquals(3, cdapDistributedSecureHadoop.clusterDefaults.getServices().size());
    Assert.assertNotNull(cdapDistributedSecureHadoop.getClusterDefaults().getConfig().get("hive"));
    Assert.assertEquals("kerberos-client",
                        cdapDistributedSecureHadoop.getCompatibilities().getServices().iterator().next());
  }

  @Test(expected = TemplateImmutabilityException.class)
  public void test_04_overrideImmutableInBody() throws Exception {
    clusterService.resolveTemplate(account, templateWithOverridesInBody);
  }

  @Test(expected = TemplateImmutabilityException.class)
  public void test_05_overrideImmutableInPartial() throws Exception {
    clusterService.resolveTemplate(account, templateWithOverridesInPartial);
  }

  @Test
  public void test_06_resolveTemplateTest() throws Exception {
    ClusterTemplate rt = clusterService.resolveTemplate(account, secureTemplate);

    Assert.assertEquals(secureTemplate.getIncludes(), rt.getIncludes());
    Assert.assertEquals(secureTemplate.getParent(), rt.getParent());
    Assert.assertEquals(secureTemplate.getName(), rt.getName());
    Assert.assertEquals(secureTemplate.getDescription(), rt.getDescription());
    Assert.assertEquals(defaultsServices, rt.getClusterDefaults().getServices());
    Assert.assertEquals(17, rt.getClusterDefaults().getConfig().entrySet().size());
    Assert.assertEquals("rackspace", rt.getClusterDefaults().getProvider());
    Assert.assertEquals("standard-large", rt.getClusterDefaults().getHardwaretype());
    Assert.assertEquals("centos6", rt.getClusterDefaults().getImagetype());
    Assert.assertEquals("example.com", rt.getClusterDefaults().getDnsSuffix());

    Assert.assertEquals(2, rt.getConstraints().getLayoutConstraint().getServicesThatMustCoexist().size());
    Assert.assertEquals(2, rt.getConstraints().getLayoutConstraint().getServicesThatMustNotCoexist().size());
    Assert.assertEquals(serviceConstraints, rt.getConstraints().getServiceConstraints());

    Assert.assertEquals(43200000, rt.getAdministration().getLeaseDuration().getInitial());
    Assert.assertEquals(43200000, rt.getAdministration().getLeaseDuration().getStep());

    Assert.assertEquals(compatibilitiesHardwaretypes, rt.getCompatibilities().getHardwaretypes());
    Assert.assertEquals(compatibilitiesImagetypes, rt.getCompatibilities().getImagetypes());
    Assert.assertEquals(compatibilitiesServices, rt.getCompatibilities().getServices());

    //overrides in second mandatory partial
    Assert.assertEquals("ldap.correct.com", rt.getClusterDefaults().getConfig().get("ldap")
      .getAsJsonObject().get("endpoint").getAsString());

    //overrides in child
    Assert.assertTrue(rt.getClusterDefaults().getConfig().get("security.server.ssl.enabled").getAsBoolean());

    //overrides in first mandatory partial
    Assert.assertEquals("mandatory config property",
                        rt.getClusterDefaults().getConfig().get("mandatory-first-service").getAsString());
  }

  @Test
  public void test_07_not_persisted_resolveTemplate() throws Exception {
    InputStream notPersistedIn = classLoader.getResourceAsStream("partials/cdap-not-persisted.json");
    ClusterTemplate notPersisted = gson.fromJson(IOUtils.toString(notPersistedIn), ClusterTemplate.class);
    ClusterTemplate rt = clusterService.resolveTemplate(account, notPersisted);

    Assert.assertEquals(notPersisted.getIncludes(), rt.getIncludes());
    Assert.assertEquals(notPersisted.getParent(), rt.getParent());
    Assert.assertEquals(notPersisted.getName(), rt.getName());
    Assert.assertEquals(notPersisted.getDescription(), rt.getDescription());

    Set<String> expectedDefaultsServices = sensuPartial.getClusterDefaults().getServices();
    Set<String> expectedCompatibilitiesServices = sensuPartial.getCompatibilities().getServices();
    JsonObject expectedDefaultsConfig = sensuPartial.getClusterDefaults().getConfig();

    expectedDefaultsServices.addAll(mandatoryPartial1.clusterDefaults.services);
    expectedDefaultsServices.addAll(mandatoryPartial2.clusterDefaults.services);

    expectedCompatibilitiesServices.addAll(mandatoryPartial1.compatibilities.services);
    expectedCompatibilitiesServices.addAll(mandatoryPartial2.compatibilities.services);

    for (JsonObject mandatoryConfig : Sets.newHashSet(mandatoryPartial1.clusterDefaults.config,
                                                      mandatoryPartial2.clusterDefaults.config)) {
      for (Map.Entry<String, JsonElement> config : mandatoryConfig.entrySet()) {
        expectedDefaultsConfig.add(config.getKey(), config.getValue());
      }
    }

    Assert.assertEquals(expectedDefaultsServices, rt.getClusterDefaults().getServices());
    Assert.assertEquals(expectedCompatibilitiesServices, rt.getCompatibilities().getServices());
    Assert.assertEquals(expectedDefaultsConfig, rt.getClusterDefaults().getConfig());
  }

  @Test(expected = TemplateImmutabilityException.class)
  public void test_08_not_persisted_overrideImmutablePartial() throws Exception {
    InputStream notPersistedIn = classLoader.getResourceAsStream("partials/cdap-not-persisted-overrides-partial.json");
    ClusterTemplate notPersisted = gson.fromJson(IOUtils.toString(notPersistedIn), ClusterTemplate.class);
    clusterService.resolveTemplate(account, notPersisted);
  }

  @Test(expected = TemplateImmutabilityException.class)
  public void test_09_not_persisted_overrideImmutableParentPartial() throws Exception {
    InputStream notPersistedIn = classLoader.getResourceAsStream("partials/cdap-not-persisted-overrides-parent.json");
    ClusterTemplate notPersisted = gson.fromJson(IOUtils.toString(notPersistedIn), ClusterTemplate.class);
    clusterService.resolveTemplate(account, notPersisted);
  }

  @Test(expected = JsonSyntaxException.class)
  public void test_10_badJsonFormat() throws Exception {
    InputStream badFormattedJsonIn = classLoader.getResourceAsStream("partials/cdap-bad-json-format.json");
    ClusterTemplate notPersisted = gson.fromJson(IOUtils.toString(badFormattedJsonIn), ClusterTemplate.class);
    clusterService.resolveTemplate(account, notPersisted);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_11_templateWithIncorrectDns() throws Exception {
    InputStream templateWithoutDefaultsIn =
      classLoader.getResourceAsStream("partials/cdap-distributed-with-incorrect-dns.json");
    ClusterTemplate template = gson.fromJson(IOUtils.toString(templateWithoutDefaultsIn), ClusterTemplate.class);
    clusterService.resolveTemplate(account, template);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_12_templateWithoutDefaultsProvider() throws Exception {
    InputStream templateWithoutDefaultsIn =
      classLoader.getResourceAsStream("partials/cdap-distributed-without-defaults-provider.json");
    ClusterTemplate template = gson.fromJson(IOUtils.toString(templateWithoutDefaultsIn), ClusterTemplate.class);
    clusterService.resolveTemplate(account, template);
  }

  @Test
  public void test_13_deleteTemplate() throws Exception {
    int beforePartialsCount = entityStoreView.getAllPartialTemplates(false).size();
    int beforeMandatoryPartialsCount = entityStoreView.getAllPartialTemplates(true).size();
    int beforeClusterTemplatesCount = entityStoreView.getAllClusterTemplates().size();

    entityStoreView.deleteClusterTemplate(insecureTemplate.getName());
    entityStoreView.deletePartialTemplate(sensuPartial.getName(), false);
    entityStoreView.deletePartialTemplate(mandatoryPartial1.getName(), true);

    int afterPartialsCount = entityStoreView.getAllPartialTemplates(false).size();
    int afterMandatoryPartialsCount = entityStoreView.getAllPartialTemplates(true).size();
    int afterClusterTemplatesCount = entityStoreView.getAllClusterTemplates().size();

    Assert.assertEquals(afterPartialsCount, beforePartialsCount - 1);
    Assert.assertEquals(afterClusterTemplatesCount, beforeClusterTemplatesCount - 1);
    Assert.assertEquals(afterMandatoryPartialsCount, beforeMandatoryPartialsCount - 1);
  }
}
