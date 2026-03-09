import com.gu.media.model.AssetType.{Subtitles, Video}
import com.gu.media.model.Platform.Url
import com.gu.media.util.AspectRatio

object BackfillDimensions extends App with BackfillBase {

  override def planActions(): List[UpdateAction] = {
    val dimensionsHelper = new DimensionsHelper(http)

    // only interested in "url" (self-hosted) atoms
    val atomIds = api.getAtomIdsForPlatform("url")

    // create action plan to update atoms
    atomIds.flatMap { atomId =>
        api.getMediaAtom(atomId).flatMap { atom =>

          // fix any assets that have issue
          val updatedAssets = atom.assets.map {

            case asset if asset.mimeType.contains("text/vtt") && asset.assetType != Subtitles =>
              println(s"wrong asset type for VTT $atomId: ${asset.assetType} -> Subtitles")
              asset.copy(assetType = Subtitles)

            case asset if asset.assetType == Video && asset.platform == Url && asset.dimensions.isEmpty =>
              val dims = dimensionsHelper.readDimensions(asset)
              val aspRatio = dims.flatMap(dim => AspectRatio.calculate(dim.width, dim.height)).map(_.name)
              println(s"missing dimensions $atomId: None, None -> $dims, $aspRatio")
              asset.copy(dimensions = dims, aspectRatio = aspRatio)

            case asset if asset.assetType == Video && asset.platform == Url && asset.aspectRatio.isEmpty =>
              val aspRatio = asset.dimensions.flatMap(dim => AspectRatio.calculate(dim.width, dim.height)).map(_.name)
              println(s"missing aspect ratio $atomId: None -> $aspRatio")
              asset.copy(aspectRatio = aspRatio)

            case asset => asset
          }

          if (updatedAssets != atom.assets)
            Some(UpdateAction(atom, atom.copy(assets = updatedAssets), shouldPublish(atom, atom.platform)))
          else
            None
        }
      } // order oldest to newest
      .sortBy(
        _.atom.contentChangeDetails.lastModified.map(_.date.getMillis).getOrElse(0L)
      )
  }

}

