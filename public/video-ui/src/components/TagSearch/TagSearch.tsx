import React from 'react';

type Props = {
  selectedTagIndex: number;
  removeDupes: any;
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
            : nextProps.selectedTagIndex - 1);
      }
    }
  }

  renderTags(
    tag: {
      id: string;
      detailedTitle:
        | string
        | number
        | boolean
        | React.ReactElement<any, string | React.JSXElementConstructor<any>>
        | Iterable<React.ReactNode>
        | React.ReactPortal;
    },
    index: number
  ) {
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
