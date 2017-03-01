package com.gu.media

import java.time.Instant

trait CapiResponses {
  def capiResult(atoms: List[String], page: Int = 1, pages: Int = 1) =
    s"""
       | {
       |   "response": {
       |     "currentPage": $page,
       |     "pages": $pages,
       |     "results": [${atoms.mkString(", ")}]
       |   }
       | }
     """.stripMargin

  def expiredAtom(assets: String*): String =
    s"""
       | {
       |   "data": {
       |     "media": {
       |       "metadata": {
       |         "expiryDate": ${Instant.now().toEpochMilli - 300000}
       |       },
       |       "assets": [${assets.mkString(", ")}]
       |     }
       |   }
       | }
     """.stripMargin

  def liveAtom(assets: String*): String =
    s"""
       | {
       |   "data": {
       |     "media": {
       |       "metadata": {
       |         "expiryDate": ${Instant.now().toEpochMilli + 300000}
       |       },
       |       "assets": [${assets.mkString(", ")}]
       |     }
       |   }
       | }
     """.stripMargin

  def youTubeAsset(id: String): String =
  s"""
     | {
     |   "platform": "youtube",
     |   "id": "$id"
     | }
     """.stripMargin

  def nonYouTubeAsset(id: String): String =
    s"""
       | {
       |   "platform": "totally-real-video-hosting.com",
       |   "id": "$id"
       | }
     """.stripMargin
}
