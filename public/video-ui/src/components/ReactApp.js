import React from 'react';

import Header from './Header';

class ReactApp extends React.Component {
  constructor(props) {
    super(props);
  }

  updateSearchTerm = (searchTerm) => {
    this.props.updateSearchTermActions.updateSearchTerm(searchTerm);
  }

  render() {
    return (
        <div className="wrap">
          <Header updateSearchTerm={this.updateSearchTerm} searchTerm={this.props.searchTerm} />
          <div>
            {this.props.children}
          </div>
        </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as updateSearchTerm from '../actions/SearchActions/updateSearchTerm';

function mapStateToProps(state) {
  return {
    searchTerm: state.searchTerm
  };
}

function mapDispatchToProps(dispatch) {
  return {
    updateSearchTermActions: bindActionCreators(Object.assign({}, updateSearchTerm), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(ReactApp)
