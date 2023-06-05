import React from 'react';

export class Presence extends React.Component {
  state = {
    client: null,
    visitors: []
  };

  componentDidMount() {
    console.log(this.props.config)
    if (this.props.video.id) {
      this.startPresence(this.props.video.id, this.props.config);
    }
  }

  componentDidUpdate(prevProps) {
    const current = this.props.video.id;
    const previous = prevProps.video.id;

    if (current !== previous) {
      if (this.state.client) {
        this.state.client.closeConnection();
        this.setState(
          Object.assign({}, this.state, {
            client: null,
            visitors: []
          })
        );
      }

      if (current) {
        this.startPresence(current, this.props.config);
      }
    }
  }

  componentWillUnmount() {
    if (this.state.client) {
      this.state.client.closeConnection();
    }
  }

  startPresence = (atom, { domain, firstName, lastName, email }, remainingAttempts) => {
    const subscriptionId = `media-${atom}`;

    const endpoint = `wss://${domain}/socket`;

    if (!window.presenceClient && remainingAttempts !== 0){
      const newRemainingAttempts = remainingAttempts ? remainingAttempts - 1 : 5;
      setTimeout(() => { this.startPresence(atom, { domain, firstName, lastName, email }, newRemainingAttempts)}, 500);
      return;
    } else if (remainingAttempts === 0){
      console.error("Failed to connect to Presence 5 times. Will no longer attempt to connect.")
    }

    const client = window.presenceClient(endpoint, {
      firstName,
      lastName,
      email
    });

    client.startConnection();

    client.on('connection.open', () => {
      client.subscribe(subscriptionId);
      if (window.location.pathname != "/videos"){
        client.enter(subscriptionId, 'document');
      }
    });

    client.on('visitor-list-updated', data => {
      if (data.subscriptionId === subscriptionId) {
        this.setState(
          Object.assign({}, this.state, {
            visitors: data.currentState
          })
        );
      }
    });

    this.setState(
      Object.assign({}, this.state, {
        client: client
      })
    );
  };

  render() {
    console.log({visitors: this.state.visitors})
    const visitorsInThisArea = this.state.visitors.filter(
      state => state.location === 'document'
    );

    const multipleVisitors = visitorsInThisArea.length > 1;

    return (
      <section>
        <div className="presence-section">
          <ul className="presence-list">
            {visitorsInThisArea.map(visitor => {
              const id = visitor.clientId.connId;
              const { firstName, lastName } = visitor.clientId.person;
              const initials = `${firstName.slice(0, 1)}${lastName.slice(0, 1)}`;
              const fullName = `${firstName} ${lastName}`;

              return (
                <li key={id} className="presence-list__user" title={fullName}>
                  {initials}
                </li>
              );
            })}
          </ul>
        </div>
        {multipleVisitors
          ? <div className="presence-section presence-warning">
              There are multiple people in this Atom. Your changes may be overwritten!
            </div>
          : ''}
      </section>
    );
  }
}
