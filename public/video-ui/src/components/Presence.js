import React from 'react';

export class Presence extends React.Component {
  state = null;

  componentDidMount() {
    if (this.props.video.id) {
      this.startPresence(this.props.video.id, this.props.config);
    }
  }

  componentDidUpdate(prevProps) {
    const current = this.props.video.id;
    const previous = prevProps.video.id;

    if (current !== previous) {
      if (this.state) {
        this.state.closeConnection();
        this.setState(null);
      }

      if (current) {
        this.startPresence(current, this.props.config);
      }
    }
  }

  componentWillUnmount() {
    if (this.state) {
      this.state.closeConnection();
    }
  }

  startPresence = (atom, { firstName, lastName, email }) => {
    const endpoint = `wss://${this.props.domain}/socket`;

    const client = window.presenceClient(endpoint, {
      firstName,
      lastName,
      email
    });

    client.startConnection();

    client.on('connection.open', () => {
      client.subscribe(`media-${atom}`);
      client.enter(`media-${atom}`, 'document');
    });

    this.setState(client);
  };

  render() {
    // No indicator in the UI yet, just reporting back for use in Workflow
    return false;
  }
}
