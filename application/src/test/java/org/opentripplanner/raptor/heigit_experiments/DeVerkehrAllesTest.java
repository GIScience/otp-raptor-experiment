package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.opentripplanner.framework.time.TimeUtils.hm2time;
import static org.opentripplanner.raptor.heigit_experiments.QueryRaptorWithTimetable.queryRaptor;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.csv_entities.FileCsvInputSource;
import org.onebusaway.gtfs.impl.GenericDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.opentripplanner.GtfsTest;


// Requires to
// 1. download feed from https://gtfs.de/de/feeds/de_full/
// 2. move it to /application/test/resources/gtfs/de_verkehr_alles.zip
// 3. extract zip contents into directory ./de_verkehr_alles/
class DeVerkehrAllesTest extends GtfsTest {


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

  @Test
  void routeFreiburgToHamburgAltona() {

    Predicate<String> accessStopsFilter = stopName -> stopName.contains("Freiburg(Breisgau) Hbf");
    Predicate<String> egressStopsFilter = stopName -> stopName.contains("Hamburg-Altona");

    LocalDate date = LocalDate.of(2024, 11, 12);
    int edt = hm2time(8, 0);
    int lat = hm2time(20, 0);

    var searchWindow = Duration.ofHours(2);

    var response = queryRaptor(
      accessStopsFilter,
      egressStopsFilter,
      edt, lat, searchWindow, date,
      timetableRepository
    );

    assertFalse(response.paths().isEmpty());
  }

  @Test
  void routeHeidelbergKarlsruhe() {

    Predicate<String> accessStopsFilter = stopName -> {
      return stopName.contains("Heidelberg, Alois-Link-Platz")
        || stopName.contains("Heidelberg, Bergfriedhof")
        || stopName.contains("Heidelberg, Weststadt/Südstadt")
        || stopName.contains("Heidelberg, Kaiserstraße")
        ;
    };
    Predicate<String> egressStopsFilter = stopName -> {
      return stopName.contains("Karlsruhe Kronenplatz")
        || stopName.contains("Karlsruhe Durlacher Tor")
        || stopName.contains("Karlsruhe Weinweg")
        || stopName.contains("Karlsruhe Hauptfriedhof")
        ;
    };

    LocalDate date = LocalDate.of(2024, 11, 12);
    int edt = hm2time(9, 0);
    int lat = hm2time(16, 0);

    var searchWindow = Duration.ofHours(1);

    for (int i = 0; i < 10; i++) {
      var response = queryRaptor(
        accessStopsFilter,
        egressStopsFilter,
        edt, lat, searchWindow, date,
        timetableRepository
      );
      assertFalse(response.paths().isEmpty());
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
