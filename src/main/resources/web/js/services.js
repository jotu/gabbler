'use strict';

angular.module('gabblerServices', ['ngResource']).factory('Message', function($resource) {
  return $resource('api/messages');
});
