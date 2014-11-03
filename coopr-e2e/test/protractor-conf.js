var config = {
  allScriptsTimeout: 11000,

  specs: [
    'e2e/providers/provider-spec.js'
  ],

  capabilities: {

    // Settings for the browser you want to test
    'browserName' : 'Chrome'
  },

  seleniumAddress: 'http://hub.browserstack.com/wd/hub',

  baseUrl: 'http://localhost:8080/',

  framework: 'jasmine',

  jasmineNodeOpts: {
    defaultTimeoutInterval: 30000
  },

  onPrepare: function() {
    browser.driver.manage().window().maximize();
  }
};

if (process.env.TRAVIS) {
  config.capabilities['browserstack.user'] = process.env.BROWSER_STACK_USERNAME;
  config.capabilities['browserstack.key'] = process.env.BROWSER_STACK_ACCESS_KEY;
  // config.sauceUser = process.env.SAUCE_USERNAME;
  // config.sauceKey = process.env.SAUCE_ACCESS_KEY;
  // config.capabilities['tunnel-identifier'] = process.env.TRAVIS_JOB_NUMBER;
  // config.capabilities['build'] = process.env.TRAVIS_BUILD_NUMBER;
  // config.capabilities['name'] = "coopr-ngui build#"+process.env.TRAVIS_BUILD_NUMBER;
  // 'browser' : 'Chrome'
}


module.exports.config = config;
