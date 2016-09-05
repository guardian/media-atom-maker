angular.module('mediaAtomApp').directive('validYoutubeUrl', function() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attr, ctrl) {

            function youtubeUrlValidator(ngModelValue) {

                if (/https?:\/\/www.youtube.com\/watch\?v=/.test(ngModelValue)) {
                    ctrl.$setValidity('youtubeValidator', true);
                }
                else {
                    ctrl.$setValidity('youtubeValidator', false);
                }
                return ngModelValue;
            }

            ctrl.$parsers.push(youtubeUrlValidator)
        }
    };
});
