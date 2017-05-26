import React from 'react';
import ContentApi from '../../services/capi';

export default class FormFieldTagPicker extends React.Component {
  state = {
    bylineTags: null,
    searchText: ''
  };

  resetTag = () => {
    this.props.onUpdateField(undefined);
  };

  updateSearchBylineTags = e => {
    const searchText = e.target.value;

    this.setState({
      searchText: searchText
    });

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
  };

  addTag = (tag) => {
    console.log(this.props)
    let newFieldValue = this.props.fieldValue.concat([tag.id]);
    this.props.onUpdateField(newFieldValue);
    this.setState({
      bylineTags: null,
      searchText: ''
    });

  };

  removeFn = () => {
    const newFieldValue = this.props.fieldValue.filter(oldFieldName => {
        return fieldName !== oldFieldName;
    });
    this.props.onUpdateField(newFieldValue);
    this.setState({
      bylineTags: null,
      searchText: ''
    });
  };

  renderBylineTags() {
    if (!this.state.bylineTags) {
      return false;
    }

    if (!this.state.bylineTags.length) {
      return <div>No tags found</div>;
    }

    return (
      <div className="form__field__bylineTags">
        {this.state.bylineTags.map(tag => {



          return (
            <a
              className="form__field__bylineTags"
              key={tag.id}
              title={tag.id}
              onClick={this.addTag(tag)}
            >
              {tag.webTitle}
            </a>
          );
        })}
      </div>
    );
  }

  renderValue = (fieldName, i) => {


    return (
      <span
        className="form__field--multiselect__value"
        key={`${fieldName}-${i}`}
        onClick={this.removeFn()}
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
        {this.props.fieldValue.length ? this.props.fieldValue.map((fieldName, i) => this.renderValue(fieldName, i)) : 'No items selected'}


        <input
          type="text"
          className="form__field "
          id={this.props.fieldName}
          onChange={this.updateSearchBylineTags}
          value={this.state.searchText}
        />
        {this.renderBylineTags()}
      </div>
    );
  }
}
