import React from 'react';
import ContentApi from '../../services/capi';
import { tagsFromStringList, tagsToStringList } from '../../util/tagParsers';
import { keyCodes } from '../../constants/keyCodes';
import UserActions from '../../constants/UserActions';
import TagTypes from '../../constants/TagTypes';
import getTagDisplayNames from '../../util/getTagDisplayNames';
import TextInputTagPicker from './TextInputTagPicker';
import TagFieldValue from '../Tags/TagFieldValue';
import CapiUnavailable from '../CapiSearch/CapiUnavailable';

export default class TagPicker extends React.Component {

  state = {
    capiTags: [],
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

    this.setState({
      capiTags: []
    });
  };

  hideTagResults = (e) => {

    // For each tag picker component, there is a tagsVisible state variable.
    // The onBlur event attached to the tag picker gets fired when
    // any of its children are clicked. This variable is used to check if the event
    // was fired by clicking on one of the child elements and makes sure that this
    // does not hide the tag search results.

    const tagsVisible = this.state.tagsVisible;

    if (!tagsVisible) {
      this.setState({
        showTags: false,
      });
    } else {
      this.setState({
        tagsVisible: false
      });
    }
  }

  showTagResults = () => {
    this.setState({
      showTags: true
    });
  }

  tagsToVisible = () => {
    this.setState({
      tagsVisible: true
    });
  }


  render() {

    if (!this.props.editable) {
      if (!this.state.tagValue || this.state.tagValue.length === 0) {
        return (
          <div>
            <p className="details-list__title">{this.props.fieldName}</p>
            <p className={'details-list__field details-list__empty'}>
              {this.props.placeholder}
            </p>
          </div>
        );
      }
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <CapiUnavailable capiUnavailable={this.state.capiUnavailable} />
          <p className="details-list__field ">
            <TagFieldValue tagValue={this.state.tagValue}/>
          </p>
        </div>
      );
    }

    return (
      <div>
        <CapiUnavailable capiUnavailable={this.state.capiUnavailable} />
        <TextInputTagPicker
          tagValue={this.state.tagValue}
          onUpdate={this.onUpdate}
          fetchTags={this.fetchTags}
          capiTags={this.state.capiTags}
          tagsToVisible={this.tagsToVisible}
          showTagResults={this.showTagResults}
          showTags={this.state.showTags}
          hideTagResults={this.hideTagResults}

          {...this.props}
        />
      </div>
    );
  }
}
