import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import VideoItem from '../../components/VideoItem';
import { frontPageSize } from '../../constants/frontPageSize';
import { getStore } from '../../util/storeAccessor';

declare global {
  interface Window { presenceClient?: any; }
}

type Video = {
  id: string
}

type VideosProps = {
  videos: Video[],
  total: number,
  videoActions: {
    getVideos: (searchTerm: string, limit: number, shouldUseCreatedDateForSort: boolean) => null;
  },
  searchTerm: string,
  limit: number,
  shouldUseCreatedDateForSort: boolean
}

type PresenceConfig = {
  domain: string,
  email: string,
  firstName: string,
  lastName: string
}

type Presence = {
  clientId: {
    connId: string,
    person: {
      browserId: string,
      email: string,
      firstName: string,
      googleId: string,
      lastName: string
    },
    lastAction: string, // ISO 8601 timestamp
    location: string // Always seems to be "document"
  },
}

type VideoPresences = {
  mediaId: string,
  presences: Presence[]
}

type PresenceData = {
  subscriptionId?: string,
  currentState?: Presence[],
  subscribedTo: PresenceData[],
  data?: PresenceData
}

type PresenceClient = {
  on: (string: string, f: (data?: PresenceData ) => void) => void;
  subscribe: (mediaIds: string[]) => void;
  startConnection: () => void;
}

const MoreLink = ({ onClick }: {onClick: () => void} ) => {
  return (<div>
    <button className="btn video__load_more" onClick={onClick}>
      Load More
    </button>
  </div>)
}

const Videos = ({videos, total, videoActions, searchTerm, limit, shouldUseCreatedDateForSort}: VideosProps) => {
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

  const startPresence = ({ domain, firstName, lastName, email }: PresenceConfig) => {
    const endpoint = `wss://${domain}/socket`;
    if (!window.presenceClient){
      console.error("Failed to connect to Presence as client was not available in window.")
      return;
    }

    const presenceClient = window.presenceClient(endpoint, {
      firstName,
      lastName,
      email
    }) as PresenceClient
    presenceClient.startConnection();
    presenceClient.on('visitor-list-subscribe', visitorData => {
      if (visitorData.data.subscribedTo){
        const initialState = visitorData.data.subscribedTo
          .map(subscribedTo => {return {mediaId: subscribedTo.subscriptionId, presences: subscribedTo.currentState} as VideoPresences})
          .filter(presence => presence.presences.length)
        setVideoPresences(initialState)
      }
    })
    presenceClient.on('visitor-list-updated', data => {
      if (data.subscriptionId && data.currentState){
        // We dump the data to a queue (which is picked up by a useEffect rather than directly modifying videoPresences
        // so we don't need to depend on videoPresences, which led to some cyclicality
        setPresencesQueue(data);
      }
    });
    setClient(presenceClient);
  };

  const getPresencesForVideo = (videoId: string): Presence[] => {
    return videoPresences.filter(videoPresence => videoPresence.mediaId === `media-${videoId}`).flatMap(videoPresence => videoPresence.presences);
  }

  useEffect(() => {
    videoActions.getVideos(searchTerm, limit, shouldUseCreatedDateForSort);
    const config = getStore().getState().config;
    const presenceConfig = config.presence;
    if (presenceConfig){
      startPresence(presenceConfig);
    }
  }, [])

  useEffect(() => {
    const newMediaIds = videos.map(video => `media-${video.id}`)
    setMediaIds(newMediaIds)
  }, [videos])

  useEffect(() => {
    if (client && mediaIds.length > 0) {
      client.subscribe(mediaIds);

      client.on('connection.open', () => {
        client.subscribe(mediaIds);
      });
    }
  }, [mediaIds, client]);

  useEffect(() => {
    if (presencesQueue){
      setVideoPresences([...videoPresences.filter(videoPresence => videoPresence.mediaId !== presencesQueue.subscriptionId), {mediaId: presencesQueue.subscriptionId, presences: presencesQueue.currentState}])
      setPresencesQueue(null);
    }
  }, [presencesQueue, videoPresences])

  useEffect(() => {
    if (searchTerm !== prevSearch) {
      setPrevSearch(searchTerm)
      videoActions.getVideos(searchTerm, limit, shouldUseCreatedDateForSort);
    }
  }, [searchTerm, prevSearch])

  useEffect(() => {
    videoActions.getVideos(searchTerm, limit, shouldUseCreatedDateForSort);
  }, [shouldUseCreatedDateForSort])

  return (
    <div>
      <div className="grid">
        {videos.length ? <ul className="grid__list">
          {videos.map(video => (
            <VideoItem key={video.id} video={video} presences={getPresencesForVideo(video.id)}/>
          ))}
        </ul> : <p className="grid__message">No videos found</p>
        }
      </div>
      {videos.length === total ? null : <MoreLink onClick={showMore}></MoreLink>}
    </div>
  )

}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideos from '../../actions/VideoActions/getVideos';

function mapStateToProps(state: {videos: {entries: number, total: number, limit: number}, searchTerm: string, shouldUseCreatedDateForSort: boolean}) {
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
    videoActions: bindActionCreators(Object.assign({}, getVideos), dispatch)
  };
}

// @ts-ignore
export default connect(mapStateToProps, mapDispatchToProps)(React.memo(Videos));
