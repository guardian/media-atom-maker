angular.module('mediaAtomApp').directive('validPosterUrl', function() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attr, ctrl) {

            function posterUrlValidator(ngModelValue) {

                if (!ngModelValue) {
                    ctrl.$setValidity('posterValidator', true);
                }
                else if (/^https?:\/\//.test(ngModelValue)) {
                    ctrl.$setValidity('posterValidator', true);
                }
                else {
                    ctrl.$setValidity('posterValidator', false);
                }
                return ngModelValue;
            }

            ctrl.$parsers.push(posterUrlValidator)
        }
    };
});
