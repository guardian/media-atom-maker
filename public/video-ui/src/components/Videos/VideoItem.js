import React from 'react';
import {Link} from 'react-router';

export default class VideoItem extends React.Component {

  render() {
    return(
        <li className="grid__item">
          <Link className="grid__link" to={'/video/videos/' + this.props.video.id}>

            <div className="grid__info">
              <p>{this.props.video.data.title}</p>
              <p>Type: {this.props.video.type}</p>
              <p>Revision: {this.props.video.contentChangeDetails.revision}</p>
              <p>No. of Assets: {this.props.video.data.assets.length}</p>
            </div>
          </Link>
        </li>
    )
  }
}
