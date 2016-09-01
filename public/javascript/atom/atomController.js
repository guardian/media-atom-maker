var mediaAtomApp = angular.module('mediaAtomApp');

mediaAtomApp.controller('AtomCtrl', ['$scope', '$http', '$routeParams', '$httpParamSerializer', '$sce', '$q', function($scope, $http, $routeParams, $httpParamSerializer, $sce, $q) {

    $scope.atom = {};
    $scope.publishedNotPreview = false;
    $scope.publishedAtom = null;
    $scope.assets = {};
    $scope.newAsset = {};
    $scope.alerts = [];
    $scope.showPublishedAtom = false;
    $scope.embedLink = '/atom/media/' + $routeParams.id;
    $scope.createNewAsset = false;

    $scope.$watch('atom', function() {
        setPublishedNotPreview();
    }, true);

    $scope.$watch('publishedAtom', function() {
        setPublishedNotPreview();
    }, true);

    function setPublishedNotPreview() {
        $scope.publishedNotPreview = !angular.equals($scope.atom, $scope.publishedAtom);
    }

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
            addAlert(err, 'danger');
            return;
        });
    };

    $scope.publish = function() {
        $scope.publishing = true;
        return $http.post('/api/atom/'+$routeParams.id+'/publish')
        .success(function() {
            $scope.publishedAtom = JSON.parse(JSON.stringify($scope.atom));
            $scope.publishing = false;
            addAlert('Published successfully', 'success');
            return;
        })
        .error(function(err) {
            $scope.publishing = false;
            addAlert(err, 'danger');
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
            $scope.atom = parseAtomFromResponse(response);
            $scope.newAsset = { version: response.data.activeVersion + 1 };
            $scope.createNewAsset = false;
            return;
        })
        .error(function(err) {
            $scope.addingAsset = false;
            addAlert(err, 'danger');
            return;
        });
    };

    $scope.revertVersion = function(version) {
        return $http.post('/api/atom/' + $routeParams.id + "/revert/" + version, {
            headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}
        })
        .success(function(response) {
            $scope.atom = parseAtomFromResponse(response);
            return;
        })
        .error(function(err) {
            addAlert(err, 'danger');
            return;
        });
    };

    addAlert = function(message, type) {
        $scope.alerts.push({
            msg: message,
            type: type
        });
    };

    function getPreviewAndPublishedAtoms(id) {
        return $q.all([$http.get('/api/atom/'+id), $http.get('/api/published-atom/'+id)])
        .then(function(responses) {

            var previewResponse = responses[0].data;
            var publishedResponse = responses[1].data;
            $scope.atom = parseAtomFromResponse(previewResponse);
            $scope.assets = previewResponse.data.assets;
            $scope.newAsset.version = $scope.assets.length > 1 ? previewResponse.data.activeVersion + 1 : previewResponse.data.activeVersion;

            if (publishedResponse === 'not published') {
                $scope.publishedAtom = null;
            } else {
                $scope.publishedAtom = parseAtomFromResponse(publishedResponse);
            }

            return;
        });
    }

    function parseAtomFromResponse(atomResponse) {

        return {
            title: atomResponse.data.title,
            category: atomResponse.data.category,
            duration: atomResponse.data.duration,
            activeVersion: atomResponse.data.activeVersion,
            posterUrl: atomResponse.data.posterUrl,
            assets: atomResponse.data.assets,
            defaultHtml: atomResponse.defaultHtml,
            trustedHtml: $sce.trustAsHtml(atomResponse.defaultHtml)
        };
    }

    getPreviewAndPublishedAtoms($routeParams.id)
}]);
