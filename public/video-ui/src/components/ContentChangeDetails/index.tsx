import React from 'react';
import { ManagedField, ManagedForm, ManagedSection } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import DatePicker from '../FormFields/DatePicker';
import { isVideoPublished } from '../../util/isVideoPublished';
import { Video } from '../../services/VideosApi';

type Props = {
  video: Video;
};

class ContentChangeDetails extends React.Component<Props> {
  getTextField = (path: string, caption: string) => (
    // @ts-expect-error TS(2769): No overload matches this call.
    <ManagedField fieldLocation={path} name={caption}>
      {/* @ts-expect-error TS(2769): No overload matches this call. */}
      <TextInput />
    </ManagedField>
  );

  getDateField = (path: string, caption: string) => (
    // @ts-expect-error TS(2769): No overload matches this call.
    <ManagedField fieldLocation={path} name={caption} className="unhide">
      {/* @ts-expect-error TS(2740): Type '{}' is missing the following properties from... Remove this comment to see the full error message */}
      <DatePicker />
    </ManagedField>
  );

  render() {
    const { video } = this.props;

    const isPublished = isVideoPublished(video);

    return (
      // @ts-expect-error TS(2769): No overload matches this call.
      <ManagedForm data={video}>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedSection>
          {this.getDateField('contentChangeDetails.created.date', 'Created at')}
          {this.getDateField(
            'contentChangeDetails.lastModified.date',
            'Last modified at'
          )}
          {isPublished &&
            this.getDateField(
              'contentChangeDetails.published.date',
              'Last published at'
            )}
        </ManagedSection>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedSection>
          {this.getTextField(
            'contentChangeDetails.created.user.email',
            'Created by'
          )}
          {this.getTextField(
            'contentChangeDetails.lastModified.user.email',
            'Last modified by'
          )}
          {isPublished &&
            this.getTextField(
              'contentChangeDetails.published.user.email',
              'Last published by'
            )}
        </ManagedSection>
      </ManagedForm>
    );
  }
}

export default ContentChangeDetails;
