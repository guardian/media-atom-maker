import React from 'react';

class AtomEdit extends React.Component {

  constructor(props){
    super(props);
    this.state = {
      atom: {
        fields: {}
      }
    };
  }

  componentWillMount() {
    this.props.atomActions.getAtom(this.props.params.id);
  }

  render() {
    const atom = this.props.atom && this.props.params.id === this.props.atom.id ? this.props.atom : undefined;
    console.log(atom);
    if (!atom) {
      return <div>Loading... </div>;
    }

    return (
        <div>
          <h2>{atom.data.title}</h2>
          <div>
            <p>Type: {atom.type}</p>
            <p>Revision: {atom.contentChangeDetails.revision}</p>
            <p>No. of Assets: {atom.data.assets.length}</p>
          </div>
        </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getAtom from '../../actions/AtomActions/getAtom';

function mapStateToProps(state) {
  return {
    atom: state.atom
  };
}

function mapDispatchToProps(dispatch) {
  return {
    atomActions: bindActionCreators(Object.assign({}, getAtom), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(AtomEdit);

