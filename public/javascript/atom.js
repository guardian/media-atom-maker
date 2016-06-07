window.AtomUtil = (function() {
  var ret = {};

  function handleError(xhr) {
    alert(err + ":" + xhr.responseJSON.error);
  }

  ret.addAsset = function(atomId) {
    var uri = $("#urlInput").val();
    var version = $("#versionInput").val()
    $.ajax({
      method: "POST",
      url: "/api/atom/" + atomId + "/asset",
      data: { uri: uri, version: version },
      success: function() { window.location.reload(); },
      error: handleError
    });
  }

  ret.publishAtom = function(atomId) {
    $.ajax({
      method: "POST",
      url: "/api/atom/" + atomId + "/publish",
      success: function() { alert("Published"); },
      error:   handleError
    });
  }

  return ret;

})()

// INIT bits
$(function () {
});
