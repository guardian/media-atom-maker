var mediaAtomApp = angular.module('mediaAtomApp', ['ngRoute'])
.config(['$routeProvider', function($routeProvider, $locationProvider) {

    console.log('hello from main!');

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

