import React, {PropTypes}  from 'react';
import moment from 'moment';

class VideoAuditTrail extends React.Component {

  componentDidMount() {
    if (this.props.video) {
      this.props.videoActions.getAudits(this.props.video.id);
    }
  }

  renderAudit(audit) {
      const itemTime = moment(audit.date, 'x');

      return (
        <tbody key={audit.date}>
          <tr>
            <td>{itemTime.format('HH:mm:ss DD/MM/YYYY')}</td>
            <td>{audit.operation}</td>
            <td>{audit.description}</td>
            <td>{audit.user}</td>
          </tr>
        </tbody>);
  }

  render() {
    if (this.props.audits) {
      return (
        <table className='table'>
          <thead className='table__header'>
            <tr className='table__header-row'>
              <th>Date</th>
              <th>Operation</th>
              <th>Description</th>
              <th>User</th>
            </tr>
          </thead>
          {this.props.audits.map((a) => this.renderAudit(a))}
        </table>
      );
    }

    return (<div>Loading...</div>);
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getAudits from '../../actions/VideoActions/getAudits';

function mapStateToProps(state) {
  console.log(state);
  return {
    audits: state.audits
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getAudits), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoAuditTrail);
