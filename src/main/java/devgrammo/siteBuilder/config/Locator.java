package devgrammo.siteBuilder.config;

import devgrammo.siteBuilder.content.Content;

public class Locator {
    private final String root;
    private final String base;
    public Locator(SiteData site) {
        this.root = site.getRoot();
        this.base = site.getUrl();
    }

    public String getRoot(){
        return this.root;
    }

    public String getContentUrl(Content c){
        return this.base + "/" + c.getPageType() + "/" + c.getSlug();
    }

    public String getContentFileUrl(Content c){
        return this.root + "/" + c.getPageType() + "/" + c.getSlug() + "/index.html";
    }

    public String getContentApi(Content c){
        return this.base + "/API/" + c.getPageType() + "/" + c.getSlug() + ".json";
    }

    public String getContentFileApi(Content c){
        return this.root + "/API/" + c.getPageType() + "/" + c.getSlug() + ".json";
    }

    public String getBase() {
        return this.base;
    }

    public String getListFileApi(String pageType) {
        return this.root + "/API/" + pageType + "/" + "index.json";
    }

    public String getPropertyApiFile(String property, String pageType) {
        return this.root + "/API/" + property + "/" + pageType + ".json";
    }

    public String getPropertySlugApiFile(String property, String pageType, String slug) {
        return this.root + "/API/" + property + "/" + slug + "/" + pageType + "/index.json";
    }

    public String getPropertySlugApiUrl(String property, String pageType, String slug) {
        return this.base + "/API/" + property + "/" + slug + "/" + pageType + "/index.json";
    }
}
