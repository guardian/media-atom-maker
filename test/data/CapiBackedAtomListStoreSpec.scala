package data

import com.gu.media.CapiAccess
import com.gu.media.model.{ChangeRecord, ContentChangeDetails, Image, User}
import model.MediaAtomSummary
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsValue, Json}

class CapiBackedAtomListStoreSpec
    extends AnyFlatSpec
    with MockitoSugar
    with Matchers {

  "Pagination" should "enable fetching of multiple capi pages" in {
    Pagination.option(maxPageSize = 200, limit = None) shouldBe empty
    Pagination.option(maxPageSize = 200, limit = Some(1000)) should contain(
      Pagination(200, 5)
    )
    Pagination.option(maxPageSize = 200, limit = Some(1001)) should contain(
      Pagination(200, 6)
    )
    Pagination.option(maxPageSize = 200, limit = Some(50)) should contain(
      Pagination(50, 1)
    )
    Pagination.option(maxPageSize = 20, limit = Some(50)) should contain(
      Pagination(20, 3)
    )
  }

  "getCapiAtoms" should "convert capi response into MediaAtomSummary" in {
    val response: JsValue = Json.parse(capiTestJson)
    val mockCapi = mock[CapiAccess]
    when(mockCapi.capiQuery(any(), any(), any())).thenReturn(response)

    val store = new CapiBackedAtomListStore(mockCapi)

    val actual = store.getCapiAtoms(Map.empty)

    actual shouldEqual CapiAtoms(
      CapiPage(expectedTotal, 1, expectedMaxPages),
      expectedAtoms
    )
  }

  "getAtoms" should "load multiple pages from CAPI" in {
    val response: JsValue = Json.parse(capiTestJson)
    val mockCapi = mock[CapiAccess]
    val spyStore = spy(new CapiBackedAtomListStore(mockCapi))
    val page1Of200Atoms =
      (for (n <- 1 to 200) yield expectedAtoms.head.copy(id = s"atom$n")).toList
    val page2Of200Atoms = (for (n <- 201 to 400)
      yield expectedAtoms.head.copy(id = s"atom$n")).toList

    doReturn(
      CapiAtoms(CapiPage(expectedTotal, 1, expectedMaxPages), page1Of200Atoms)
    )
      .when(spyStore)
      .getCapiAtoms(Map("types" -> "media", "order-by" -> "newest"))
    val unlimited = spyStore.getAtoms(
      search = None,
      limit = None,
      shouldUseCreatedDateForSort = false,
      mediaPlatform = None,
      orderByOldest = false
    )
    unlimited.total shouldEqual 6081
    unlimited.atoms.size shouldEqual 200
    unlimited.atoms shouldEqual page1Of200Atoms

    doReturn(
      CapiAtoms(CapiPage(expectedTotal, 1, expectedMaxPages), page1Of200Atoms)
    )
      .when(spyStore)
      .getCapiAtoms(
        Map(
          "types" -> "media",
          "order-by" -> "newest",
          "page-size" -> "10",
          "page" -> "1"
        )
      )
    val limit10 = spyStore.getAtoms(
      search = None,
      limit = Some(10),
      shouldUseCreatedDateForSort = false,
      mediaPlatform = None,
      orderByOldest = false
    )
    limit10.total shouldEqual 6081
    limit10.atoms.size shouldEqual 10
    limit10.atoms shouldEqual page1Of200Atoms.take(10)

    doReturn(
      CapiAtoms(CapiPage(expectedTotal, 1, expectedMaxPages), page1Of200Atoms)
    )
      .when(spyStore)
      .getCapiAtoms(
        Map(
          "types" -> "media",
          "order-by" -> "newest",
          "page-size" -> "200",
          "page" -> "1"
        )
      )
    doReturn(
      CapiAtoms(CapiPage(expectedTotal, 2, expectedMaxPages), page2Of200Atoms)
    )
      .when(spyStore)
      .getCapiAtoms(
        Map(
          "types" -> "media",
          "order-by" -> "newest",
          "page-size" -> "200",
          "page" -> "2"
        )
      )
    val limit201 = spyStore.getAtoms(
      search = None,
      limit = Some(201),
      shouldUseCreatedDateForSort = false,
      mediaPlatform = None,
      orderByOldest = false
    )
    limit201.total shouldEqual 6081
    limit201.atoms.size shouldEqual 201
    limit201.atoms shouldEqual page1Of200Atoms ++ page2Of200Atoms.take(1)
  }

  val expectedTotal = 6081
  val expectedMaxPages = 609
  val expectedAtoms = List(
    MediaAtomSummary(
      id = "e75c870c-3183-4634-8fd1-ac8d4440e4b6",
      title = "Test 4:5",
      posterImage = None,
      contentChangeDetails = ContentChangeDetails(
        Some(
          ChangeRecord(
            DateTime.parse("2025-12-18T11:12:00.000"),
            Some(User("dev@guardian.co.uk", Some("Dev"), Some("Eng")))
          )
        ),
        Some(
          ChangeRecord(
            DateTime.parse("2025-12-15T13:45:28.000"),
            Some(User("dev@guardian.co.uk", Some("Dev"), Some("Eng")))
          )
        ),
        Some(
          ChangeRecord(
            DateTime.parse("2025-12-18T11:12:00.000"),
            Some(User("dev@guardian.co.uk", Some("Dev"), Some("Eng")))
          )
        ),
        6,
        None,
        None,
        None
      ),
      mediaPlatforms = Nil,
      currentMediaPlatform = None
    ),
    MediaAtomSummary(
      id = "0de596fb-bca3-48f9-846b-3f843508f772",
      title = "Test Atom",
      posterImage = None,
      contentChangeDetails = ContentChangeDetails(
        Some(
          ChangeRecord(
            DateTime.parse("2025-12-18T11:03:15.000"),
            Some(User("dev@guardian.co.uk", Some("Dev"), Some("Eng")))
          )
        ),
        None,
        Some(
          ChangeRecord(
            DateTime.parse("2025-12-18T11:03:15.000"),
            Some(User("dev@guardian.co.uk", Some("Dev"), Some("Eng")))
          )
        ),
        8,
        None,
        None,
        None
      ),
      mediaPlatforms = List("url"),
      currentMediaPlatform = Some("url")
    )
  )

  val capiTestJson =
    """{
      |  "response": {
      |    "status": "ok",
      |    "userTier": "internal",
      |    "total": 6081,
      |    "startIndex": 1,
      |    "pageSize": 10,
      |    "currentPage": 1,
      |    "pages": 609,
      |    "results": [
      |      {
      |        "id": "e75c870c-3183-4634-8fd1-ac8d4440e4b6",
      |        "atomType": "media",
      |        "labels": [],
      |        "defaultHtml": "\u003Cdiv /\u003E",
      |        "data": {
      |          "media": {
      |            "assets": [],
      |            "title": "Test 4:5",
      |            "category": "news",
      |            "duration": 0,
      |            "metadata": {
      |              "tags": [],
      |              "youtube": {
      |                "title": "Test 4:5"
      |              },
      |              "selfHost": {
      |                "videoPlayerFormat": "loop"
      |              }
      |            },
      |            "byline": [],
      |            "commissioningDesks": [],
      |            "keywords": [],
      |            "commentsEnabled": false,
      |            "platform": "url"
      |          }
      |        },
      |        "contentChangeDetails": {
      |          "lastModified": {
      |            "date": 1766056320000,
      |            "user": {
      |              "email": "dev@guardian.co.uk",
      |              "firstName": "Dev",
      |              "lastName": "Eng"
      |            }
      |          },
      |          "created": {
      |            "date": 1765806328000,
      |            "user": {
      |              "email": "dev@guardian.co.uk",
      |              "firstName": "Dev",
      |              "lastName": "Eng"
      |            }
      |          },
      |          "published": {
      |            "date": 1766056320000,
      |            "user": {
      |              "email": "dev@guardian.co.uk",
      |              "firstName": "Dev",
      |              "lastName": "Eng"
      |            }
      |          },
      |          "revision": 6
      |        },
      |        "flags": {
      |          "blockAds": false
      |        },
      |        "title": "Test 4:5",
      |        "commissioningDesks": []
      |      },
      |      {
      |        "id": "0de596fb-bca3-48f9-846b-3f843508f772",
      |        "atomType": "media",
      |        "labels": [],
      |        "defaultHtml": "\n\u003Cvideo controls=\"controls\" preload=\"metadata\" ()\u003E\n \u003Csource type=\"video/mp4\" src=\"https://randomsite.com/video.mp4\"/\u003E\n\u003C/video\u003E\n        ",
      |        "data": {
      |          "media": {
      |            "assets": [
      |              {
      |                "assetType": "video",
      |                "version": 1,
      |                "id": "https://randomsite.com/video.mp4",
      |                "platform": "url",
      |                "mimeType": "video/mp4"
      |              }
      |            ],
      |            "activeVersion": 1,
      |            "title": "Test Atom",
      |            "category": "explainer",
      |            "duration": 40,
      |            "metadata": {
      |              "tags": [],
      |              "youtube": {
      |                "title": "Test Atom"
      |              },
      |              "selfHost": {
      |                "videoPlayerFormat": "loop"
      |              }
      |            },
      |            "byline": [],
      |            "commissioningDesks": [],
      |            "keywords": [],
      |            "commentsEnabled": false,
      |            "platform": "url"
      |          }
      |        },
      |        "contentChangeDetails": {
      |          "lastModified": {
      |            "date": 1766055795000,
      |            "user": {
      |              "email": "dev@guardian.co.uk",
      |              "firstName": "Dev",
      |              "lastName": "Eng"
      |            }
      |          },
      |          "published": {
      |            "date": 1766055795000,
      |            "user": {
      |              "email": "dev@guardian.co.uk",
      |              "firstName": "Dev",
      |              "lastName": "Eng"
      |            }
      |          },
      |          "revision": 8
      |        },
      |        "flags": {
      |          "blockAds": false
      |        },
      |        "title": "Test Atom",
      |        "commissioningDesks": []
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin

}
