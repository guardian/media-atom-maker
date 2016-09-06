var mediaAtomApp = angular.module('mediaAtomApp');

mediaAtomApp.controller('AtomCtrl', ['$scope', '$http', '$routeParams', '$httpParamSerializer', '$sce', '$q', 'appConfig', function($scope, $http, $routeParams, $httpParamSerializer, $sce, $q, appConfig) {

    $scope.atom = {};
    $scope.publishedNotPreview = false;
    $scope.publishedAtom = null;
    $scope.assets = {};
    $scope.newAsset = {};
    $scope.alerts = [];
    $scope.embedLink = '/atom/media/' + $routeParams.id;
    $scope.createNewAsset = false;
    $scope.showPreviewAtom = true;

    $scope.$watch('atom', function() {
        setPublishedNotPreview();
    }, true);

    $scope.$watch('publishedAtom', function() {
        setPublishedNotPreview();
    }, true);

    $scope.$watchGroup(['config', 'publishedAtom'], function() {
        setCapiLink();
    });

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

    $scope.preventPublishedVersionRevert = function() {
        addAlert('You cannot change the version of a published atom', 'danger');
    };

    $scope.validateVersion = function(form) {

        // If the uri has not been set yet, we cannot validate the version
        if (!$scope.newAsset.uri) {
            form.versionField.$setValidity('required', true);
        } else {

            var platform;

            if (/https:\/\/www.youtube.com\/watch\?v=/.test($scope.newAsset.uri)) {
                platform = 'Youtube';

            } else if (/https:\/\//.test($scope.newAsset.uri)) {
                platform = 'Url';
            }

            if ($scope.assets.some(function(asset) {
                    return asset.platform === platform && asset.version === $scope.newAsset.version;
            }) ) {
                form.versionField.$setValidity('required', false);
            }
            else {
                form.versionField.$setValidity('required', true);
            }

        }
    }

    function getConfig() {
        return $http.get('/api/config-values')
        .then(function(response) {
            $scope.config = response.data;
        });
    }

    function setCapiLink() {
        if ($scope.config && $scope.config.stage === 'PROD') {
            if ($scope.publishedAtom) {
                $scope.linkToCapi = appConfig.prodLiveUrl + $scope.embedLink + appConfig.capiApiKey;
            } else {
                $scope.linkToCapi = appConfig.prodPreviewUrl + $scope.embedLink + appConfig.capiApiKey;
            }
        } else {
            if ($scope.publishedAtom) {
                $scope.linkToCapi = appConfig.codeLiveUrl + $scope.embedLink + appConfig.capiApiKey;
            } else {
                $scope.linkToCapi = appConfig.codePreviewUrl + $scope.embedLink + appConfig.capiApiKey;

            }
        }
    }

    function addAlert(message, type) {
        $scope.alerts.push({
            msg: message,
            type: type
        });
    };

    function setPublishedNotPreview() {
        $scope.publishedNotPreview = !angular.equals($scope.atom, $scope.publishedAtom);
    }

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

    getPreviewAndPublishedAtoms($routeParams.id);
    getConfig();
}]);
