import React from 'react';

class VideoUsages extends React.Component {

  componentDidMount() {
    if (this.props.video) {
      this.props.fetchUsages(this.props.video.id);
    }
  }

  componentWillReceiveProps(newProps) {
    const oldVidId = this.props.video && this.props.video.id;
    const newVidId = newProps.video && newProps.video.id;

    if (oldVidId !== newVidId) {
      this.updateUsages(newVidId);
    }
  }

  renderUsage(usage) {
    return (
      <li key={usage} className="detail__list__item">
        {usage}
      </li>
    )
  }

  renderUsages() {

    if (!this.props.usages) {
      return (<div className="baseline-margin">Fetching Usages...</div>)
    }

    if (!this.props.usages.length) {
      return (<div className="baseline-margin">No usages found</div>)
    }

    return (
      <ul className="detail__list">
        {this.props.usages.map(this.renderUsage)}
      </ul>
    )
  }

  render() {

    if (!this.props.video) {
      return false;
    }

    return (
      <div className="detail">
        {this.renderUsages()}
      </div>
    );
  }

}

export default VideoUsages;
