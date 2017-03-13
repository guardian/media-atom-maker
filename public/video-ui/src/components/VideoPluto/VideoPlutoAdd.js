import React from 'react';

export default class VideoPlutoAdd extends React.Component {

  state = {
    plutoIds: ['1', '2', '3', '4', '5', '6', '7'],
    hasPlutoId: false
  }

  updatePlutoId(event) {
    this.props.video.plutoProjectId = event.target.value;
    if (event.target.value !== '') {
      this.setState({
        hasPlutoId: true
      });
    } else {
      this.setState({
        hasPlutoId: false
      });
    }
  }

  renderDefaultOption() {
    return (
      <option value="">Select pluto project id</option>
      );
  }

  render () {
    return (
      <div className="list-container">
        <select className="form__field form__field--select form__field--pluto" value={this.props.video.plutoProjectId} onChange={(event) => this.updatePlutoId(event)}>
          {this.renderDefaultOption()}
          {this.state.plutoIds.map(v =>
            {
              return (<option value={v} key={v}>{v}</option> );
            })
          };
        </select>
        <button
          type="button"
          disabled= {!this.state.hasPlutoId}
          className="btn btn--pluto"
          onClick={() => this.props.onProjectAdd(this.props.video.id, this.props.video.plutoProjectId)}
        >
          Add
        </button>
      </div>
    );
  }
}
