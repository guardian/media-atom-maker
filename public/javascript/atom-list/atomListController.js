var mediaAtomApp = angular.module('mediaAtomApp');

mediaAtomApp.controller('AtomListCtrl', ['$scope', '$http', '$httpParamSerializer', '$location', function($scope, $http, $httpParamSerializer, $location) {

    $scope.atom = {};
    $scope.mediaAtoms = [];
    $scope.alerts = [];

    $scope.saveAtom = function() {

        $scope.savingAtom = true;

        return $http.post('/api/atom',
            $httpParamSerializer($scope.atom), {
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}
        })
        .success(function(atom) {
            $scope.savingAtom = false;
            $location.path('/atom/' + atom.id);
            return;
        })
        .error(function(err) {
            addAlert(err);
            return;
        });
    }

    function getAtoms() {
        return $http.get('/api/atoms')
        .success(function(atoms) {
            $scope.mediaAtoms = atoms;
            return;
        })
        .error(function(err) {
            $scope.mediaAtoms = null;
             addAlert(err.error);
             return;
        });
    };

    addAlert = function(message) {
        $scope.alerts.push({
            msg: message,
            type: 'danger'
        });
    };

    getAtoms();

}]);
