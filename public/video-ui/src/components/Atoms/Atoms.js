import React from 'react';
import {Link} from 'react-router';

import {getAtoms} from '../../services/AtomsApi'

export default class Atoms extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      atoms: []
    };
  }

  componentDidMount() {
    this.populateAtoms()
  }

  populateAtoms() {
    getAtoms().then((atoms) => {
      this.setState({
        atoms: atoms
      });
    });
  }

  renderList() {
    if(this.state.atoms.length) {
      return (
          <ul>
            {this.renderListItems()}
          </ul>)
    } else {
        return (<p>No atoms found</p>)
    }
  }

  renderListItems() {
    return (
        this.state.atoms.map((atom) => <li key={atom.id}><Link to={'/beta/atoms/' + atom.id}>{atom.data.title}</Link>, {atom.type}, {atom.contentChangeDetails.revision}, {atom.data.assets.length}</li>)
    );
  }


  render() {
    return (
        <div>
          <h1>Atoms</h1>
          <div>
            {this.renderList()}
          </div>
        </div>
    )
  }
}
