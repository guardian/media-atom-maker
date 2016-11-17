import React from 'react';
import {fetchUsages} from '../../services/capi';

class VideoUsages extends React.Component {

  state = {
    usages: undefined
  }

  updateUsages(videoId) {

    if (!videoId) {
      return this.setState({
        usages: undefined
      });
    }

    fetchUsages(videoId).then((resp) => {

      const usages = resp.response.results;

      this.setState({
        usages: usages
      });
    });
  }

  componentDidMount() {
    if (this.props.video) {
      this.updateUsages(this.props.video.id);
    }
  }

  componentWillReceiveProps(newProps) {
    const oldVidId = this.props.video && this.props.video.id;
    const newVidId = newProps.video && newProps.video.id;

    if (oldVidId === newVidId) {
      this.updateUsages(newVidId);
    }
  }

  renderUsage(usage) {
    return (
      <li className="usages__list__item">
        {usage}
      </li>
    )
  }

  renderUsages() {

    const fakeUsages = [
      'technology/2016/oct/21/unlaunched-test-article',
      'technology/2016/oct/14/testing-witness-embed',
      'technology/2013/sep/18/ios-7-review-can-apple-hold-off-the-attack-of-the-giant-screens',
    ];

    if (!this.state.usages) {
      return (<div>Fetching Usages...</div>)
    }

    if (!this.state.usages.length) {
      return (<div>No usages found</div>)
    }

    return (
      <ul className="usages__list">
        {fakeUsages.map(this.renderUsage)}
      </ul>
    )
  }

  render() {


    if (!this.props.video) {
      return false;
    }

    return (
      <div className="usages">
        <h1>Usages</h1>
        {this.renderUsages()}
      </div>
    );
  }

}

export default VideoUsages;
