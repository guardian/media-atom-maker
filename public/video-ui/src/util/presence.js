export class Presence {
  constructor(id, { domain, firstName, lastName, email }) {
    const endpoint = `wss://${domain}/socket`;
    const user = { firstName, lastName, email };

    this.client = window.presenceClient(endpoint, user);
    this.client.startConnection();

    this.client.on('connection.open', () => {
      this.client.subscribe(`media-${id}`);
      this.client.enter(`media-${id}`, 'document');
    });
  }

  close() {
    this.client.closeConnection();
  }
}
