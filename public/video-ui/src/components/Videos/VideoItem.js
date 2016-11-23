import React from 'react';
import {Link} from 'react-router';

export default class VideoItem extends React.Component {

  render() {
    return(
        <li className="grid__item">
          <Link className="grid__link" to={'/video/videos/' + this.props.video.id}>

            <div className="grid__info">
              <div className="grid__image">
                <img src="http://placehold.it/350x150?text=image" alt="image"/>
              </div>
              <p>{this.props.video.title}</p>
              <p>Type: media</p>
              <p>Revision: {this.props.video.contentChangeDetails.revision}</p>
              <p>No. of Assets: {this.props.video.assets.length}</p>
            </div>
          </Link>
        </li>
    )
  }
}
