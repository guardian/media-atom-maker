window.AtomUtil = (function() {
  var ret = {};

  function handleError(xhr, err) {
    alert(err + ": " + xhr.responseText);
  }

  ret.addAsset = function(atomId) {
    var uri = $("#urlInput").val();
    var mimeType = $("#mimeTypeInput").val();
    var version = $("#versionInput").val();
    $.ajax({
      method: "POST",
      url: "/api/atom/" + atomId + "/asset",
      data: {uri: uri, mimetype: mimeType, version: version},
      success: function() { window.location.reload(); },
      error: handleError
    });
  };

  ret.publishAtom = function(atomId) {
    $.ajax({
      method: "POST",
      url: "/api/atom/" + atomId + "/publish",
      success: function() { alert("Published"); },
      error:   handleError
    });
  };

  ret.createAtom = function() {
    var title = $("#titleInput").val();
    var category = $("#categoryInput").val();
    var duration = $("#durationInput").val();
    var posterUrl = $("#posterInput").val();
    $.ajax({
      method: "POST",
      url: "/api/atom",
      contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
      data: {title: title, category: category, duration: duration, posterUrl: posterUrl},
      success: function(data, status, xhr) {
        window.location = xhr.getResponseHeader("Location");
      },
      error: handleError
    });
  };

  ret.updateAtom = function (atomId) {
    var title = $("#titleInput").val();
    var category = $("#categoryInput").val();
    var duration = $("#durationInput").val();
    var posterUrl = $("#posterInput").val();
    $.ajax({
      method: "POST",
      url: "/api/atom/" + atomId,
      data: {title: title, category: category, duration: duration, posterUrl: posterUrl},
      success: function (data, status, xhr) {
        window.location = xhr.getResponseHeader("Location");
      },
      error: handleError
    });
  };

  ret.revertAtom = function(atomId, version) {
    $.ajax({
      method: "POST",
      url: "/api/atom/" + atomId + "/revert/" + version,
      contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
      success: function () {
        window.location = "/atom/" + atomId;
      },
      error: handleError
    });
  };

  return ret;

})();

// INIT bits
$(function () {
});
