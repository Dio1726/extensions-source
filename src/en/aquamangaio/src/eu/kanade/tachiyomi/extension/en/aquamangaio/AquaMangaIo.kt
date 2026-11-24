package eu.kanade.tachiyomi.extension.en.aquamangaio

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import io.github.keiyoushi.annotations.Source
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

@Source
class AquaMangaIo : ParsedHttpSource() {

    override val supportsLatest = true

    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/popular?page=$page", headers)
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/latest?page=$page", headers)

    override fun popularMangaSelector(): String = "div.manga-item, div.series-card"
    override fun latestUpdatesSelector(): String = popularMangaSelector()

    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        title = element.selectFirst("h3, .title")!!.text()
        setUrlWithoutDomain(element.selectFirst("a")!!.attr("href"))
        thumbnail_url = element.selectFirst("img")?.absUrl("src")
    }

    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun popularMangaNextPageSelector(): String? = "a.next, a[rel=next]"
    override fun latestUpdatesNextPageSelector(): String? = popularMangaNextPageSelector()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        GET("$baseUrl/search?q=$query&page=$page", headers)

    override fun searchMangaSelector(): String = popularMangaSelector()
    override fun searchMangaFromElement(element: Element): SManga = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector(): String? = popularMangaNextPageSelector()

    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.selectFirst("h1")!!.text()
        description = document.selectFirst("div.description, p.summary")?.text()
        genre = document.select("div.genres a").joinToString { it.text() }
        thumbnail_url = document.selectFirst("div.cover img")?.absUrl("src")
    }

    override fun chapterListSelector(): String = "ul.chapter-list li a, div.chapters a"

    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        name = element.text()
        setUrlWithoutDomain(element.attr("href"))
    }

    override fun pageListParse(document: Document): List<Page> {
        return document.select("div.reader-area img, div.chapter-images img").mapIndexed { index, img ->
            Page(index, "", img.absUrl("src"))
        }
    }

    override fun imageUrlParse(document: Document): String = throw UnsupportedOperationException()
}
