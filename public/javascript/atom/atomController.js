var mediaAtomApp = angular.module('mediaAtomApp');

mediaAtomApp.controller('AtomCtrl', ['$scope', '$http', '$routeParams', '$httpParamSerializer', function($scope, $http, $routeParams, $httpParamSerializer) {

    $scope.atom = {}
    $scope.assets = {}
    $scope.newAsset = {};

    $scope.saveAtom = function() {
        $scope.savingAtom = true;
        return $http.post('/api/atom/'+$routeParams.id,
            $httpParamSerializer($scope.atom), {
            headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}
        })
        .success(function() {
            $scope.savingAtom = false;
            $scope.newAsset.version += 1;
            return;
        })
        .error(function(err) {
            $scope.saving = false;
            alert(err);
        });
    };

    $scope.publish = function() {
        $scope.publishing = true;
        return $http.post('/api/atom/'+$routeParams.id+'/publish')
        .success(function() {
            $scope.publishing = false;
            alert('Published successfully');
            return;
        })
        .error(function(err) {
            $scope.publishing = false;
            alert(err.error);
            return;
        });
    };

    $scope.addAsset = function() {
        if (!$scope.newAsset.mimetype) {
            $scope.newAsset.mimetype = "";
        }
        $scope.addingAsset = true;
        return $http.post('/api/atom/' + $routeParams.id + '/asset',
                $httpParamSerializer($scope.newAsset), {
                    headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}
        })
        .success(function(response) {
            $scope.addingAsset = false;
            $scope.assets = response.data.assets;
            $scope.newAsset = { version: response.data.activeVersion + 1 };
            return;
        })
        .error(function(err) {
            $scope.addingAsset = false;
            alert(err);
        });
    };

    $scope.revertVersion = function(version) {
        return $http.post('/api/atom/' + $routeParams.id + "/revert/" + version, {
            headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}
        })
        .success(function() {
            $scope.atom.activeVersion = version;
        })
        .error(function(err) {
            alert(err);
        });
    };

    function getAtom(id) {
        return $http.get('/api/atom/'+id)
        .success(function(response) {
            $scope.atom = parseAtomFromResponse(response);
            $scope.assets = response.data.assets;
            $scope.newAsset.version = response.data.assets.length > 1 ? response.data.activeVersion + 1 : response.data.activeVersion;
            return;
        })
        .error(function(err) {
            $scope.atom = null;
            alert(err);
        });
    }

    function parseAtomFromResponse(atomResponse) {

        return {
            title: atomResponse.data.title,
            category: atomResponse.data.category,
            duration: atomResponse.data.duration,
            activeVersion: atomResponse.data.activeVersion,
            defaultHtml: atomResponse.defaultHtml,
            trustedHtml: $sce.trustAsHtml(atomResponse.defaultHtml)
        };
    }

    getAtom($routeParams.id)
}]);
