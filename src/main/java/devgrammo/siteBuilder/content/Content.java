package devgrammo.siteBuilder.content;

import devgrammo.siteBuilder.config.SiteData;

import java.util.List;

public class Content {
    private final String slug;
    private final String pageType;
    private final SiteData siteData;
    private final ContentData contentData;
    private final ContentBody body;

    public Content(SiteData siteData, String pageType, String slug, ContentData contentData, ContentBody body) {
        this.slug = slug;
        this.pageType = pageType;
        this.siteData = siteData;
        this.contentData = contentData;
        this.body = body;
    }
    public String getSlug() {
        return slug;
    }

    public String getPageType() {
        return pageType;
    }

    public SiteData getSiteData() {
        return siteData;
    }

    public ContentData getContentData() {
        return contentData;
    }


    @Override
    public String toString() {
        return "Content{" +
                "slug='" + slug + '\'' +
                ", pageType='" + pageType + '\'' +
                ", siteData=" + siteData +
                ", contentData=" + contentData +
                ", body='" + body + '\'' +
                '}';
    }

    // htmlDoc getter

    public String getAuthor() {

        return siteData.getUser();
    }

    public String getHtmlTitle() {
        String title = contentData.getTitle();
        if (title == null) {
            title = siteData.getSiteName();
        }
        return title + " | " + getAuthor();
    }

    public String getTitle() {
        String title = contentData.getTitle();
        if (title == null) {
            title = siteData.getSiteName();
        }
        return title;
    }

    public String getDescription() {
        String description = contentData.getDescription();
        if (description == null) {
            description = siteData.getSiteDescription();
        }
        return description;
    }

    public String getImage() {
        String image = contentData.getImage();
        if (image == null) {
            image = siteData.getSiteImage();
        }
        return image;
    }
    public String getCreated() {
        return body.getCreated();
    }
    public String getUpdated() {
        return body.getUpdated();
    }

    public List<String> getPropertySlugs(String property) {

        List<String> result = null;

        if (property.equals("tag")) {
            result = getContentData().getTags();
        }
        if (property.equals("category")) {
            result = getContentData().getCategories();
        }

        if (result == null) {
            result = List.of();
        }
        return result;
    }

    public String getBody() {
        return body.getContent();
    }

    public String getTwitter() {
        return siteData.getTwitter();
    }
}

