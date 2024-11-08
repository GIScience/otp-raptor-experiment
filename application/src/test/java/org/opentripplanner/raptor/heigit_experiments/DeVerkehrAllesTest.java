package org.opentripplanner.raptor.heigit_experiments;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.csv_entities.FileCsvInputSource;
import org.onebusaway.gtfs.impl.GenericDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;


// Requires to
// 1. download feed from https://gtfs.de/de/feeds/de_full/
// 2. move it to /application/test/resources/gtfs/de_verkehr_alles.zip
// 3. extract zip contents into directory ./de_verkehr_alles/
class DeVerkehrAllesTest { //extends GtfsTest {


  public String getFeedName() {
    return "gtfs/de_verkehr_alles";
//    return "gtfs/de_fernverkehr";
  }

  /**
   * Currently fails with OutOfMemoryError
   */
  @Test
  void readGtfsDataWithOneBusAway() throws IOException {

    GtfsReader reader = new GtfsReader();

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
    //GtfsDaoImpl store = new GtfsDaoImpl();

    SimpleGenericMutableDao store = new SimpleGenericMutableDao();

    reader.setEntityStore(store);

    reader.run();

    // Access entities through the store
    Collection<AgencyAndId> agencyAndIds = store.getAllEntitiesForType(AgencyAndId.class);

    for (AgencyAndId agency : agencyAndIds) {
      System.out.println("agency: " + agency.getAgencyId());
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

  private static class SimpleGenericMutableDao extends GenericDaoImpl {

    long count = 1;

    @Override
    public void saveEntity(Object o) {
      count++;
      if (count % 100000 == 0)
        System.out.println(count + ": " + o);
      super.saveEntity(o);
    }

//    @Override
//    public void updateEntity(Object o) {
//
//    }
//
//    @Override
//    public void saveOrUpdateEntity(Object o) {
//
//    }
  }

}
