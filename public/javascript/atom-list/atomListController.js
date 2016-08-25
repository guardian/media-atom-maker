var mediaAtomApp = angular.module('mediaAtomApp');

mediaAtomApp.controller('AtomListCtrl', ['$scope', '$http', '$httpParamSerializer', function($scope, $http, $httpParamSerializer) {

    $scope.newAtom = {};
    $scope.mediaAtoms = [];

    $scope.saveAtom = function() {

        $scope.savingAtom = true;

        return $http.post('/api/atom',
                $httpParamSerializer($scope.newAtom), {
                    headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}
                })
        .success(function(atom) {
            $scope.savingAtom = false;
            $scope.mediaAtoms.push(atom);
            return;
        })
        .error(function(err) {
            alert(err.error);
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
             alert(err);
             return;
        });
    };

    getAtoms();


}]);
