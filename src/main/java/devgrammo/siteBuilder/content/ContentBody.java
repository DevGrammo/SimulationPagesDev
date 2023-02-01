package devgrammo.siteBuilder.content;

public class ContentBody {
    final String content;
    final String created;
    final String updated;

    public ContentBody(String content, String created, String updated) {
        this.content = content;
        this.created = created;
        this.updated = updated;
    }

    public String getContent() {
        return content;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
