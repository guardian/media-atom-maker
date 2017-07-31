import React from 'react';
import ContentApi from '../../services/capi';
import { tagsFromStringList, tagsToStringList } from '../../util/tagParsers';
import { keyCodes } from '../../constants/keyCodes';
import UserActions from '../../constants/UserActions';
import TagTypes from '../../constants/TagTypes';
import getTagDisplayNames from '../../util/getTagDisplayNames';
import TextInputTagPicker from './TextInputTagPicker';

export default class TagPicker extends React.Component {
  state = {
    capiTags: [],
    inputString: '',
    tagValue: [],
    capiUnavailable: false,
    showTags: true,
    tagsVisible: false
  };

  componentDidMount() {
    if (this.props.fieldValue !== this.props.placeholder) {
      tagsFromStringList(this.props.fieldValue, this.props.tagType)
        .then(result => {
          this.setState({
            tagValue: result
          });
        })
        .catch(() => {
          // capi is unavailable and we cannot get webtitles for tags
          this.setState({
            tagValue: this.props.fieldValue.slice(),
            capiUnavailable: true
          });
        });
    }
  }

  fetchTags = searchText => {
    ContentApi.getTagsByType(searchText, this.props.tagType)
      .then(capiResponse => {
        const tags = getTagDisplayNames(capiResponse.response.results, this.props.tagType);
        this.setState({
          capiTags: tags
        });
      })
      .catch(() => {
        this.setState({
          capiTags: [],
          capiUnavailable: true
        });
      });
  }

  onUpdate = newValue => {
    this.setState({
      tagValue: newValue
    });
    this.props.onUpdateField(tagsToStringList(newValue));

    //TODO: does setting this at all times produce weird
    //behaviour when deleting bylines?
    //Do we need to displayAnbele this when deleting/
    this.setState({
      capiTags: []
    });
  };

  render() {

    return (
      <TextInputTagPicker
        tagValue={this.state.tagValue}
        capiUnavailable={this.state.capiUnavailable}
        onUpdate={this.onUpdate}
        fetchTags={this.fetchTags}
        capiTags={this.state.capiTags}
        selectNewTag={this.selectNewTag}
        {...this.props}
      />
    );
  }
}
