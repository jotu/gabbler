'use strict';

function GabblerCtrl($scope, Message, $http) {

  $scope.message = new Message();

  $scope.sendMessage = function() {
    $scope.message.$save();
    $scope.message = new Message();
  };

  $scope.messages = [];

  $scope.getMessages = function() {
    var messages = Message.query(function() {
      $scope.messages = messages.concat($scope.messages);
      $scope.getMessages();
    });
  }
}
