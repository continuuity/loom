var module = angular.module(PKG.name+'.services');

module.factory('myApi_hardwaretypes', function($resource, myApiPrefix){

  return {

    HardwareType: $resource(myApiPrefix + 'hardwaretypes/:name',
      { name: '@name' },
      { 
        update: {
          method: 'PUT'
        }
      }
    )

  };

});

