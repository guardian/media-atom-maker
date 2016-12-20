import React, {PropTypes}  from 'react';
import moment from 'moment';

class VideoAuditTrail extends React.Component {

  componentDidMount() {
    if (this.props.video) {
      this.props.videoActions.getAudits(this.props.video.id);
    }
  }

  state = {
    renderAll: false
  };

  showAll = () => {
    this.setState({
      renderAll: true
    });
  };

  renderAudit(audit) {
      const itemTime = moment(audit.date, 'x');

      return (
        <tr key={audit.date}>
          <td>{itemTime.format('HH:mm:ss DD/MM/YYYY')}</td>
          <td>{audit.operation}</td>
          <td>{audit.description}</td>
          <td>{audit.user}</td>
        </tr>
      );
  }

  renderList() {
    const audits = this.props.audits.map(x => x).sort((a, b) => {
      if (a.date < b.date) {
        return 1;
      }
      if (a.date > b.date) {
        return -1;
      }
      return 0;
    });

    if (this.state.renderAll) {
      return (<tbody>{audits.map((a) => this.renderAudit(a))}</tbody>);
    } else {
      return (<tbody>{audits.slice(0, 5).map((a) => this.renderAudit(a))}</tbody>);
    }
  }

  renderExpandButton() {
      return (<button className="video-assets__show-btn" type="button" onClick={this.showAll}>Show all audits</button>);
  }

  render() {
    if (this.props.audits) {
      return (
        <div>
          <table className='table'>
            <thead className='table__header'>
              <tr className='table__header-row'>
                <th>Date</th>
                <th>Operation</th>
                <th>Description</th>
                <th>User</th>
              </tr>
            </thead>
            {this.renderList()}
          </table>
          {!this.state.renderAll && this.props.audits.length > 5 ? this.renderExpandButton() : false}
        </div>
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
