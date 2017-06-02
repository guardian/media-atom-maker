import React from 'react';
import ContentApi from '../../services/capi';

export default class FormFieldBylinePicker extends React.Component {
  state = {
    bylineTags: null,
    searchText: ''
  };

  processTagInput = e => {
    // only search or add the last word we've added
    const allWords = e.target.value.split(' ');
    const latestWord = allWords[allWords.length - 1];

    if (e.keyCode === 32) {
      this.setState({
        searchText: ''
      });

      const newFieldValue = this.props.fieldValue.concat([latestWord]);

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
      const newFieldValue = this.props.fieldValue.concat([tag.id]);
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

  renderValue = (fieldName, i) => {
    const removeFn = () => {
      const newFieldValue = this.props.fieldValue.filter(oldFieldName => {
        return fieldName !== oldFieldName;
      });
      this.props.onUpdateField(newFieldValue);
    };
    return (
      <span
        className="form__field--multiselect__value"
        key={`${fieldName}-${i}`}
        onClick={removeFn}
      >
        {fieldName}{' '}
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
            {this.props.fieldValue.join(', ')}
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
          ? this.props.fieldValue.map((fieldName, i) =>
              this.renderValue(fieldName, i)
            )
          : 'No tags selected'}

        <input
          type="text"
          className="form__field "
          id={this.props.fieldName}
          onKeyDown={this.processTagInput}
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
