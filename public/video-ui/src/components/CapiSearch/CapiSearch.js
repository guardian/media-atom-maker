 import React from 'react';
import removeStringTagDuplicates from '../../util/removeStringTagDuplicates';

export default class CapiSearch extends React.Component {

  renderTags(tag) {
    const addTag = () => {

      const valueWithoutStringDupes = this.props.removeDupes(
        tag,
        this.props.tagValue
      );

      const newFieldValue = valueWithoutStringDupes.concat([tag]);

      this.props.selectNewTag(newFieldValue);

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

  render() {

    if (this.props.capiTags.length !== 0 && this.props.showTags) {
      return (
        <div className="form__field__tags" onMouseDown={this.props.tagsToVisible}>
          {this.props.capiTags.map(tag => this.renderTags(tag))}
        </div>
      );
    }

    return null;
  }
}
