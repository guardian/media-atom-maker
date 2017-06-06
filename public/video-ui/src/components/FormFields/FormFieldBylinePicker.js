import React from 'react';
import ContentApi from '../../services/capi';

export default class FormFieldBylinePicker extends React.Component {
  state = {
    bylineTags: null,
    inputString: '',
    lastAction: 'OTHER'
  };

  updateInput = e => {
    if (this.state.lastAction === 'SPACE') {
      const newFieldValue = this.props.fieldValue.concat([
        this.state.inputString
      ]);
      this.props.onUpdateField(newFieldValue);
      this.setState({
        inputString: ''
      });

      // If the user did not add new text input, we update the tag search
    } else if (this.state.lastAction === 'DELETE') {
      const length = this.props.fieldValue.length;
      const lastInput = this.props.fieldValue[length - 1];

      this.setState({
        inputString: lastInput,
        lastAction: 'OTHER'
      });

      const newValue = this.props.fieldValue.slice(
        0,
        this.props.fieldValue.length - 1
      );
      this.props.onUpdateField(newValue);
    } else {
      this.setState({
        inputString: e.target.value
      });

      const searchText = e.target.value;

      ContentApi.getBylineTags(searchText)
        .then(capiResponse => {
          const bylines = capiResponse.response.results.map(result => {
            const tags = { id: result.id, webTitle: result.webTitle };
            return tags;
          });
          this.setState({
            bylineTags: bylines
          });
          return;
        })
        .catch(() => {
          this.setState({
            bylineTags: null
          });
        });
    }
  };

  processTagInput = e => {
    if (e.keyCode === 32) {
      this.setState({
        lastAction: 'SPACE'
      });
    } else if (e.keyCode === 8) {
      if (this.state.inputString.length === 0) {
        const lastInput = this.props.fieldValue[
          this.props.fieldValue.length - 1
        ];

        if (typeof lastInput === 'string') {
          //User is trying to delete a string input
          this.setState(
            {
              lastAction: 'DELETE'
            },
            () => {
              this.updateInput();
            }
          );
        }
      }
    } else {
      this.setState({
        lastAction: 'OTHER'
      });
    }
  };

  fieldValueToString(value, index) {
    if (value.webTitle) {
      return (
        <span key={`${value.id}-${index}`}>
          <span className="form__field__tag__display">{value.webTitle}</span>
          {' '}
        </span>
      );
    } else {
      return `${value} `;
    }
  }

  renderBylineTags(tag) {
    const addTag = () => {
      const newFieldValue = this.props.fieldValue.concat([tag]);

      this.setState({
        inputString: ''
      });

      this.props.onUpdateField(newFieldValue);
      this.setState({
        bylineTags: null
      });
    };

    return (
      <a
        className="form__field__tags"
        key={tag.id}
        title={tag.id}
        onClick={addTag}
      >
        {' '}{tag.webTitle}{' '}
      </a>
    );
  }

  renderValue = (field, i) => {
    const removeFn = () => {
      const newFieldValue = this.props.fieldValue.filter(oldField => {
        return field.id !== oldField.id;
      });

      this.setState({
        inputString: ''
      });
      this.props.onUpdateField(newFieldValue);
    };

    if (field.id) {
      return (
        <span
          className="form__field--multiselect__value"
          key={`${field.id}-${i}`}
          onClick={removeFn}
        >
          {field.webTitle}{' '}
        </span>
      );
    }
    return (
      <span
        className="form__field--multistring__value"
        key={`${field.id}-${i}`}
      >
        {' '}{field}{' '}
      </span>
    );
  };

  render() {
    if (!this.props.editable) {
      if (!this.props.fieldValue || this.props.fieldValue.length === 0) {
        return (
          <div>
            <p className="details-list__title">{this.props.fieldName}</p>
            <p className={'details-list__field details-list__empty'}>
              {this.props.placeholder}
            </p>
          </div>
        );
      }
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <p className="details-list__field ">
            {this.props.fieldValue.map(this.fieldValueToString)}
          </p>
        </div>
      );
    }

    return (
      <div className="form__row">

        <div className="form__label__layout">
          <label className="form__label">{this.props.fieldName}</label>
        </div>

        <div className="form__field__byline">
          {this.props.fieldValue.length
            ? this.props.fieldValue.map((value, i) =>
                this.renderValue(value, i)
              )
            : ''}

          <input
            type="text"
            className="form__field__byline--input"
            id={this.props.fieldName}
            onKeyDown={this.processTagInput}
            onChange={this.updateInput}
            value={this.state.inputString}
          />
        </div>

        {this.state.bylineTags && this.state.bylineTags.length !== 0
          ? <div className="form__field__tags">
              {this.state.bylineTags.map(tag => this.renderBylineTags(tag))}
            </div>
          : ''}

        {this.props.fieldValue.map(this.fieldValueToString)}

      </div>
    );
  }
}
