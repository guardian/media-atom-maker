import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { Tab, TabPanel } from 'react-tabs';
import { getIconikItemById, IconikItemType } from '../../../services/IconikApi';
import { Video } from '../../../services/VideosApi';
import { RootState } from '../../../util/setupStore';

export const IconikTab = ({ disabled, ...rest }: { disabled: boolean }) => {
  return (
    <Tab disabled={disabled} {...rest}>
      Iconik
    </Tab>
  );
};

IconikTab.tabsRole = Tab.tabsRole;

export const IconikTabPanel = ({ video, ...rest }: { video: Video }) => {
  const { config } = useSelector(({ config }: RootState) => ({
    config
  }));

  if (!config.showIconik) {
    return (
      <TabPanel {...rest}>
        <div className="form__group">
          <p>Iconik integration is not currently enabled.</p>
        </div>
      </TabPanel>
    );
  }

  return (
    <TabPanel {...rest}>
      <div className="form__group">
        {video.iconikData && (
          <div>
            <header className="video__detailbox__header">Iconik</header>
            <div className="form__section">
              <ReadOnlyIconikItem
                label={'Working Group'}
                id={video.iconikData?.workingGroupId}
                itemType="working-groups"
              />
              <ReadOnlyIconikItem
                label={'Commission'}
                id={video.iconikData?.commissionId}
                itemType="commissions"
              />
              <ReadOnlyIconikItem
                label={'Project'}
                id={video.iconikData?.projectId}
                itemType="projects"
              />
            </div>
          </div>
        )}
        {!video.iconikData && (
          <p>
            No Iconik data associated with this video yet. Associations can be
            added in the &quot;upload&quot; view.
          </p>
        )}
      </div>
    </TabPanel>
  );
};

IconikTabPanel.tabsRole = TabPanel.tabsRole;

const ReadOnlyIconikItem = ({
  label,
  itemType,
  id
}: {
  label: string;
  itemType: IconikItemType;
  id?: string;
}) => {
  const [title, setTitle] = useState(id ? 'Loading...' : 'Not set');

  useEffect(() => {
    if (id) {
      getIconikItemById(id, itemType)
        .then(data => setTitle(data.title))
        .catch(e => {
          const errorMessage = `Failed to lookup Iconik ${itemType} with ID '${id}'`;
          console.error(errorMessage, e);
          setTitle(errorMessage);
        });
    }
  }, [id, itemType]);

  return (
    <div>
      <p className="details-list__title">{label}</p>
      <p className="details-list__field">{title}</p>
    </div>
  );
};
