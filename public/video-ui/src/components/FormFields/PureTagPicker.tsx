import React from 'react';
import TagSearch from '../TagSearch/TagSearch';
import { removeTagDuplicates } from '../../util/removeTagDuplicates';

type Props = {
    tagValue: any[];
    fetchTags: (...args: any[]) => any;
    onUpdate: (...args: any[]) => any;
    searchResultTags: any[];
    tagsToVisible: (...args: any[]) => any;
    showTags: boolean;
    hideTagResults: (...args: any[]) => any;
    selectedTagIndex?: number;
    inputClearCount: number;
    inputPlaceholder: string;
    updateSideEffects?: (...args: any[]) => any;
};

type State = {
    inputString: any;
};

class PureTagPicker extends React.Component<Props, State> {

  state: State = {
    inputString: ''
  };

  componentDidUpdate(prevProps: Props) {
    if (prevProps.inputClearCount !== this.props.inputClearCount) {
      this.setState({
        inputString: ''
      });
    }
  }

  updateInput = (e: { target: { value: any; }; }) => {
    const searchText = e.target.value;
    this.props.fetchTags(searchText);
    this.setState({
      inputString: searchText
    });
  };

  selectNewTag = (newFieldValue: any) => {
    this.setState({
      inputString: ''
    });

    this.props.onUpdate(newFieldValue);
  };

  render() {
    return (
      <div>
        <input
          type="text"
          className="form__field"
          onChange={this.updateInput}
          placeholder={this.props.inputPlaceholder}
          value={this.state.inputString}
        />

        <TagSearch
          searchResultTags={this.props.searchResultTags}
          showTags={this.props.showTags}
          tagsToVisible={this.props.tagsToVisible}
          selectNewTag={this.selectNewTag}
          tagValue={this.props.tagValue}
          // @ts-expect-error TS(2322): Type '<T extends Tag>(tag: Tag, tagValue: T[]) => ... Remove this comment to see the full error message
          removeDupes={removeTagDuplicates}
          selectedTagIndex={this.props.selectedTagIndex}
        />
      </div>
    );
  }
}

export default React.memo(PureTagPicker);
