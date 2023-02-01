package devgrammo.siteBuilder.content;

import devgrammo.siteBuilder.config.Locator;
import devgrammo.siteBuilder.config.SiteData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContentReader {
    private final SiteData site;

    public ContentReader(SiteData site) {
        this.site = site;
    }

    final Function<Path, String> readFileAsString = path -> {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    public SiteData getSite() {
        return site;
    }

    public Map<String, Document> getTemplates() {
        try (Stream<Path> paths = Files.walk(Paths.get(site.getDirectory() + "/templates"))) {
            return paths.filter(Files::isRegularFile)
                    .map(p -> {
                        try {
                            String fileName = p.getFileName().toString().replace(".html", "");
                            Document template = Jsoup.parse(p.toFile(),"ISO-8859-1");
                            return new AbstractMap.SimpleEntry<>(fileName, template);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toMap(
                            AbstractMap.SimpleEntry::getKey,
                            AbstractMap.SimpleEntry::getValue));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Locator getLocator() {
        return new Locator(site);
    }

    final Function<File, String> getPageTypeFromContentDir = file -> Path.of(file.getParent()).getFileName().toString();
    final Function<File, String> getSlugFromContentDir = File::getName;
    final Function<String, ContentBody> makeContentBody = s -> {
        try {
            Path path = Path.of(s);
            String body = readFileAsString.apply(path);
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            ZoneId zone = ZoneId.of(getSite().getTimeZone());
            String created = attr.creationTime().toInstant().atZone(zone).toString();
            String updated = attr.lastModifiedTime().toInstant().atZone(zone).toString();
            return new ContentBody(body, created, updated);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    final Function<String, ContentData> makeContentData = dirPath -> {
        try {
            return SiteData.objectMapper.readValue(Path.of(dirPath + "/content.json").toFile(), ContentData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    public Content makeContent(File file) {
        return new Content(
                site,
                getPageTypeFromContentDir.apply(file),
                getSlugFromContentDir.apply(file),
                makeContentData.apply(file.toString()),
                makeContentBody.apply(file + "/content.md"));
    }

    public final Function<String, ContentsByPage>
            getContents = (String pageTypePath) -> {
        File directory = new File(pageTypePath);
        String pageType = directory.getName();
        if (directory.exists()) {
            return new ContentsByPage(pageType,
                    Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                            .parallel()
                            .filter(File::isDirectory)
                            .map(this::makeContent)
                            .collect(Collectors.toList()));
        } else {
            return new ContentsByPage(pageType, List.of());
        }
    };

}

