import React from 'react';
import ContentApi from '../../services/capi';

export default class FormFieldTagPicker extends React.Component {
  state = {
    bylineTags: null,
    searchText: ''
  };

  selectTag = id => {
    this.props.onUpdateField(id);

    this.setState({
      bylineTags: null,
      searchText: ''
    });
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
          const addTag = () => {
            this.selectTag(tag.id);
          };

          return (
            <a
              className="form__field__bylineTags"
              key={tag.id}
              title={tag.id}
              onClick={addTag}
            >
              {tag.webTitle}
            </a>
          );
        })}
      </div>
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
    if (this.props.fieldValue) {
      return (
        <div className={this.props.formRowClass || 'form__row'}>
          {this.props.fieldLabel
            ? <label htmlFor={this.props.fieldName} className="form__label">
                {this.props.fieldLabel}
              </label>
            : false}

          <input
            className="form__field"
            value={`${this.props.fieldValue}`}
            disabled={true}
          />
          <button className="btn" onClick={this.resetTag}>Change Tag</button>

        </div>
      );
    }

    return (
      <div className={this.props.formRowClass || 'form__row'}>
        {this.props.fieldLabel
          ? <label htmlFor={this.props.fieldName} className="form__label">
              {this.props.fieldLabel}
            </label>
          : false}
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
