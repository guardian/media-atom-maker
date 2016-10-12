import React from 'react';
import {getAtom} from '../../services/AtomsApi';

export default class AtomEdit extends React.Component {

  constructor(props){
    super(props);
    this.state = {
      atom: {
        fields: {}
      }
    };
  }

  componentDidMount() {
    this.getAtom(this.props.routeParams.id);
  }

  getAtom(atomId) {
    getAtom(atomId).then((atom) => {
      this.setState({
        atom: atom
      });
    });
  }

  render() {
    return (
        <h1>{this.state.atom.id}</h1>
    );
  }
}
