package devgrammo.siteBuilder.content;

import java.util.List;

public class ContentData {
    private String title;
    private String description;
    private String image;
    private List<String> mentions;
    private List<String> categories;
    private List<String> tags;
    private String template;

    public ContentData() {
    }

    public ContentData(String title, String description, String image, List<String> mentions, List<String> categories, List<String> tags, String template) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.mentions = mentions;
        this.categories = categories;
        this.tags = tags;
        this.template = template;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public List<String> getMentions() {
        return mentions;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getTemplate() {
        return template;
    }

}
