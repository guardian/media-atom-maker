var mediaAtomApp = angular.module('mediaAtomApp');

mediaAtomApp.directive('mediaAlerts', function() {

    return {
        restrict: 'E',
        scope: {
            alerts: '=alerts'
        },
        controller: ['$scope', function alertsController($scope) {

            $scope.closeAlert = function(index) {
                $scope.alerts.splice(index, 1);
            };
        }],
        templateUrl: 'assets/javascript/shared/directives/alert.html'
    };
});
