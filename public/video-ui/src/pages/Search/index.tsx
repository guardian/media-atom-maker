import React, { useEffect, useState } from 'react';
import VideoItem from '../../components/VideoItem';
import { frontPageSize } from '../../constants/frontPageSize';
import { Presence, PresenceClient, PresenceConfig, PresenceData, safelyStartPresence } from '../../services/presence';
import { getStore } from '../../util/storeAccessor';

type Video = {
  id: string
}

type VideosProps = {
  videos: Video[],
  total: number,
  videoActions: {
    getVideos: (searchTerm: string, limit: number, shouldUseCreatedDateForSort: boolean) => null;
  },
  presenceActions: {
    reportPresenceClientError: (err: unknown) => void
  },
  searchTerm: string,
  limit: number,
  shouldUseCreatedDateForSort: boolean
}


type VideoPresences = {
  mediaId: string,
  presences: Presence[]
}


const MoreLink = ({ onClick }: { onClick: () => void }) => {
  return (<div>
    <button className="btn video__load_more" onClick={onClick}>
      Load More
    </button>
  </div>);
};

const Videos = ({ videos, total, videoActions, searchTerm, limit, shouldUseCreatedDateForSort, presenceActions }: VideosProps) => {
  const [mediaIds, setMediaIds] = useState<string[]>([]);
  const [videoPresences, setVideoPresences] = useState<VideoPresences[]>([]);
  const [client, setClient] = useState<PresenceClient>(null);
  const [presencesQueue, setPresencesQueue] = useState<PresenceData>(null);
  const [prevSearch, setPrevSearch] = useState("");

  const showMore = () => {
    videoActions.getVideos(
      searchTerm,
      limit + frontPageSize,
      shouldUseCreatedDateForSort
    );
  };

  const startPresence = (presenceConfig: PresenceConfig) => {

    safelyStartPresence(
      (presenceClient) => {
        presenceClient.startConnection();
        presenceClient.on('visitor-list-subscribe', visitorData => {
          if (visitorData.data.subscribedTo) {
            const initialState = visitorData.data.subscribedTo
              .map(subscribedTo => { return { mediaId: subscribedTo.subscriptionId, presences: subscribedTo.currentState } as VideoPresences; })
              .filter(presence => presence.presences.length);
            setVideoPresences(initialState);
          }
        });
        presenceClient.on('visitor-list-updated', data => {
          if (data.subscriptionId && data.currentState) {
            // We dump the data to a queue (which is picked up by a useEffect rather than directly modifying videoPresences
            // so we don't need to depend on videoPresences, which led to some cyclicality
            setPresencesQueue(data);
          }
        });
        setClient(presenceClient);
      },
      presenceActions.reportPresenceClientError,
      presenceConfig
    );

  };

  const getPresencesForVideo = (videoId: string): Presence[] => {
    return videoPresences.filter(videoPresence => videoPresence.mediaId === `media-${videoId}`).flatMap(videoPresence => videoPresence.presences);
  };

  useEffect(() => {
    videoActions.getVideos(searchTerm, limit, shouldUseCreatedDateForSort);
    const config = getStore().getState().config;
    const presenceConfig = config.presence;
    if (presenceConfig) {
      startPresence(presenceConfig);
    }
  }, []);

  useEffect(() => {
    const newMediaIds = videos.map(video => `media-${video.id}`);
    setMediaIds(newMediaIds);
  }, [videos]);

  useEffect(() => {
    if (client && mediaIds.length > 0) {
      client.subscribe(mediaIds);

      client.on('connection.open', () => {
        client.subscribe(mediaIds);
      });
    }
  }, [mediaIds, client]);

  useEffect(() => {
    if (presencesQueue) {
      setVideoPresences([...videoPresences.filter(videoPresence => videoPresence.mediaId !== presencesQueue.subscriptionId), { mediaId: presencesQueue.subscriptionId, presences: presencesQueue.currentState }]);
      setPresencesQueue(null);
    }
  }, [presencesQueue, videoPresences]);

  useEffect(() => {
    if (searchTerm !== prevSearch) {
      setPrevSearch(searchTerm);
      videoActions.getVideos(searchTerm, limit, shouldUseCreatedDateForSort);
    }
  }, [searchTerm, prevSearch]);

  useEffect(() => {
    videoActions.getVideos(searchTerm, limit, shouldUseCreatedDateForSort);
  }, [shouldUseCreatedDateForSort]);

  return (
    <div>
      <div className="grid">
        {videos.length ? <ul className="grid__list">
          {videos.map(video => (
            <VideoItem key={video.id} video={video} presences={getPresencesForVideo(video.id)} />
          ))}
        </ul> : <p className="grid__message">No videos found</p>
        }
      </div>
      {videos.length === total ? null : <MoreLink onClick={showMore}></MoreLink>}
    </div>
  );

};

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as reportPresenceClientError from '../../actions/PresenceActions/reportError';
import * as getVideos from '../../actions/VideoActions/getVideos';

function mapStateToProps(state: { videos: { entries: number, total: number, limit: number }, searchTerm: string, shouldUseCreatedDateForSort: boolean }) {
  return {
    videos: state.videos.entries,
    total: state.videos.total,
    limit: state.videos.limit,
    searchTerm: state.searchTerm,
    shouldUseCreatedDateForSort: state.shouldUseCreatedDateForSort
  };
}

// @ts-ignore
function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideos), dispatch),
    presenceActions: bindActionCreators(Object.assign({}, reportPresenceClientError), dispatch)
  };
}

// @ts-ignore
export default connect(mapStateToProps, mapDispatchToProps)(React.memo(Videos));
