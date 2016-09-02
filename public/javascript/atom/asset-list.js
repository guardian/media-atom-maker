angular.module('mediaAtomApp').directive('assetList', function() {
    return {
        restrict: 'E',
        scope: {
            allowEdit: '=',
            assets: '=',
            activeVersion: '=',
            revertVersion: '&'
        },
        templateUrl: 'assets/javascript/atom/asset-list.html'
    };
});
