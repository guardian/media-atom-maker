import React from 'react';
import removeStringTagDuplicates from '../../util/removeStringTagDuplicates';

export default class CapiSearch extends React.Component {

  componentWillReceiveProps(nextProps) {
    if (nextProps.selectedTagIndex !== null && this.props.selectedTagIndex !== nextProps.selectedTagIndex) {
      const selectedTag = this.props.capiTags[nextProps.selectedTagIndex];
      const listNode = this.refs.list;
      const elementHeight = listNode.children[0].offsetHeight;
      if (listNode) {
        listNode.scrollTop = elementHeight * (nextProps.selectedTagIndex === 0 ? 0 : nextProps.selectedTagIndex - 1)
      }
    }
  }

  renderTags(tag, index) {

    const getTagClassName = () => {
      return "form__field__tags" + (index === this.props.selectedTagIndex ? " form__field__tags--selected" : "");
    }

    const addTag = () => {

      const valueWithoutStringDupes = this.props.removeDupes(
        tag,
        this.props.tagValue
      );

      const newFieldValue = valueWithoutStringDupes.concat([tag]);

      this.props.selectNewTag(newFieldValue);

    };

    return (
      <li
        className={getTagClassName()}
        key={tag.id}
        title={tag.id}
        onClick={addTag}
      >
        {' '}{tag.webTitle}{' '}
      </li>
    );
  }

  render() {

    if (this.props.capiTags.length !== 0 && this.props.showTags) {
      return (
        <ul ref="list" className="form__field__tags" onMouseDown={this.props.tagsToVisible}>
          {this.props.capiTags.map((tag, index) => this.renderTags(tag, index))}
        </ul>
      );
    }

    return null;
  }
}
