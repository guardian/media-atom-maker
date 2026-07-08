import * as React from 'react';
import PropTypes from 'prop-types';
export class ErrorBoundary extends React.Component {
  static propTypes = {
    fallback: PropTypes.object.isRequired
  };
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }
  static getDerivedStateFromError() {
    return { hasError: true };
  }
  render() {
    if (this.state.hasError) {
      return this.props.fallback;
    }

    return this.props.children;
  }
}
