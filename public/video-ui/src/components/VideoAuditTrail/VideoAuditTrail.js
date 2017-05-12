import React from 'react';
import moment from 'moment';

class VideoAuditTrail extends React.Component {
  componentWillReceiveProps(nextProps) {
    if (nextProps.video != this.props.video) {
      this.props.videoActions.getAudits(nextProps.video.id);
    }
  }

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
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
    const audits = this.props.audits
      .map(x => x)
      .sort((a, b) => b.date - a.date);
    return <tbody>{audits.map(a => this.renderAudit(a))}</tbody>;
  }

  render() {
    if (this.props.audits) {
      return (
        <div>
          <div className="video__detail__page">
            <span className="video__detailbox__header">Atom Audit Trail</span>
          </div>
          <div>
            <table className="table">
              <thead className="table__header">
                <tr className="table__header-row">
                  <th>Date</th>
                  <th>Operation</th>
                  <th>Description</th>
                  <th>User</th>
                </tr>
              </thead>
              {this.renderList()}
            </table>
          </div>
        </div>
      );
    }

    return <div>Loading...</div>;
  }
}
//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as getAudits from '../../actions/VideoActions/getAudits';
import * as updateVideo from '../../actions/VideoActions/updateVideo';

function mapStateToProps(state) {
  return {
    video: state.video,
    audits: state.audits
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(
      Object.assign({}, getVideo, getAudits, updateVideo),
      dispatch
    )
  };
}
export default connect(mapStateToProps, mapDispatchToProps)(VideoAuditTrail);
