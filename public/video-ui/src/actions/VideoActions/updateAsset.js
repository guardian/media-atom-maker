//NOTE: THIS DOESN'T SAVE THE ASSET, ONLY UPDATES THE CLIENT STATE. USE saveAsset TO SAVE
export function updateAsset(asset) {
  return {
    type:       'ASSET_UPDATE_REQUEST',
    asset:      asset,
    receivedAt: Date.now()
  };
}
