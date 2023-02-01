package siteBuilder.config;

import devgrammo.siteBuilder.config.SiteData;
import devgrammo.siteBuilder.config.SiteException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SiteDataTest {
    @Test
    public void shouldReadSiteData() {
        SiteData site = SiteData.getInstance();
        assertEquals("site-name-test", site.getSiteName());
    }
    @Test
    public void shouldReadSiteDataFromPath() throws SiteException {
        SiteData site = SiteData.getInstance("http://localhost:3000", "TestFiles/site.json");
        assertEquals("site-name-test", site.getSiteName());
        assertEquals("TestFiles/ContentsTest", site.getDirectory());
    }

    @Test
    void shouldReturnException() {

        SiteException thrown0 = Assertions.assertThrows(SiteException.class, () -> {
            SiteData site = SiteData.getInstance("http://localhost:3000", "failing path");
            System.out.println(site);
        });

        Assertions.assertTrue(thrown0.getMessage().startsWith("Site exception"));

        SiteException thrown1 = Assertions.assertThrows(SiteException.class, () -> {
            SiteData site = SiteData.getInstance("http://localhost:3000", "TestFiles/siteNoTimeZone.json");
            System.out.println(site);
        });
        Assertions.assertTrue(thrown1.getMessage().startsWith("Site exception"));

        SiteException thrown2 = Assertions.assertThrows(SiteException.class, () -> {
            SiteData site = SiteData.getInstance("http://localhost:3000","TestFiles/siteInvalidTimeZone.json");
            System.out.println(site);
        });
        Assertions.assertTrue(thrown2.getMessage().startsWith("Site exception"));

        Assertions.assertTrue(thrown1.getMessage().startsWith("Site exception"));

        SiteException thrown3 = Assertions.assertThrows(SiteException.class, () -> {
            SiteData site = SiteData.getInstance("http://localhost:3000","TestFiles/siteNoLanguage.json");
            System.out.println(site);
        });
        Assertions.assertTrue(thrown3.getMessage().startsWith("Site exception"));
    }
}
