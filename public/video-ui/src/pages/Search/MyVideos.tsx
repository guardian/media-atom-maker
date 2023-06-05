import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import VideoItem from '../../components/VideoItem';
import { frontPageSize } from '../../constants/frontPageSize';
import { getStore } from '../../util/storeAccessor';

declare global {
  interface Window { presenceClient?: any; }
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
  }
}

type VideoPresence = {
  mediaId: string,
  presences: Presence[]
}

type VideoActions = {
  getVideos: (searchTerm: string, limit: number) => null;
}

type Video = {
  id: string
}

type VideosProps = {
  videos: Video[], 
  total: number, 
  videoActions: VideoActions,
  searchTerm: string,
  limit: number
}

type PresenceData = {
  subscriptionId: string,
  current: string
}

type PresenceClient = {
  on: (string: string, f: (data?: PresenceData) => void) => void;
  subscribe: (mediaIds: string[]) => void;
}

const MoreLink = ({ onClick }: {onClick: () => void} ) => {
  return (<div>
    <button className="btn video__load_more" onClick={onClick}>
      Load More
    </button>
  </div>)
}

const Videos = ({videos, total, videoActions, searchTerm, limit}: VideosProps) => {
  const [mediaIds, setMediaIds] = useState<string[]>([]);
  const [videoPresences, setVideoPresences] = useState([]);
  const [client, setClient] = useState<PresenceClient>(null);

  const showMore = () => {
    videoActions.getVideos(
      searchTerm,
      limit + frontPageSize
    );
  };

  const startPresence = ({ domain, firstName, lastName, email }: PresenceConfig) => {
    const endpoint = `wss://${domain}/socket`;

    if (!window.presenceClient){
      console.error("Failed to connect to Presence as client was not available in window.")
      return;
    }

    const newClient = window.presenceClient(endpoint, {
      firstName,
      lastName,
      email
    })
    newClient.startConnection();
    setClient(newClient)
  };

  useEffect(() => {
    videoActions.getVideos(searchTerm, limit);
    const config = getStore().getState().config;
    const presenceConfig = config.presence;
    if (presenceConfig){
      startPresence(presenceConfig);
    }
    const newMediaIds = videos.map(video => `media-${video.id}`)
    setMediaIds(newMediaIds)
  }, [])

  useEffect(() => {
    if (client && mediaIds.length > 0) {
      client.on('connection.open', (data) => {
        console.log(data)
        client.subscribe(mediaIds);
      });
      client.on('visitor-list-updated', data => {
        // console.log(data)
        setVideoPresences([...videoPresences, {id: data.subscriptionId, current: data.current}])
      });
      client.on('visitor-list-subscribed', data => {
        // console.log(data)
        setVideoPresences([...videoPresences, {id: data.subscriptionId, current: data.current}])
      });
    } 
  }, [mediaIds, client]);


  return (
    <div>
      <div className="grid">
        {videos.length ? <ul className="grid__list">
          {videos.map(video => (
            <VideoItem key={video.id} video={video} />
          ))}
        </ul> : <p className="grid__message">No videos found</p>
        }
      </div>
      {videos.length === total ? <MoreLink onClick={showMore}></MoreLink> : null}
    </div>
  )
  
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideos from '../../actions/VideoActions/getVideos';

function mapStateToProps(state: {videos: {entries: number, total: number, limit: number}, searchTerm: string}) {
  return {
    videos: state.videos.entries,
    total: state.videos.total,
    limit: state.videos.limit,
    searchTerm: state.searchTerm
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
