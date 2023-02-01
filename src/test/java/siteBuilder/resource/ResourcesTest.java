package siteBuilder.resource;

import devgrammo.siteBuilder.config.SiteData;
import devgrammo.siteBuilder.config.SiteException;
import devgrammo.siteBuilder.content.Content;
import devgrammo.siteBuilder.content.ContentReader;
import devgrammo.siteBuilder.content.ContentsByPage;
import devgrammo.siteBuilder.content.property.PropertyIndex;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResourcesTest {
    final String siteName = "site-name-test";
    final String testArticlesPath = "TestFiles/ContentsTest/article";
    final String testMissingArticlesPath = "TestFiles/ContentsTest/FAIL";
    final String testMissingArticlesContentsPath = "TestFiles/ContentsTest/empty_articles";
    final String testPostsPath = "TestFiles/ContentsTest/post";
    final String sitePath0 = "TestFiles/site.json";
    final String sitePathNoArticles = "TestFiles/siteNoArticles.json";

    @Test
    public void shouldCreateResource() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        assertEquals(siteName, r.getSite().getSiteName());
    }

    @Test
    public void shouldThrowSiteException() {

        SiteException thrown = Assertions.assertThrows(SiteException.class, () -> {
            SiteData site = SiteData.getInstance("http://localhost:3000", "failing path");
            ContentReader r = new ContentReader(site);
            System.out.println(r);
        });
        Assertions.assertTrue(thrown.getMessage().startsWith("Site exception"));
    }

    @Test
    public void shouldReturnContent() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        Content c = r.makeContent(new File( "TestFiles/PostTest/test_post"));
        assertEquals("test_post", c.getSlug());
        assertEquals("PostTest", c.getPageType());
        assertEquals(List.of("A", "B", "C"), c.getPropertySlugs("tag"));
        assertEquals(List.of(), c.getPropertySlugs("category"));
    }

    @Test
    public void shouldReturnContents() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        // happy path
        ContentsByPage articles = r.getContents.apply(testArticlesPath);
        ContentsByPage posts = r.getContents.apply(testPostsPath);
        // sad path
        // if directory does not exist or contents do not exist return empty list
        ContentsByPage missingDirectory = r.getContents.apply(testMissingArticlesPath);
        ContentsByPage missingContents = r.getContents.apply(testMissingArticlesContentsPath);

        assertTrue(posts.getContents().size() > 0);
        assertTrue(articles.getContents().size() > 0);
        assertEquals(0, missingDirectory.getContents().size());
        assertEquals(0, missingContents.getContents().size());
    }

    @Test
    public void shouldReturnTemplates() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        Map<String, Document> templates = r.getTemplates();
        assertFalse(templates.isEmpty());
        assertTrue(templates.containsKey("default"));
    }

    @Test
    public void shouldReturnIndexes() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        PropertyIndex postTagIndex = PropertyIndex.makePropertyIndex("tag", r.getContents.apply(testPostsPath));
        PropertyIndex articleTagIndex = PropertyIndex.makePropertyIndex("tag", r.getContents.apply(testArticlesPath));
        assertNotNull(postTagIndex);
        assertNotNull(articleTagIndex);
        assertEquals(List.of("A", "B", "C"), new ArrayList<>(postTagIndex.getIndex().keySet()));
        assertEquals(List.of("D"), new ArrayList<>(articleTagIndex.getIndex().keySet()));
    }

    @Test
    public void shouldReturnEmptyIndex() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePathNoArticles));
        ContentsByPage articles = r.getContents.apply(sitePathNoArticles + "/article");

        assertTrue(articles.getContents().isEmpty());
        assertTrue(PropertyIndex.makePropertyIndex("tag", articles).getIndex().isEmpty());
    }

}