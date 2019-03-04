import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import DurationInput from '../FormFields/DurationInput';
import ScribeEditorField from '../FormFields/ScribeEditor';
import SelectBox from '../FormFields/SelectBox';
import DatePicker from '../FormFields/DatePicker';
import TagPicker from '../FormFields/TagPicker';
import TagTypes from '../../constants/TagTypes';
import { fieldLengths } from '../../constants/videoEditValidation';
import { videoCategories } from '../../constants/videoCategories';
import VideoUtils from '../../util/video';

class VideoData extends React.Component {
  hasCategories = () => this.props.youtube.categories.length !== 0;
  hasChannels = () => this.props.youtube.channels.length !== 0;

  componentWillMount() {
    if (!this.hasCategories()) {
      this.props.youtubeActions.getCategories();
    }
    if (!this.hasChannels()) {
      this.props.youtubeActions.getChannels();
    }
  }

  render() {
    const isCommercialType = VideoUtils.isCommercialType(this.props.video);
    const hasAssets = VideoUtils.hasAssets(this.props.video);

    return (
      <div className="form__group">
        <ManagedForm
          data={this.props.video}
          updateData={this.props.updateVideo}
          editable={this.props.editable}
          updateErrors={this.props.updateErrors}
          updateWarnings={this.props.updateWarnings}
          formName={this.props.formName}
          formClass="atom__edit__form"
        >
          <ManagedField
            fieldLocation="title"
            name="Headline"
            maxLength={fieldLengths.title}
            isRequired={true}
          >
            <TextInput />
          </ManagedField>
          <ManagedField
            fieldLocation="description"
            name="Standfirst"
            customValidation={this.props.descriptionValidator}
            maxCharLength={fieldLengths.description.charMax}
            maxLength={fieldLengths.description.max}
          >
            <ScribeEditorField
              allowedEdits={[
                'bold',
                'italic',
                'linkPrompt',
                'unlink',
                'insertUnorderedList'
              ]}
            />
          </ManagedField>
          <ManagedField
            fieldLocation="trailText"
            derivedFrom={this.props.video.description}
            name="Trail Text"
            maxCharLength={fieldLengths.description.charMax}
            maxLength={fieldLengths.description.max}
            isDesired={!this.props.canonicalVideoPageExists}
            isRequired={this.props.canonicalVideoPageExists}
          >
            <ScribeEditorField
              allowedEdits={['bold', 'italic']}
              isDesired={!this.props.canonicalVideoPageExists}
              isRequired={this.props.canonicalVideoPageExists}
            />
          </ManagedField>

          <ManagedField
            fieldLocation="byline"
            name="Byline"
            formRowClass="form__row__byline"
            tagType={TagTypes.contributor}
          >
            <TagPicker />
          </ManagedField>
          <ManagedField
            fieldLocation="commissioningDesks"
            name="Commissioning Desks"
            formRowClass="form__row__byline"
            tagType={TagTypes.tracking}
            isDesired={!this.props.canonicalVideoPageExists}
            isRequired={this.props.canonicalVideoPageExists}
            inputPlaceholder="Search commissioning info (type '*' to show all)"
          >
            <TagPicker disableTextInput />
          </ManagedField>

          <ManagedField
            fieldLocation="keywords"
            name="Keywords"
            formRowClass="form__row__byline"
            tagType={isCommercialType ? TagTypes.commercial : TagTypes.keyword}
            isDesired={true}
            inputPlaceholder="Search keywords (type '*' to show all)"
            customValidation={this.props.validateKeywords}
            updateSideEffects={this.props.composerKeywordsToYouTube}
          >
            <TagPicker disableTextInput />
          </ManagedField>
          <ManagedField fieldLocation="source" name="Video Source">
            <TextInput />
          </ManagedField>
          <ManagedField fieldLocation="expiryDate" name="Expiry Date">
            <DatePicker />
          </ManagedField>
          <ManagedField
            fieldLocation="category"
            name="Category"
            disabled={hasAssets}
          >
            <SelectBox selectValues={videoCategories} />
          </ManagedField>
          <ManagedField fieldLocation="duration" name="Video Duration (mm:ss)">
            <DurationInput />
          </ManagedField>
        </ManagedForm>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCategories from '../../actions/YoutubeActions/getCategories';
import * as getChannels from '../../actions/YoutubeActions/getChannels';

function mapStateToProps(state) {
  return {
    youtube: state.youtube,
    workflow: state.workflow
  };
}

function mapDispatchToProps(dispatch) {
  return {
    youtubeActions: bindActionCreators(
      Object.assign({}, getCategories, getChannels),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoData);
