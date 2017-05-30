import React from 'react';
import ContentApi from '../../services/capi';

export default class FormFieldTagPicker extends React.Component {
  state = {
    bylineTags: null,
    searchText: ''
  };

  updateSearchBylineTags = e => {
    const searchText = e.target.value;

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
  };

  renderBylineTags(tag) {
    const addTag = () => {
      const newFieldValue = this.props.fieldValue.concat([tag.webTitle]);
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
    return (
      <div className={this.props.formRowClass || 'form__row'}>

        {this.props.fieldLabel
          ? <label htmlFor={this.props.fieldName} className="form__label">
              {this.props.fieldLabel}
            </label>
          : false}
        {this.props.fieldValue.length
          ? this.props.fieldValue.map((fieldName, i) =>
              this.renderValue(fieldName, i)
            )
          : 'No tags selected'}

        <input
          type="text"
          className="form__field "
          id={this.props.fieldName}
          onChange={this.updateSearchBylineTags}
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
