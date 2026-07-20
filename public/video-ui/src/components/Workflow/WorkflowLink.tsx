import React from 'react';
import Icon from '../Icon';
import WorkflowApi from '../../services/WorkflowApi';
import type { Video } from '../../services/VideosApi';

type Props = {
  video: Pick<Video, 'id'>;
};

export const WorkflowLink = ({ video }: Props) => (
  <a
    className="button inline-block"
    target="_blank"
    rel="noopener noreferrer"
    href={WorkflowApi.workflowItemLink(video)}
  >
    <Icon icon="open_in_new" className="icon__edit">
      Open in Workflow
    </Icon>
  </a>
);
