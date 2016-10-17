import React, {PropTypes} from 'react';
import {Link} from 'react-router';
import AtomItem from './AtomItem';

class Atoms extends React.Component {

  static propTypes = {
    atoms: PropTypes.array.isRequired,
  }

  componentDidMount() {
    this.props.atomActions.getAtoms();
  }

  renderList() {
    if(this.props.atoms.length) {
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
        this.props.atoms.map((atom) => <AtomItem key={atom.id} atom={atom} />)
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


//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getAtoms from '../../actions/AtomActions/getAtoms';

function mapStateToProps(state) {
  return {
    atoms: state.atoms
  };
}

function mapDispatchToProps(dispatch) {
  return {
    atomActions: bindActionCreators(Object.assign({}, getAtoms), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Atoms);
