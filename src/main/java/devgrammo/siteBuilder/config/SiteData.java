package devgrammo.siteBuilder.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.TimeZone;

public class SiteData {
    public static final
    ObjectMapper objectMapper = new ObjectMapper()
            .configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false)
            .configure(
                    DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
                    false);
    String siteName;
    String user;
    String siteDescription;
    String siteImage;
    String siteIcon;
    String timeZone;
    String language;
    String email;
    String twitter;
    String url;
    String directory;
    String root;

    public SiteData() {
    }

    public String getSiteName() {
        return siteName;
    }

    public String getUser() {
        return user;
    }

    public String getSiteDescription() {
        return siteDescription;
    }

    public String getSiteImage() {
        return siteImage;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getLanguage() {
        return language;
    }

    public String getEmail() {
        return email;
    }

    public String getTwitter() {
        return twitter;
    }

    public String getUrl() {
        return url;
    }

    public String getRoot() {
        return root;
    }

    public String getDirectory() {
        return directory;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "SiteData{" +
                "siteName='" + siteName + '\'' +
                ", user='" + user + '\'' +
                ", siteDescription='" + siteDescription + '\'' +
                ", siteImage='" + siteImage + '\'' +
                ", siteIcon='" + siteIcon + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", language='" + language + '\'' +
                ", email='" + email + '\'' +
                ", twitter='" + twitter + '\'' +
                ", url='" + url + '\'' +
                ", contentsDirectory='" + directory + '\'' +
                ", root='" + root + '\'' +
                '}';
    }

    private static SiteData readFile(String base, String configPath) throws SiteException {
        try {
            Set<String> TIMEZONES = Set.of(TimeZone.getAvailableIDs());

            SiteData site = objectMapper.readValue(Path.of(configPath).toFile(), SiteData.class);
            if (site.getTimeZone() == null) {
                throw new SiteException("Site exception: TimeZone is required.");
            }

            if (!TIMEZONES.contains(site.getTimeZone())) {
                throw new SiteException("Site exception: Invalid time zone " + site.getTimeZone());
            }

            if (site.getLanguage() == null) {
                throw new SiteException("Site exception: field language is required by html lang attribute.");
            }

            site.setUrl(base);
            return site;

        } catch (IOException e) {
            throw new SiteException("Site exception: " + "file " + configPath + " not found. \n" + e.getMessage());
        }
    }

    public static SiteData getInstance() {
        try {
            return readFile("http://localhost:3000", "site.json");
        } catch (SiteException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static SiteData getInstance(String base, String path) throws SiteException {
        try {
            if (base == null) {
                throw new SiteException("url site is required.");
            }
            if (path == null) {
                path = "site.json";
            }
            return readFile(base, path);
        } catch (SiteException e) {
            throw new SiteException(e.getMessage());
        }
    }

}
