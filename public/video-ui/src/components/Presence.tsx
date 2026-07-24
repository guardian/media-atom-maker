import React from 'react';
import { PresenceConfig, safelyStartPresence } from '../services/presence';
import { reportPresenceClientError } from '../actions/PresenceActions/reportError';

type Props = {
  video: any;
  config: any;
  reportPresenceClientError: typeof reportPresenceClientError;
};

type State = {
  client: null;
  visitors: any[];
};

export class Presence extends React.Component<Props, State> {
  state = {
    // @ts-expect-error TS(7018): Object literal's property 'client' implicitly has ... Remove this comment to see the full error message
    client: null,
    // @ts-expect-error TS(7018): Object literal's property 'visitors' implicitly ha... Remove this comment to see the full error message
    visitors: []
  };

  componentDidMount() {
    if (this.props.video.id) {
      this.startPresence(this.props.video.id, this.props.config);
    }
  }

  componentDidUpdate(prevProps: Props) {
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

      if (current && window.presenceClient) {
        this.startPresence(current, this.props.config);
      }
    }
  }

  componentWillUnmount() {
    if (this.state.client) {
      this.state.client.closeConnection();
    }
  }

  startPresence = (atom: any, presenceConfig: PresenceConfig) => {
    const subscriptionId = `media-${atom}`;
    const component = this;

    safelyStartPresence(
      presenceClient => {
        presenceClient.startConnection();

        presenceClient.on('connection.open', () => {
          // @ts-expect-error TS(2345): Argument of type 'string' is not assignable to par... Remove this comment to see the full error message
          presenceClient.subscribe(subscriptionId);
          (presenceClient as any).enter(subscriptionId, 'document');
        });

        presenceClient.on('visitor-list-updated', data => {
          if (data.subscriptionId === subscriptionId) {
            component.setState(
              Object.assign({}, component.state, {
                visitors: data.currentState
              })
            );
          }
        });

        component.setState(
          Object.assign({}, component.state, {
            client: presenceClient
          })
        );
      },
      this.props.reportPresenceClientError,
      presenceConfig
    );
  };

  render() {
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
        {multipleVisitors ? (
          <div className="presence-section presence-warning">
            There are multiple people in this Atom. Your changes may be
            overwritten!
          </div>
        ) : (
          ''
        )}
      </section>
    );
  }
}
