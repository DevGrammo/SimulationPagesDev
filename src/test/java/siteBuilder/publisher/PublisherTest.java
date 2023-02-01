package siteBuilder.publisher;

import devgrammo.siteBuilder.config.SiteData;
import devgrammo.siteBuilder.config.SiteException;
import devgrammo.siteBuilder.content.property.PropertyIndex;
import devgrammo.siteBuilder.content.ContentReader;
import devgrammo.siteBuilder.content.Content;
import devgrammo.siteBuilder.content.ContentsByPage;
import devgrammo.siteBuilder.publisher.Publisher;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PublisherTest {
    final String testPostPath = "TestFiles/ContentsTest/post/example_post";
    final String testPostsPath = "TestFiles/ContentsTest/post";
    final String sitePath0 = "TestFiles/site.json";
    @Test
    public void shouldReturnPublisher() throws SiteException {
        Publisher pub = new Publisher(new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0)));
        assertNotNull(pub);
    }
    @Test
    public void shouldProduceHtmlDocument() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        Publisher pub = new Publisher(r);
        Content c = r.makeContent(new File(testPostPath));
        Document html = pub.makeDocument(c);
        assertFalse(html.toString().isEmpty());
        Elements metaTags =  html.getElementsByTag("meta");
        assertFalse(metaTags.isEmpty());
    }

    @Test
    public void shouldProduceAPIs() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        Publisher pub = new Publisher(r);
        Content c = r.makeContent(new File(testPostPath));
        String api = pub.makeJSONApi.apply(c);
        String itemApi = pub.makeJSONApiAsItem.apply(c);
        assertFalse(api.isEmpty());
        assertFalse(itemApi.isEmpty());
    }

    @Test
    public void shouldPublishPosts() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        List<Content> posts = r.getContents.apply(testPostsPath).getContents();
        Publisher pub = new Publisher(r);
        System.out.println(posts.toString());
        pub.publishContentList(posts);
    }

    @Test
    public void shouldPublishPropertyTree() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        ContentsByPage posts = r.getContents.apply(testPostPath);
        Publisher pub = new Publisher(r);
        pub.publishPropertyTree(PropertyIndex.makePropertyIndex("tag", posts));
    }

    @Test
    public void shouldProduceProfileApi() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        Publisher pub = new Publisher(r);
        String api = pub.makeJSONProfileAPI(r.getSite());
        assertFalse(api.isEmpty());
    }

    @Test
    public void shouldProduceHomeDocument() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        Publisher pub = new Publisher(r);
        Document doc = pub.makeHomeDocument(r.getSite());
        assertFalse(doc.toString().isEmpty());
    }

    @Test
    public void shouldPublishHome() throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        Publisher pub = new Publisher(r);
        pub.publishHomePage(r.getSite());
    }

    @Test
    public void shouldMakeUrlList () throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        Publisher pub = new Publisher(r);
        ContentsByPage posts = r.getContents.apply(testPostPath);

        List<String> result = pub.makeContentPageUrlList.apply(posts.getContents());
        System.out.println(result);
    }

    @Test
    public void shouldPublishSiteMap () throws SiteException {
        ContentReader r = new ContentReader(SiteData.getInstance("http://localhost:3000", sitePath0));
        ContentsByPage posts = r.getContents.apply(testPostPath);
        Publisher pub = new Publisher(r);
        pub.publishSiteMap(List.of(posts.getContents()));
    }
}
