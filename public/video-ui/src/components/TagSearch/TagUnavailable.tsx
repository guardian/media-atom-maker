import React from 'react';

type Props = {
  capiUnavailable: boolean;
};

export default class TagUnavailable extends React.Component<Props> {
  render() {
    if (this.props.capiUnavailable) {
      return (
        <div className="form__field--external-error">
          Tags are currently unavailable
        </div>
      );
    }

    return null;
  }
}