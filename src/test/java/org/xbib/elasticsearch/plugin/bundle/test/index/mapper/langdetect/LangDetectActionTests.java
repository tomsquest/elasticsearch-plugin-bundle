package org.xbib.elasticsearch.plugin.bundle.test.index.mapper.langdetect;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.xbib.elasticsearch.plugin.bundle.action.langdetect.LangdetectRequestBuilder;
import org.xbib.elasticsearch.plugin.bundle.action.langdetect.LangdetectResponse;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.util.Collection;
import java.util.Collections;

/**
 * Language detecttion action test.
 */
public class LangDetectActionTests extends ESSingleNodeTestCase {

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(BundlePlugin.class);
    }

    public void testLangDetectProfile() throws Exception {
        //startCluster();
        //try {
        // normal profile
        LangdetectRequestBuilder langdetectRequestBuilder =
                new LangdetectRequestBuilder(client())
                        .setText("hello this is a test");
        LangdetectResponse response = langdetectRequestBuilder.execute().actionGet();
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertNull(response.getProfile());

        // short-text profile
        LangdetectRequestBuilder langdetectProfileRequestBuilder =
                new LangdetectRequestBuilder(client())
                        .setText("hello this is a test")
                        .setProfile("shorttext");
        response = langdetectProfileRequestBuilder.execute().actionGet();
        assertNotNull(response);
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertEquals("shorttext", response.getProfile());

        // again normal profile
        langdetectRequestBuilder = new LangdetectRequestBuilder(client())
                .setText("hello this is a test");
        response = langdetectRequestBuilder.execute().actionGet();
        assertNotNull(response);
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertNull(response.getProfile());
        //} finally {
        //    stopCluster();
        //}
    }

    public void testSort() throws Exception {
        //startCluster();
        //try {

        Settings settings = Settings.builder()
                .build();

        client().admin().indices().prepareCreate("test")
                .setSettings(settings)
                .addMapping("article",
                        XContentFactory.jsonBuilder().startObject()
                                .startObject("article")
                                .startObject("properties")
                                .startObject("content")
                                .field("type", "langdetect")
                                .array("languages", "de", "en", "fr")
                                .endObject()
                                .endObject()
                                .endObject()
                                .endObject())
                .execute().actionGet();

        client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

        client().prepareIndex("test", "article", "1")
                .setSource(XContentFactory.jsonBuilder().startObject()
                        .field("title", "Some title")
                        .field("content", "Oh, say can you see by the dawn`s early light, " +
                                "What so proudly we hailed at the twilight`s last gleaming?")
                        .endObject()).execute().actionGet();
        client().prepareIndex("test", "article", "2")
                .setSource(XContentFactory.jsonBuilder().startObject()
                        .field("title", "Ein Titel")
                        .field("content", "Einigkeit und Recht und Freiheit für das deutsche Vaterland!")
                        .endObject()).execute().actionGet();
        client().prepareIndex("test", "article", "3")
                .setSource(XContentFactory.jsonBuilder().startObject()
                        .field("title", "Un titre")
                        .field("content", "Allons enfants de la Patrie, Le jour de gloire est arrivé!")
                        .endObject()).execute().actionGet();

        client().admin().indices().prepareRefresh().execute().actionGet();

        SearchResponse searchResponse = client().prepareSearch()
                .setQuery(QueryBuilders.termQuery("content", "en"))
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().getTotalHits());
        assertEquals("Oh, say can you see by the dawn`s early light, What so proudly we hailed at the twilight`s last gleaming?",
                searchResponse.getHits().getAt(0).getSourceAsMap().get("content").toString());

        searchResponse = client().prepareSearch()
                .setQuery(QueryBuilders.termQuery("content", "de"))
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().getTotalHits());
        assertEquals("Einigkeit und Recht und Freiheit für das deutsche Vaterland!",
                searchResponse.getHits().getAt(0).getSourceAsMap().get("content").toString());

        searchResponse = client().prepareSearch()
                .setQuery(QueryBuilders.termQuery("content", "fr"))
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().getTotalHits());
        assertEquals("Allons enfants de la Patrie, Le jour de gloire est arrivé!",
                searchResponse.getHits().getAt(0).getSourceAsMap().get("content").toString());
        //} finally {
        //    stopCluster();
        //}
    }
}
