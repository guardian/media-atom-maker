window.AtomUtil = (function() {
  var ret = {};

  ret.addAsset = function(atomId) {
    var uri = $("#urlInput").val();
    var version = $("#versionInput").val()
    $.ajax({
      method: "POST",
      url: "/api/atom/" + atomId + "/asset",
      data: { uri: uri, version: version },
      success: function() { window.location.reload(); },
      error: function(xhr, status, err) { alert(err + ":" + xhr.responseJSON.error); }
    });
  }

  return ret;

})()

// INIT bits
$(function () {
});
