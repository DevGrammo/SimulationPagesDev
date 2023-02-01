package devgrammo.siteBuilder;

import devgrammo.siteBuilder.config.SiteData;
import devgrammo.siteBuilder.config.SiteException;
import devgrammo.siteBuilder.content.Content;
import devgrammo.siteBuilder.content.ContentReader;
import devgrammo.siteBuilder.content.ContentsByPage;
import devgrammo.siteBuilder.content.property.PropertyIndex;
import devgrammo.siteBuilder.publisher.Publisher;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Application {

    public static void purgeDirectory(File dir) {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }

    public static void cleanRoot(SiteData site) {
        List<File> directories = List.of(
                new File(site.getRoot() + "/API"),
                new File(site.getRoot() + "/article"),
                new File(site.getRoot() + "/post"));
        directories.forEach(Application::purgeDirectory);

        File index = new File(site.getRoot() + "/index.html");
        File siteMap = new File(site.getRoot() + "/sitemap.txt");
        index.delete();
        siteMap.delete();
    }

    public static void publishPages(Publisher pub, List<String> pageTypes, List<String> properties) {

        ContentReader r = pub.getReader();
        // publishing pages
        List<List<Content>> contentsList =
                pageTypes.stream()
                        .parallel()
                        .map(s -> {
                            ContentsByPage contents = r.getContents.apply(r.getSite().getDirectory() + "/" + s);
                            pub.publishContentList(contents.getContents());
                            properties.stream()
                                    .parallel()
                                    .forEach(
                                            p -> pub.publishPropertyTree(
                                                    PropertyIndex.makePropertyIndex(p, contents)));
                            return contents;
                        })
                        .map(ContentsByPage::getContents)
                        .collect(Collectors.toList());

        // publishing home page
        pub.publishHomePage(r.getSite());
        // publish site map
        pub.publishSiteMap(contentsList);

    }

    public static void build(SiteData site) {

        ContentReader r = new ContentReader(site);
        Publisher pub = new Publisher(r);
        publishPages(pub,
                List.of("post", "article"),
                List.of("tag", "category"));

    }

    public static void main(String[] args) throws SiteException {

        String url = args[0];
        String path;
        long len = Arrays.stream(args).count();
        if (len == 2) {
            path = args[1];
        } else {
            path = null;
        }

            SiteData site = SiteData.getInstance(url, path);
            System.out.println("cleaning docs ...");
            cleanRoot(site);
            System.out.println("... building site.");
            build(site);
            System.out.println("... done\nBye.");

    }
}
