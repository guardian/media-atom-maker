var mediaAtomApp = angular.module('mediaAtomApp', ['ngRoute', 'ui.bootstrap'])
.constant('appConfig', {
    codePreviewUrl: 'https://preview.content.code.dev-guardianapis.com',
    codeLiveUrl: 'http://content.code.dev-guardianapis.com',
    prodPreviewUrl: 'https://preview.content.guardianapis.com',
    prodLiveUrl: 'https://content.guardianapis.com',
    capiApiKey: '?api-key=test'
})
.config(['$routeProvider', function($routeProvider, $locationProvider) {

    return $routeProvider.when('/', {
        templateUrl: '/assets/javascript/atom-list/atom-list.html',
        controller: 'AtomListCtrl'
    }).when('/atom/:id', {
        templateUrl: '/assets/javascript/atom/atom.html',
        controller: 'AtomCtrl'
    }).otherwise({
        redirectTo: '/'
    });

    $locationProvider.html5Mode(true);
}]);
