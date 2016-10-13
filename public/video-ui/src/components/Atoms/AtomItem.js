import React from 'react';
import {Link} from 'react-router';

export default class AtomItem extends React.Component {

  render() {
    return(
        <li className="grid__item">
          <Link className="grid__link" to={'/video/atoms/' + this.props.atom.id}>

            <div className="grid__info">
              <p>{this.props.atom.data.title}</p>
              <p>Type: {this.props.atom.type}</p>
              <p>Revision: {this.props.atom.contentChangeDetails.revision}</p>
              <p>No. of Assets: {this.props.atom.data.assets.length}</p>
            </div>
          </Link>
        </li>
    )
  }
}
