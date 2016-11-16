import React, {PropTypes} from 'react';
import {Link} from 'react-router';
import {findSmallestAsset} from '../../util/imageHelpers';

export default class VideoDetails extends React.Component {

  renderPosterImage() {
    if (!this.props.video.data.posterImage) {
      return <dd className="details-list__field">No Image Selected</dd>
    }

    const image = findSmallestAsset(this.props.video.data.posterImage.assets)

    return (
       <dd className="details-list__field">
         <img src={image.url}/>
       </dd>
    );
  }

  render() {
    return (
        <div className="video__sidebar video-details">
          <div className="video__sidebar__group">
            <dl className="details-list">

              <dt className="details-list__title">Title</dt>
              <dd className="details-list__field">{this.props.video.data.title}</dd>

              <dt className="details-list__title">Category</dt>
              <dd className="details-list__field">{this.props.video.data.category}</dd>

              <dt className="details-list__title">Poster Image</dt>
              {this.renderPosterImage()}

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
            <button className="btn" onClick={this.props.onPublishVideo}>Publish video</button>
          </div>
        </div>
    )
  }
}
