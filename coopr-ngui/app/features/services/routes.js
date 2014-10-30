angular.module(PKG.name)
  .config(function ($stateProvider, MYAUTH_ROLE, myHelpers) {

    var crud = myHelpers.crud.mkState,
        abstractSubnav = myHelpers.crud.abstractSubnav;

    /**
     * State Configurations
     */
    $stateProvider


      /*
        /#/services/...
       */
      .state(abstractSubnav('Service', {
        authorizedRoles: MYAUTH_ROLE.admin
      }))
        .state(crud('Service', 'list', 'CrudListCtrl'))
        .state(crud('Service', 'edit', 'ServiceFormCtrl'))
        .state(crud('Service', 'create', 'ServiceFormCtrl'))



      ;


  });
