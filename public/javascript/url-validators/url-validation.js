angular.module('mediaAtomApp').directive('validUrl', function() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attr, ctrl) {

            function urlValidator(ngModelValue) {

                if (!ngModelValue) {
                    ctrl.$setValidity('urlValidator', true);
                }
                else if (/^https:\/\//.test(ngModelValue)) {
                    ctrl.$setValidity('urlValidator', true);
                }
                else {
                    ctrl.$setValidity('urlValidator', false);
                }
                return ngModelValue;
            }

            ctrl.$parsers.push(urlValidator)
        }
    };
});
