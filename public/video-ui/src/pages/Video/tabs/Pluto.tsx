
import React, { useEffect, useState } from 'react';
import { Tab, TabPanel } from 'react-tabs';
import PlutoProjectLink from '../../../components/Pluto/PlutoProjectLink';
import { getPlutoItemById } from '../../../services/PlutoApi';

type PlutoTabProps = {
    disabled: any;
};

export class PlutoTab extends React.Component<PlutoTabProps> {
  static tabsRole = Tab.tabsRole;

  render() {
    return <Tab {...this.props}>Pluto</Tab>;
  }
}

type PlutoTabPanelProps = {
    video: any;
};

export class PlutoTabPanel extends React.Component<PlutoTabPanelProps> {
  static tabsRole = TabPanel.tabsRole;

  render() {
    // @ts-expect-error TS(2339): Property 'updateVideo' does not exist on type 'Rea... Remove this comment to see the full error message
    const { video, updateVideo, ...rest } = this.props;

    return (
      <TabPanel {...rest}>
        <div className="form__group">
          {video.plutoData && (
            <PlutoProjectLink projectId={video.plutoData.projectId} />
          )}

          <header className="video__detailbox__header">Commission</header>
          <ReadOnlyPlutoItem
            id={video.plutoData?.commissionId}
            itemType="commissions"
          />

          <header className="video__detailbox__header">Project</header>
          <ReadOnlyPlutoItem
            id={video.plutoData?.projectId}
            itemType="projects"
          />
        </div>
      </TabPanel>
    );
  }
}

/**
 *
 * @param {{id: string, itemType: import('../../../services/PlutoApi').PlutoItemType}} param0
 * @returns
 */
// @ts-expect-error TS(2694): Namespace '"/Users/david_furey/code/media-atom-mak... Remove this comment to see the full error message
function ReadOnlyPlutoItem({ id, itemType }: { id: string; itemType: import('../../../services/PlutoApi').PlutoItemType; }) {
  const [title, setTitle] = useState(id ? 'Loading...' : '');

  useEffect(() => {
    if (id) {
      getPlutoItemById(id, itemType)
        .then(data => setTitle(data.title))
        .catch(e => {
          const errorMessage = `Failed to lookup ${itemType} with ID '${id}'`;
          console.error(errorMessage, e);
          setTitle(errorMessage);
        });
    }
  }, [id, itemType]);

  return <p className="details-list__field">{title}</p>;
}
