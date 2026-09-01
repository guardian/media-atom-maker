import React from 'react';
import { PresenceConfig, safelyStartPresence } from '../services/presence';
import { Video } from "../services/VideosApi";
import { reportPresenceClientError } from "../actions/PresenceActions/reportError";

type Props = {
    video: Video;
    config: any;
    reportPresenceClientError: typeof reportPresenceClientError;
};

type State = any;

export class Presence extends React.Component<Props, State> {
  state = {
    client: null,
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
        (this.state.client as any).closeConnection();
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
      (this.state.client as any).closeConnection();
    }
  }

  startPresence = (atom: string, presenceConfig: PresenceConfig) => {
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
          // @ts-expect-error TS(18048): 'data' is possibly 'undefined'.
          if (data.subscriptionId === subscriptionId) {
            component.setState(
              Object.assign({}, component.state, {
                // @ts-expect-error TS(18048): 'data' is possibly 'undefined'.
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
    const visitorsInThisArea = this.state.visitors.filter(state => (state as any).location === 'document');

    const multipleVisitors = visitorsInThisArea.length > 1;

    return (
      <section>
        <div className="presence-section">
          <ul className="presence-list">
            {visitorsInThisArea.map(visitor => {
              const id = (visitor as any).clientId.connId;
              const { firstName, lastName } = (visitor as any).clientId.person;
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
