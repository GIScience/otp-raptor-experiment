package org.opentripplanner.raptor.heigit_experiments.gtfslib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.conveyal.gtfs.GTFSFeed;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;

class GtfsLibTests {

  @Test
  void loadFromZipFile() throws Exception {
    try (ZipFile zip = new ZipFile("./src/test/resources/gtfs/simple.zip")) {
      GTFSFeed feed = new GTFSFeed();
      feed.loadFromFile(zip);

      assertEquals(28, feed.stops.size());
      assertEquals(19, feed.routes.size());
    }
  }
}
