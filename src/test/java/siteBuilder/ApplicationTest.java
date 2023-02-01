package siteBuilder;

import devgrammo.siteBuilder.Application;
import devgrammo.siteBuilder.config.SiteData;
import devgrammo.siteBuilder.config.SiteException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {

    public boolean isEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
                return !directory.iterator().hasNext();
            }
        }

        return false;
    }
    @Test
    public void shouldCleanRoot() throws SiteException, IOException {
        SiteData site = SiteData.getInstance("http://localhost:3000", "TestFiles/site.json");

        File api = new File(site.getRoot() + "/API");
        File articleDir = new File(site.getRoot() + "/article");
        File postDir = new File(site.getRoot() + "/post");
        File index = new File(site.getRoot() + "/index.html");
        File siteMap = new File(site.getRoot() + "/sitemap.txt");

        System.out.println("cleaning directories ...");
        Application.cleanRoot(site);
        System.out.println("... done.");

        assertTrue(isEmpty(api.toPath()));
        assertTrue(isEmpty(articleDir.toPath()));
        assertTrue(isEmpty(postDir.toPath()));
        assertFalse(index.exists());
        assertFalse(siteMap.exists());
    }

    @Test
    public void shouldBuildSite() throws SiteException, IOException {
        SiteData site = SiteData.getInstance("http://localhost:3000", "TestFiles/site.json");

        File api = new File(site.getRoot() + "/API");
        File articleDir = new File(site.getRoot() + "/article");
        File postDir = new File(site.getRoot() + "/post");
        File index = new File(site.getRoot() + "/index.html");
        File siteMap = new File(site.getRoot() + "/sitemap.txt");

        System.out.println("Building site ...");
        System.out.println(" - cleaning directories ...");
        Application.cleanRoot(site);
        System.out.println("   ... done.");

        Application.build(site);

        assertFalse(isEmpty(api.toPath()));
        assertFalse(isEmpty(articleDir.toPath()));
        assertFalse(isEmpty(postDir.toPath()));
        assertTrue(index.exists());
        assertTrue(siteMap.exists());
    }
}
