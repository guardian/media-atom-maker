import React from 'react';

type Props = {
  capiError: string | null;
};

export const TagUnavailable = ({ capiError }: Props) => {
  if (capiError) {
    return <div className="form__field--external-error">{capiError}</div>;
  }

  return null;
};
