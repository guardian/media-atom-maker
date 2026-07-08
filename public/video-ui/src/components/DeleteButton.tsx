import React, { useEffect, useRef, useState } from 'react';
import ReactTooltip from 'react-tooltip';
import Icon from './Icon';

type DeleteButtonProps = {
  tooltip: string;
  onDelete: () => void;
  disabled?: boolean;
  tooltipWhenDisabled?: string;
};

export const DeleteButton = ({
  tooltip,
  onDelete,
  disabled,
  tooltipWhenDisabled
}: DeleteButtonProps) => {
  const [confirmDelete, setConfirmDelete] = useState(false);
  const resetTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const handleClick = () => {
    setConfirmDelete(true);

    if (resetTimerRef.current) {
      clearTimeout(resetTimerRef.current);
    }

    resetTimerRef.current = setTimeout(() => {
      setConfirmDelete(false);
      resetTimerRef.current = null;
    }, 2000);
  };

  useEffect(() => {
    return () => {
      if (resetTimerRef.current) {
        clearTimeout(resetTimerRef.current);
      }
    };
  }, []);

  if (confirmDelete) {
    return (
      <>
        <ReactTooltip />
        <button
          className="btn button__secondary--remove-confirm"
          onClick={onDelete}
          data-tip="Confirm delete. This cannot be undone."
          data-testid="delete-button"
          disabled={disabled}
        >
          <Icon icon="delete_forever">Confirm delete</Icon>
        </button>
      </>
    );
  }

  return (
    <>
      <ReactTooltip />
      <button
        className="btn button__secondary--remove"
        onClick={handleClick}
        data-tip={disabled ? (tooltipWhenDisabled ?? tooltip) : tooltip}
        data-testid="delete-button"
        disabled={disabled}
      >
        <Icon icon="delete">Delete</Icon>
      </button>
    </>
  );
};
