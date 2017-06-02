import React from 'react';
import ContentApi from '../../services/capi';

export default class FormFieldBylinePicker extends React.Component {
  state = {
    bylineTags: null,
    searchText: '',
    inputString: this.fieldValueToString(this.props.fieldValue)
  };

  updateInput = e => {
    const newVal = e.target.value;
    const appendSpace = newVal[newVal.length - 1] == ' ';
    const valueAsArray = newVal.split(' ');
    if (appendSpace) {
      const lastElem = valueAsArray[valueAsArray.length - 1];
      valueAsArray[valueAsArray.length - 1] = lastElem + ' ';
    }
    this.setState({
      inputString: this.fieldValueToString(valueAsArray)
    });
  };

  processTagInput = e => {
    // only search or add the last word we've added
    const allWords = e.target.value.split(' ');

    const latestWord = allWords[allWords.length - 1];

    if (e.keyCode === 32) {
      this.setState({
        searchText: ''
      });

      const newFieldValue = this.props.fieldValue.concat([latestWord + ' ']);
      this.setState({
        inputString: this.fieldValueToString(newFieldValue)
      });

      this.props.onUpdateField(newFieldValue);
    } else {
      const searchText = latestWord;

      ContentApi.getBylineTags(searchText)
        .then(capiResponse => {
          const bylines = capiResponse.response.results.map(result => {
            const tags = { id: result.id, webTitle: result.webTitle };
            return tags;
          });
          this.setState({
            bylineTags: bylines,
            searchText: ''
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

  renderBylineTags(tag) {
    const addTag = () => {
      const newFieldValue = this.props.fieldValue.concat([tag]);

      this.setState({
        inputString: this.fieldValueToString(newFieldValue)
      });

      this.props.onUpdateField(newFieldValue);
      this.setState({
        bylineTags: null,
        searchText: ''
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
    return <span key={`${field.id}-${i}`}> {field}{' '}</span>;
  };

  fieldValueToString(fieldValue) {
    const concatenatedValues = fieldValue.reduce((values, value) => {
      if (value.webTitle) {
        values += value.webTitle + ' ';
      } else {
        values += value + ' ';
      }
      return values;
    }, '');
    return concatenatedValues.substring(0, concatenatedValues.length - 1);
  }

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
            {this.fieldValueToString(this.props.fieldValue)}
          </p>
        </div>
      );
    }

    return (
      <div className={this.props.formRowClass || 'form__row'}>

        <div className="form__label__layout">
          <label className="form__label">{this.props.fieldName}</label>
        </div>
        {this.props.fieldValue.length
          ? this.props.fieldValue.map((value, i) => this.renderValue(value, i))
          : 'No tags selected'}

        <input
          type="text"
          className="form__field "
          id={this.props.fieldName}
          onKeyDown={this.processTagInput}
          onChange={this.updateInput}
          value={this.state.inputString}
        />

        {this.state.bylineTags
          ? <div className="form__field__tags">
              {this.state.bylineTags.map(tag => this.renderBylineTags(tag))}
            </div>
          : ''}

      </div>
    );
  }
}
