import React from 'react';

type Props = {
  selectedTagIndex: number | undefined;
  removeDupes: any[] | any[];
  tagValue: any[];
  selectNewTag: any;
  searchResultTags: any[];
  showTags: boolean;
  tagsToVisible: any;
};

class TagSearch extends React.Component<Props> {
  listNodeRef: any;
  constructor(props: Props) {
    super(props);
    this.listNodeRef = React.createRef();
  }

  componentDidUpdate(prevProps: Props) {
    const nextProps = this.props;

    if (
      nextProps.selectedTagIndex !== null &&
      prevProps.selectedTagIndex !== nextProps.selectedTagIndex
    ) {
      if (this.listNodeRef.current) {
        const elementHeight = this.listNodeRef.current.children[0].offsetHeight;
        this.listNodeRef.current.scrollTop =
          elementHeight *
          (nextProps.selectedTagIndex === 0
            ? 0
            : // @ts-expect-error TS(18048): 'nextProps.selectedTagIndex' is possibly 'undefine... Remove this comment to see the full error message
              nextProps.selectedTagIndex - 1);
      }
    }
  }

  renderTags(tag: any, index: any) {
    const getTagClassName = () => {
      return (
        'form__field__tags' +
        (index === this.props.selectedTagIndex
          ? ' form__field__tags--selected'
          : '')
      );
    };

    const addTag = () => {
      // @ts-expect-error TS(2349): This expression is not callable.
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
    if (this.props.searchResultTags.length !== 0 && this.props.showTags) {
      return (
        <ul
          ref={this.listNodeRef}
          className="form__field__tags"
          onMouseDown={this.props.tagsToVisible}
        >
          {this.props.searchResultTags.map((tag, index) =>
            this.renderTags(tag, index)
          )}
        </ul>
      );
    }

    return null;
  }
}

export default React.memo(TagSearch);
