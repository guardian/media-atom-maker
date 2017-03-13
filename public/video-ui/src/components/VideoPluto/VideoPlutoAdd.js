import React from 'react';

export default class VideoPlutoAdd extends React.Component {

  state = {
    plutoIds: ['1', '2', '3', '4', '5', '6', '7'],
    hasPlutoId: false
  }

  updatePlutoId(event) {
    this.props.video.plutoProjectId = event.target.value;
    this.setState({
      hasPlutoId: true
    });
  }

  render () {
    return (
      <div>
        <select value={this.props.video.plutoProjectId} onChange={(event) => this.updatePlutoId(event)}>
          {this.state.plutoIds.map(v =>
            {
              return (<option value={v} key={v}>{v}</option> );
            })
          };
        </select>
        <button
          type="button"
          disabled= {!this.state.hasPlutoId}
          className="btn"
          onClick={() => this.props.onProjectAdd(this.props.video.id, this.props.video.plutoProjectId)}
        >
          Add Pluto Video
        </button>
      </div>
    );
  }
}
