import React from 'react';

class PresencePublisher{
  constructor(state, setState, config, videos){
      this.state = state;
      this.setState = setState;
      this.config = config
  }
  startPresence = ({ domain, firstName, lastName, email }, remainingAttempts) => {
    const subscriptionIds = videos.map(video => `media-${video.id}`)

    const endpoint = `wss://${domain}/socket`;

    if (!window.presenceClient && remainingAttempts !== 0){
      const newRemainingAttempts = remainingAttempts ? remainingAttempts - 1 : 5;
      setTimeout(() => { this.startPresence({ domain, firstName, lastName, email }, newRemainingAttempts)}, 500);
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
      client.subscribe(subscriptionIds);
    });

    client.on('visitor-list-updated', data => {
      if (data.subscriptionId === subscriptionId) {
        this.setState(
          Object.assign({}, this.state, {
            subscriptions: [...this.state.subcriptions, {id: data.subscriptionId, currentState: data.currentState }]
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
}
