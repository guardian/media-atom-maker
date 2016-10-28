import React, {PropTypes} from 'react';
import {Link} from 'react-router';

export default class VideoDetails extends React.Component {


  render() {
    return (
        <div className="video__sidebar video-details">
          <div className="video__sidebar__group">
            <dl className="details-list">

              <dt className="details-list__title">Atom ID</dt>
              <dd className="details-list__field">{this.props.video.id}</dd>

              <dt className="details-list__title">Title</dt>
              <dd className="details-list__field">{this.props.video.data.title}</dd>

              <dt className="details-list__title">Category</dt>
              <dd className="details-list__field">{this.props.video.data.category}</dd>

              <dt className="details-list__title">Duration (ms)</dt>
              <dd className="details-list__field">{this.props.video.data.duration}</dd>

              <dt className="details-list__title">Poster Image URL</dt>
              <dd className="details-list__field">{this.props.video.data.posterUrl}</dd>

              <dt className="details-list__title">Version</dt>
              <dd className="details-list__field">{this.props.video.data.activeVersion}</dd>

              <dt className="details-list__title">CAPI link</dt>
              <dd className="details-list__field">
                <a href={'https://preview.content.code.dev-guardianapis.com/atom/media/' + this.props.video.id + '?api-key=test'}>
                  /atom/media/{this.props.video.id}
                </a>
              </dd>
            </dl>
            {this.props.enableEditing ? <button className="btn" type="button" onClick={this.props.enableEditing}>Edit</button> : ''}
          </div>
        </div>
    )
  }
}
