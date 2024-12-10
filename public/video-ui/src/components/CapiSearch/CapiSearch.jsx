import React from 'react';

class CapiSearch extends React.Component {
  constructor(props) {
    super(props);
    this.listNode = React.createRef();
  }

  componentDidUpdate(prevProps) {
    const nextProps = this.props;

    if (
      nextProps.selectedTagIndex !== null &&
      prevProps.selectedTagIndex !== nextProps.selectedTagIndex
    ) {
      const listNode = this.listNode.current;
      if (listNode && listNode.children && listNode.children[0]) {
        const elementHeight = listNode.children[0].offsetHeight;

        listNode.scrollTop =
          elementHeight *
          (nextProps.selectedTagIndex === 0
            ? 0
            : nextProps.selectedTagIndex - 1);
      }
    }
  }

  renderTags(tag, index) {
    const getTagClassName = () => {
      return (
        'form__field__tags' +
        (index === this.props.selectedTagIndex
          ? ' form__field__tags--selected'
          : '')
      );
    };

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
        key={tag.id + index}
        title={tag.id}
        onClick={addTag}
      >
        {' '}
        {tag.detailedTitle}{' '}
      </li>
    );
  }

  render() {
    if (this.props.capiTags.length !== 0 && this.props.showTags) {
      return (
        <ul
          ref={this.listNode}
          className="form__field__tags"
          onMouseDown={this.props.tagsToVisible}
        >
          {this.props.capiTags.map((tag, index) => this.renderTags(tag, index))}
        </ul>
      );
    }

    return null;
  }
}

export default React.memo(CapiSearch);
