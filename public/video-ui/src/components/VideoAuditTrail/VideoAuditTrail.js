import React, {PropTypes}  from 'react';
import moment from 'moment';

export default class VideoAuditTrail extends React.Component {

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
    const audits = this.props.audits.map(x => x).sort((a, b) => b.date - a.date);

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
