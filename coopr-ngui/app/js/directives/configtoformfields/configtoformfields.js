/**
 * Configtoformfields directive.
 *
 * This takes a config and converts fields into form fields bound to a provided object.
 * !! This directive modifies model passed to it from the parent.
 * @param {Object|String} fieldsconfig configuration for fields.
 * Expected config:
 * fields: {
 *   <fieldname>: {
 *     label: string,
 *     override: boolean,
 *     tip: string,
 *     type: string
 *   }
 * },
 * required: [
 *     [<fieldname>, <fieldname>]
 *   ]
 * }
 * @param {Object} model Model to attach field values to.
 * @param {Booelan} allowOverride Determines whether only override-able form fields should be
 * enabled.                                
 *
 * <div my-configtoformfields 
 *       data-config="json" 
 *       data-model="whatever" 
 *       data-allow-override="true"
 *  />
 */

angular.module(PKG.name+'.directives').directive('myConfigtoformfields', 
function myConfigtoformfieldsDirective () {
  return {
    restrict: 'AE',
    replace: false,

    scope: {
      config: '=',
      model: '='
    },

    templateUrl: 'configtoformfields/configtoformfields.html',

    link: function (scope, element, attrs) {

      scope.allowOverride = attrs.allowOverride && attrs.allowOverride!=='false';

      scope.$watch('config', function(newVal, oldVal) {
        if(!scope.model) {
          return;
        }

        if(oldVal) { // remove model values
          angular.forEach(oldVal.fields, function (field, key) {
            delete scope.model[key];
          });          
        }

        if(newVal) { // set default values
          angular.forEach(newVal.fields, function (field, key) {
            if (field.hasOwnProperty('default') && !scope.model[key]) {
              scope.model[key] = field.default;
            }
          });
        }

        // set required fields
        scope.required = (newVal.required || []).reduce(function (memo, arr) {
          // could be an array of strings, or an array of arrays of strings.
          angular.forEach(angular.isArray(arr) ? arr : [arr], function (field) {
            memo[field] = true;
          });
          return memo;
        }, {});

      }, true);

    }
  };

});
