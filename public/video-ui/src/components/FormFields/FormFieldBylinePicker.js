import React from 'react';
import ContentApi from '../../services/capi';

export default class FormFieldBylinePicker extends React.Component {
  state = {
    bylineTags: null,
    inputString: '',
    lastAction: 'OTHER',
    tagValue: []
  };

  componentDidMount() {
    if (this.props.fieldValue !== this.props.placeholder) {
      this.tagsFromString(this.props.fieldValue).then(result => {
        this.setState({
          tagValue: result
        });
      });
    }
  }

  tagsFromString = savedTags => {
    return Promise.all(
      savedTags.split('|').map(element => {
        if (element.match('^profile/')) {
          return ContentApi.getLivePage(element).then(capiResponse => {
            const tag = capiResponse.response.tag;
            return {
              id: tag.id,
              webTitle: tag.webTitle
            };
          });
        } else {
          return new Promise(resolve => resolve(element));
        }
      })
    );
  };

  tagsToString = addedTags => {
    return addedTags
      .map(tag => {
        if (typeof tag === 'string') {
          return tag;
        } else return tag.id;
      })
      .join('|');
  };

  onUpdate = newValue => {
    this.setState({
      tagValue: newValue
    });
    this.props.onUpdateField(this.tagsToString(newValue));
  };

  updateInput = e => {
    if (this.state.lastAction === 'SPACE') {
      const newFieldValue = this.state.tagValue.concat([
        this.state.inputString
      ]);
      this.onUpdate(newFieldValue);
      this.setState({
        inputString: ''
      });

      // If the user did not add new text input, we update the tag search
    } else if (this.state.lastAction === 'DELETE') {
      const length = this.state.tagValue.length;
      const lastInput = this.state.tagValue[length - 1];

      this.setState({
        inputString: lastInput,
        lastAction: 'OTHER'
      });

      const newValue = this.state.tagValue.slice(
        0,
        this.state.tagValue.length - 1
      );
      this.onUpdate(newValue);
    } else {
      this.setState({
        inputString: e.target.value
      });

      const searchText = e.target.value;

      ContentApi.getBylineTags(searchText)
        .then(capiResponse => {
          const bylines = capiResponse.response.results.map(result => {
            const tags = { id: result.id, webTitle: result.webTitle };
            return tags;
          });
          this.setState({
            bylineTags: bylines
          });
          return;
        })
        .catch(() => {
          this.setState({
            bylineTags: null
          });
        });
    }
  };

  processTagInput = e => {
    if (e.keyCode === 32) {
      this.setState({
        lastAction: 'SPACE'
      });
    } else if (e.keyCode === 8) {
      if (this.state.inputString.length === 0) {
        const lastInput = this.state.tagValue[this.state.tagValue.length - 1];

        if (typeof lastInput === 'string') {
          //User is trying to delete a string input
          this.setState(
            {
              lastAction: 'DELETE'
            },
            () => {
              this.updateInput();
            }
          );
        }
      }
    } else {
      this.setState({
        lastAction: 'OTHER'
      });
    }
  };

  renderFieldValue(value, index) {
    if (value.webTitle) {
      return (
        <span key={`${value.id}-${index}`}>
          <span className="form__field__tag__display">{value.webTitle}</span>
          {' '}
        </span>
      );
    } else {
      return `${value} `;
    }
  }

  renderBylineTags(tag) {
    const addTag = () => {
      const newFieldValue = this.state.tagValue.concat([tag]);

      this.setState({
        inputString: ''
      });

      this.onUpdate(newFieldValue);
      this.setState({
        bylineTags: null
      });
    };

    return (
      <a
        className="form__field__tags"
        key={tag.id}
        title={tag.id}
        onClick={addTag}
      >
        {' '}{tag.webTitle}{' '}
      </a>
    );
  }

  renderValue = (field, i) => {
    const removeFn = () => {
      const newFieldValue = this.state.tagValue.filter(oldField => {
        return field.id !== oldField.id;
      });

      this.setState({
        inputString: ''
      });
      this.onUpdate(newFieldValue);
    };

    if (field.id) {
      return (
        <span
          className="form__field--multiselect__value"
          key={`${field.id}-${i}`}
          onClick={removeFn}
        >
          {field.webTitle}{' '}
        </span>
      );
    }
    return (
      <span
        className="form__field--multistring__value"
        key={`${field.id}-${i}`}
      >
        {' '}{field}{' '}
      </span>
    );
  };

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
          <p className="details-list__field ">
            {this.state.tagValue.map(this.renderFieldValue)}
          </p>
        </div>
      );
    }

    return (
      <div className="form__row">

        <div className="form__label__layout">
          <label className="form__label">{this.props.fieldName}</label>
        </div>

        <div className="form__field__byline">
          {this.state.tagValue.length
            ? this.state.tagValue.map((value, i) => this.renderValue(value, i))
            : ''}

          <input
            type="text"
            className="form__field__byline--input"
            id={this.props.fieldName}
            onKeyDown={this.processTagInput}
            onChange={this.updateInput}
            value={this.state.inputString}
          />
        </div>

        {this.state.bylineTags && this.state.bylineTags.length !== 0
          ? <div className="form__field__tags">
              {this.state.bylineTags.map(tag => this.renderBylineTags(tag))}
            </div>
          : ''}

        {this.state.tagValue.map(this.renderFieldValue)}

      </div>
    );
  }
}
