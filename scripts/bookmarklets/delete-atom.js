(function () {
  const regex = /\/videos\/([\w-]*)$/g;

  function isValidPage() {
    return window.location.pathname.match(regex);
  }

  function getAtomId() {
    return regex.exec(window.location.pathname)[1];
  }

  function deleteAtom(atomId) {
    const request = new Request('/api2/atom/' + atomId, {
      method: 'DELETE',
      credentials: 'same-origin'
    });

    return fetch(request);
  }

  function hasConfirmed(atomId) {
    const message = [
      'You are about to DELETE this Video Atom',
      'Any usage of it WILL BREAK',
      'Enter DELETE followed by the Atom ID to confirm.',
      'For example: DELETE a-b-c'
    ].join('\n\n');

    return prompt(message) === 'DELETE ' + atomId;
  }

  if (! isValidPage()) {
    alert('Not a Video Atom page.');
  } else {
    const atomId = getAtomId();

    if (!hasConfirmed(atomId)) {
      alert('Confirmation failed. Atom NOT deleted.');
    } else {
      deleteAtom(atomId).then(function (resp) {
        if (resp.status === 200) {
          alert('Video Atom successfully deleted.');
          window.location = '/';
        }
      }).catch(function (err) {
        console.log(err);
      });
    }
  }
})();
