package devgrammo.siteBuilder.publisher;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import devgrammo.siteBuilder.config.SiteData;
import devgrammo.siteBuilder.content.Content;
import devgrammo.siteBuilder.content.property.PropertyIndex;
import devgrammo.siteBuilder.config.Locator;
import devgrammo.siteBuilder.content.ContentReader;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Publisher {
    private static final
    ObjectMapper objectMapper = new ObjectMapper()
            .configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false)
            .configure(
                    DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
                    false);
    private final Locator locator;
    private final Map<String, Document> templates;

    private final ContentReader reader;

    public Publisher(ContentReader r) {
        this.locator = r.getLocator();
        this.templates = r.getTemplates();
        this.reader = r;
    }

    public ContentReader getReader() {
        return this.reader;
    }

    void toRoot(BuildOutput output) {
        try {
            Files.createDirectories(Paths.get(output.getPath()).getParent());
            BufferedWriter writer = new BufferedWriter(new FileWriter(output.getPath()));
            writer.write(output.getContent());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Document getContentTemplate(Content c) {
        String templateKey = c.getContentData().getTemplate();
        if (null == templateKey) templateKey = c.getPageType();
        return templates.get(templateKey);
    }

    public void publishHomePage(SiteData site) {

        BuildOutput api = new BuildOutput(site.getRoot() + "/API/profile.json", makeJSONProfileAPI(site));
        BuildOutput html = new BuildOutput(site.getRoot() + "/index.html", makeHomeDocument(site).toString());
        toRoot(api);
        toRoot(html);
    }

    public void publishSiteMap(List<List<Content>> contents) {
        List<String> urls = contents.stream()
                .map(makeContentPageUrlList)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        urls.add(locator.getBase());
        StringJoiner joiner = new StringJoiner("\n");
        for (String url : urls) {
            joiner.add(url);
        }
        BuildOutput output = new BuildOutput(locator.getRoot() + "/sitemap.txt", joiner.toString());
        toRoot(output);
    }

    public void publishContentList(List<Content> list) {
        Function<Content, Content> publishContent = c -> {
            toRoot(buildHtml(c));
            toRoot(buildApi(c));
            return c;
        };
        Function<Content, String> publishToItemApi =
                publishContent.andThen(makeJSONApiAsItem);

        BuildOutput output = (new BuildOutput(
                locator.getListFileApi(list.get(0).getPageType()),
                list.stream()
                        .parallel()
                        .map(publishToItemApi)
                        .collect(Collectors.toList())
                        .toString()
        ));
        toRoot(output);
    }

    public void publishPropertyTree(PropertyIndex propIndex) {
        Consumer<Map.Entry<String, List<Content>>> publishToPropertyItem =
                e -> toRoot(buildPropertyItems(
                        propIndex.getProperty(),
                        propIndex.getPageType(),
                        e.getKey(),
                        e.getValue()));

        propIndex.getIndex()
                .entrySet()
                .stream()
                .parallel()
                .forEach(publishToPropertyItem);

        toRoot(buildProperty(propIndex.getProperty(), propIndex.getPageType(), propIndex.getIndex()));
    }

    public BuildOutput buildProperty(String property, String pageType, Map<String, List<Content>> index) {
        return new BuildOutput(
                locator.getPropertyApiFile(property, pageType),
                index.keySet()
                        .stream()
                        .map(slug ->
                                "{\"tag\":" + "\"" + slug + "\"" + "," +
                                        "\"api\":" + "\"" + locator.getPropertySlugApiUrl(property, pageType, slug) + "\""
                                        + "}")
                        .collect(Collectors.toList())
                        .toString());
    }

    public BuildOutput buildPropertyItems(String property, String pageType, String slug, List<Content> items) {
        return new BuildOutput(
                locator.getPropertySlugApiFile(property, pageType, slug),
                items.stream()
                        .map(makeJSONApiAsItem)
                        .collect(Collectors.toList())
                        .toString());
    }

    public BuildOutput buildHtml(Content c) {
        return new BuildOutput(locator.getContentFileUrl(c), makeDocument(c).toString());
    }

    public BuildOutput buildApi(Content c) {
        return new BuildOutput(locator.getContentFileApi(c), makeJSONApi.apply(c));
    }

    public Document makeDocument(Content c) {

        BiConsumer<Document, List<String>>
                // List<String>: [attribute key, attribute value, content value]
                setMetaTag = (document, l) ->
                document
                        .head()
                        .appendElement("meta").attr(l.get(0), l.get(1))
                        .attr("content", l.get(2));

        Document doc = getContentTemplate(c);

        // set document language
        doc.select("html")
                .first()
                .attr("lang", c.getSiteData().getLanguage());

        doc.title(c.getHtmlTitle());

        setMetaTag.accept(doc, List.of("name", "title", c.getTitle()));
        setMetaTag.accept(doc, List.of("name", "description", c.getDescription()));
        setMetaTag.accept(doc, List.of("name", "excerpt", c.getDescription()));
        setMetaTag.accept(doc, List.of("name", "author", c.getAuthor()));

        String url = locator.getContentUrl(c);
        String api = locator.getContentApi(c);

        setMetaTag.accept(doc, List.of("name", "url", url));
        setMetaTag.accept(doc, List.of("name", "api", api));

        List<String> tags = c.getContentData().getTags();
        String twitter = c.getTwitter();
        String image = c.getImage();

        if (!(tags == null) && !tags.isEmpty()) {
            setMetaTag.accept(doc, List.of("name", "keywords", String.join(", ", tags)));
        }

        setMetaTag.accept(doc, List.of("property", "og:title", c.getTitle()));
        setMetaTag.accept(doc, List.of("property", "og:description", c.getDescription()));
        setMetaTag.accept(doc, List.of("property", "og:type", c.getPageType()));
        setMetaTag.accept(doc, List.of("property", "og:site_name", c.getSiteData().getSiteName()));
        setMetaTag.accept(doc, List.of("property", "og:site_url", locator.getBase()));
        setMetaTag.accept(doc, List.of("property", "og:url", url));
        setMetaTag.accept(doc, List.of("property", c.getPageType() + ":published_time", c.getCreated()));
        setMetaTag.accept(doc, List.of("property", c.getPageType() + ":modified_time", c.getUpdated()));
        if (!(image == null)) {
            setMetaTag.accept(doc, List.of("property", "og:image", image));
        }

        if (!(twitter == null)) {
            setMetaTag.accept(doc, List.of("name", "twitter:site", twitter));
            setMetaTag.accept(doc, List.of("name", "twitter:creator", twitter));
            setMetaTag.accept(doc, List.of("name", "twitter:card", "summary_large_image"));
            setMetaTag.accept(doc, List.of("name", "twitter:title", c.getHtmlTitle()));
            setMetaTag.accept(doc, List.of("name", "twitter:description", c.getDescription()));
            if (!(image == null)) {
                setMetaTag.accept(doc, List.of("name", "twitter:image", image));
            }
        }

        return doc;
    }

    final Function<Map<String, Object>, String> mapToJSONString = data -> {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    public final Function<Content, String> makeJSONApi = (Content c) -> {
        Map<String, Object>
                data =
                new java.util.HashMap<>(Map.of(
                        "title", c.getTitle(),
                        "description", c.getDescription(),
                        "author", c.getAuthor(),
                        "created", c.getCreated(),
                        "updated", c.getUpdated()
                ));
        if (!(null == c.getImage())) {
            data.put("image", c.getImage());
        }
        if (!(null == c.getContentData().getTags())) {
            data.put("tags", c.getContentData().getTags());
        }
        if (!(null == c.getContentData().getCategories())) {
            data.put("categories", c.getContentData().getCategories());
        }
        if (!(null == c.getContentData().getMentions())) {
            data.put("mentions", c.getContentData().getMentions());
        }

        data.put("api", getReader().getLocator().getContentApi(c));
        data.put("url", getReader().getLocator().getContentUrl(c));
        data.put("body", c.getBody());

        return mapToJSONString.apply(data);
    };

    public final Function<Content, String> makeJSONApiAsItem = (Content c) -> {
        Map<String, Object>
                data =
                new java.util.HashMap<>(Map.of(
                        "title", c.getTitle(),
                        "author", c.getAuthor(),
                        "created", c.getCreated(),
                        "updated", c.getUpdated(),
                        "api", getReader().getLocator().getContentApi(c),
                        "url", getReader().getLocator().getContentUrl(c)
                ));
        if (!(null == c.getImage())) {
            data.put("image", c.getImage());
        }

        return mapToJSONString.apply(data);
    };

    public String makeJSONProfileAPI(SiteData site) {

        Map<String, Object>
                data =
                new java.util.HashMap<>(Map.of(
                        "siteName", site.getSiteName(),
                        "siteDescription", site.getSiteDescription(),
                        "user", site.getUser(),
                        "url", site.getUrl(),
                        "image", site.getSiteImage()
                ));

        if (!(site.getTwitter() == null)) {
            data.put("twitter", site.getTwitter());
        }
        if (!(site.getEmail() == null)) {
            data.put("email", site.getEmail());
        }

        return mapToJSONString.apply(data);

    }

    public Document makeHomeDocument(SiteData site) {

        BiConsumer<Document, List<String>>
                // List<String>: [attribute key, attribute value, content value]
                setMetaTag = (document, l) ->
                document
                        .head()
                        .appendElement("meta").attr(l.get(0), l.get(1))
                        .attr("content", l.get(2));

        Document doc = templates.get("home");

        // set document language
        doc.select("html")
                .first()
                .attr("lang", site.getLanguage());

        doc.title(site.getSiteName());

        setMetaTag.accept(doc, List.of("name", "title", site.getSiteName()));
        setMetaTag.accept(doc, List.of("name", "description", site.getSiteDescription()));
        setMetaTag.accept(doc, List.of("name", "author", site.getUser()));

        setMetaTag.accept(doc, List.of("name", "url", site.getUrl()));

        String twitter = site.getTwitter();
        String image = site.getSiteImage();


        setMetaTag.accept(doc, List.of("property", "og:title", site.getSiteName()));
        setMetaTag.accept(doc, List.of("property", "og:description", site.getSiteDescription()));
        setMetaTag.accept(doc, List.of("property", "og:type", "home_page"));
        setMetaTag.accept(doc, List.of("property", "og:site_name", site.getSiteName()));
        setMetaTag.accept(doc, List.of("property", "og:site_url", site.getUrl()));
        setMetaTag.accept(doc, List.of("property", "og:url", site.getUrl()));
        if (!(image == null)) {
            setMetaTag.accept(doc, List.of("property", "og:image", image));
        }

        if (!(twitter == null)) {
            setMetaTag.accept(doc, List.of("name", "twitter:site", twitter));
            setMetaTag.accept(doc, List.of("name", "twitter:creator", twitter));
            setMetaTag.accept(doc, List.of("name", "twitter:card", "summary_large_image"));
            setMetaTag.accept(doc, List.of("name", "twitter:title", site.getSiteName()));
            setMetaTag.accept(doc, List.of("name", "twitter:description", site.getSiteDescription()));
            if (!(image == null)) {
                setMetaTag.accept(doc, List.of("name", "twitter:image", image));
            }
        }

        return doc;
    }

    public final Function<List<Content>, List<String>>
            makeContentPageUrlList =
            (List<Content> contents) -> contents.stream()
                    .map(getReader().getLocator()::getContentUrl)
                    .collect(Collectors.toList());

    public static class BuildOutput {
        private final String path;
        private final String content;

        public BuildOutput(String contentFileUrl, String content) {
            this.path = contentFileUrl;
            this.content = content;
        }

        public String getPath() {
            return path;
        }

        public String getContent() {
            return content;
        }

    }
}



