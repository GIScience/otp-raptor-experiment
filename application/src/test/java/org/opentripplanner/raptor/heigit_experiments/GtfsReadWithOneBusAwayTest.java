package org.opentripplanner.raptor.heigit_experiments;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;

class GtfsReadWithOneBusAwayTest {


  @Test
  void readGtfsDataDirectlyWithOneBusAway() throws IOException {
    // Code adapted from https://github.com/OneBusAway/onebusaway-gtfs-modules/blob/master/onebusaway-gtfs/src/test/java/org/onebusaway/gtfs/examples/GtfsReaderExampleMain.java

    GtfsReader reader = new GtfsReader();
    var gtfsLocation = new File("./src/test/resources/gtfs/simple");
    reader.setInputLocation(gtfsLocation);

    /**
     * You can register an entity handler that listens for new objects as they
     * are read
     */
    reader.addEntityHandler(new GtfsEntityHandler());

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
