import React, { useEffect, useRef, useState } from 'react';
import ReactTooltip from 'react-tooltip';

type ActivateButtonProps = {
  className: string;
  onActivate: () => void;
  disabled?: boolean;
  confirmAssetActivation: boolean;
};

export const ActivateButton = ({
  className,
  onActivate,
  disabled,
  confirmAssetActivation
}: ActivateButtonProps) => {
  const [confirmActivate, setConfirmActivate] = useState(false);
  const resetTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const handleClick = () => {
    setConfirmActivate(true);

    if (resetTimerRef.current) {
      clearTimeout(resetTimerRef.current);
    }

    resetTimerRef.current = setTimeout(() => {
      setConfirmActivate(false);
      resetTimerRef.current = null;
    }, 4000);
  };

  useEffect(() => {
    return () => {
      if (resetTimerRef.current) {
        clearTimeout(resetTimerRef.current);
      }
    };
  }, []);

  if (confirmActivate) {
    return (
      <>
        <ReactTooltip />
        <button
          className={className}
          onClick={onActivate}
          data-tip="There is already an active asset. Continue activating?"
          data-testid="activate-button"
          disabled={disabled}
        >
          Confirm activate
        </button>
      </>
    );
  }

  return (
    <>
      <button
        className={className}
        onClick={confirmAssetActivation ? handleClick : onActivate}
        data-testid="activate-button"
        disabled={disabled}
      >
        Activate
      </button>
    </>
  );
};
