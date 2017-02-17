package util

import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import play.filters.gzip.GzipFilter

class Filters @Inject() (gzip: GzipFilter, redirectFilter: RedirectFilter) extends DefaultHttpFilters(gzip, redirectFilter)
