package org.opentripplanner.raptor.heigit_experiments;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.csv_entities.FileCsvInputSource;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;


// Requires to
// 1. download feed from https://gtfs.de/de/feeds/de_full/
// 2. move it to /application/test/resources/gtfs/de_verkehr_alles.zip
// 3. extract zip contents into directory ./de_verkehr_alles/
class DeVerkehrAllesTest { //extends GtfsTest {


  public String getFeedName() {
    return "gtfs/de_verkehr_alles";
  }

  /**
   * Currently fails with OutOfMemoryError
   * @throws IOException
   */
  @Test
  void readGtfsDataWithOneBusAway() throws IOException {

    GtfsReader reader = new GtfsReader();

    // I did not find a simple way to hand in a reader, an input stream or a path.
    // This probably requires a self-made implementation of CsvInputSource
    var gtfsFolder = new File("./src/test/resources/" + getFeedName());
    reader.setInputSource(new FileCsvInputSource(gtfsFolder));
    reader.setInputLocation(gtfsFolder);

    /**
     * You can register an entity handler that listens for new objects as they
     * are read
     */
    // reader.addEntityHandler(new GtfsEntityHandler());

    /**
     * Or you can use the internal entity store, which has references to all the
     * loaded entities
     */
    GtfsDaoImpl store = new GtfsDaoImpl();
    reader.setEntityStore(store);

    reader.run();

    // Access entities through the store
    Map<AgencyAndId, Route> routesById = store.getEntitiesByIdForEntityType(
      AgencyAndId.class, Route.class);

    for (Route route : routesById.values()) {
      System.out.println("route: " + route.getShortName());
    }
  }

  private static class GtfsEntityHandler implements EntityHandler {

    public void handleEntity(Object bean) {
      if (bean instanceof Stop) {
        Stop stop = (Stop) bean;
        System.out.println("stop: " + stop.getName());
      }
    }
  }


}
