import React, { useEffect, useState } from 'react';

import Header from './Header';
import { useDispatch, useSelector, useStore } from 'react-redux';
import { AppDispatch, RootState } from '../util/setupStore';
import { selectPublishedVideo, selectVideo } from '../slices/video';
import {
  updateMediaPlatformFilter,
  updateSearchTerm,
  updateShouldUseCreatedDateForSort
} from '../slices/search';
import { bindActionCreators } from 'redux';
import { reportPresenceClientError } from '../actions/PresenceActions/reportError';
import { publishVideo } from '../actions/VideoActions/publishVideo';
import { updateVideoPage } from '../actions/VideoActions/videoPageUpdate';
import { createVideoPage } from '../actions/VideoActions/videoPageCreate';
import { deleteVideo } from '../actions/VideoActions/deleteVideo';
import { updateVideo } from '../actions/VideoActions/updateVideo';
import { saveVideo } from '../actions/VideoActions/saveVideo';
import { getVideo } from '../actions/VideoActions/getVideo';
import { getPublishedVideo } from '../actions/VideoActions/getPublishedVideo';
import { getUploads } from '../slices/uploads';

export const ReactApp = (
  props: React.PropsWithChildren<{
    location: { query: unknown; pathname: unknown };
    params: { id: string };
  }>
) => {
  const [fetchedVideoFor, setFetchedVideoFor] = useState('');

  const dispatch = useDispatch<AppDispatch>();

  const store = useSelector(
    ({
      config,
      error,
      formFieldsWarning,
      search,
      saveState,
      s3Upload,
      usage,
      videoEditOpen
    }: RootState) => ({
      config,
      error,
      formFieldsWarning,
      search,
      saveState,
      s3Upload,
      usage,
      videoEditOpen
    })
  );

  const video = useSelector(selectVideo);
  const publishedVideo = useSelector(selectPublishedVideo);

  useEffect(() => {
    if (
      props.params.id &&
      (!video || props.params.id !== video.id) &&
      fetchedVideoFor !== props.params.id
    ) {
      dispatch(getVideo(props.params.id));
      dispatch(getPublishedVideo(props.params.id));
      dispatch(getUploads(props.params.id));
      setFetchedVideoFor(props.params.id);
    }
  }, [props.params.id, video.id]);

  useEffect(() => {
    document.body.scrollIntoView({ block: 'start', behavior: 'smooth' });
  }, [store.error.key]);

  useEffect(() => {
    const handleKeyUp = (event: KeyboardEvent) => {
      if (window.self !== window.top) {
        window.parent.postMessage({ eventKey: event.key }, '*');
      }
    };

    window.addEventListener('keyup', handleKeyUp);

    return () => {
      window.removeEventListener('keyup', handleKeyUp);
    };
  }, []);

  return (
    <div className="wrap">
      <Header
        updateMediaPlatformFilter={bindActionCreators(
          updateMediaPlatformFilter,
          dispatch
        )}
        shouldUseCreatedDateForSort={store.search.shouldUseCreatedDateForSort}
        updateShouldUseCreatedDateForSort={bindActionCreators(
          updateShouldUseCreatedDateForSort,
          dispatch
        )}
        reportPresenceClientError={bindActionCreators(
          reportPresenceClientError,
          dispatch
        )}
        updateSearchTerm={bindActionCreators(updateSearchTerm, dispatch)}
        search={store.search}
        currentPath={props.location.pathname}
        video={video || {}}
        publishedVideo={publishedVideo || {}}
        showPublishedState={props.params.id}
        s3Upload={store.s3Upload}
        publishVideo={bindActionCreators(publishVideo, dispatch)}
        saveState={store.saveState}
        updateVideoPage={bindActionCreators(updateVideoPage, dispatch)}
        createVideoPage={bindActionCreators(createVideoPage, dispatch)}
        videoEditOpen={store.videoEditOpen}
        usages={store.usage}
        presenceConfig={store.config.presence}
        isTrainingMode={store.config.isTrainingMode}
        formFieldsWarning={store.formFieldsWarning}
        deleteVideo={bindActionCreators(deleteVideo, dispatch)}
        updateVideo={bindActionCreators(updateVideo, dispatch)}
        saveVideo={bindActionCreators(saveVideo, dispatch)}
        query={props.location.query}
        error={store.error}
      />
      {store.error.message ? (
        <div
          key={store.error.key}
          className={`${store.error.isWarning ? 'error-bar--warning' : 'error-bar'} error-bar--animate `}
        >
          {store.error.message}
        </div>
      ) : null}
      <div>{props.children}</div>
    </div>
  );
};
