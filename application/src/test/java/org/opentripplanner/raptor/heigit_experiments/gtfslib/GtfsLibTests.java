package org.opentripplanner.raptor.heigit_experiments.gtfslib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.conveyal.gtfs.GTFSFeed;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GtfsLibTests {

  @Test
  void loadSimpleFromZipFile() throws Exception {
    try (ZipFile zip = new ZipFile("./src/test/resources/gtfs/simple.zip")) {
      GTFSFeed feed = new GTFSFeed();
      feed.loadFromFile(zip);

      assertEquals(28, feed.stops.size());
      assertEquals(19, feed.routes.size());

      feed.stops.forEach(
        (key, stop) -> System.out.println(stop.stop_id)
      );
      feed.close();
    }
  }

  @Test
  void loadCaltrain() throws Exception {
    GTFSFeed feed = new GTFSFeed();
    feed.loadFromFile(new ZipFile("./src/test/resources/gtfs/caltrain_gtfs.zip"));

    assertEquals(31, feed.stops.size());
    assertEquals(3, feed.routes.size());
    assertEquals(4560, feed.stop_times.size());
  }

  @Test
  void loadDe() throws Exception {
    GTFSFeed feed = new GTFSFeed();
    feed.loadFromFile(new ZipFile("./src/test/resources/gtfs/de_verkehr_alles.zip"));

    assertEquals(667252, feed.stops.size());
//    assertEquals(3, feed.routes.size());
//    assertEquals(4560, feed.stop_times.size());
  }

  @Test
  void loadDelfiGesamtdeutschland() throws Exception {
    GTFSFeed feed = new GTFSFeed();
    feed.loadFromFile(new ZipFile("./src/test/resources/gtfs/de_delfi_gesamtdeutschland.zip"));

    System.out.println(feed.stops.size());
    System.out.println(feed.routes.size());
    System.out.println(feed.stop_times.size());
  }

  @Test
  @Disabled("takes too long")
  void loadBKG() throws Exception {
    GTFSFeed feed = new GTFSFeed();
    feed.loadFromFile(new ZipFile("./src/test/resources/gtfs/bkg_gtfs.zip"));

    System.out.println(feed.stops.size());
    System.out.println(feed.routes.size());
    System.out.println(feed.stop_times.size());
  }

}
