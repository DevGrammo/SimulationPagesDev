package devgrammo.siteBuilder.content;

import java.util.List;

public class ContentsByPage {
    private final List<Content> contents;
    private final String pageType;

    public ContentsByPage(String pageType, List<Content> contents) {
        this.contents = contents;
        this.pageType = pageType;
    }

    public List<Content> getContents() {
        return contents;
    }

    public String getPageType() {
        return pageType;
    }
}
