import React from 'react';
import {Link} from 'react-router';
import AtomItem from './AtomItem';

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
          <ul className="grid__list">
            {this.renderListItems()}
          </ul>)
    } else {
        return (<p className="grid__message">No atoms found</p>)
    }
  }

  renderListItems() {
    return (
        this.state.atoms.map((atom) => <AtomItem key={atom.id} atom={atom} />)
    );
  }


  render() {
    return (
        <div>
          <div className="grid">
            {this.renderList()}
          </div>
        </div>
    )
  }
}
