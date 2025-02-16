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
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.opentripplanner.GtfsTest;


// Get data as specified in https://github.com/public-transport/transitous/blob/main/feeds/de.json
class DeDelfiGesamtdeutschlandTest  extends GtfsTest {


  public String getFeedName() {
    return "gtfs/de_delfi_gesamtdeutschland";
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
     * Or you can use the internal entity store, which has references to all the
     * loaded entities
     */
    GtfsDaoImpl store = new GtfsDaoImpl();

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

    Predicate<String> accessStopsFilter = stopName -> stopName.contains("Freiburg Hauptbahnhof");
    Predicate<String> egressStopsFilter = stopName -> stopName.contains("Hamburg-Altona");

    LocalDate date = LocalDate.of(2024, 11, 12);
    int edt = hm2time(8, 0);
    int lat = hm2time(20, 0);

    var searchWindow = Duration.ofHours(2);

//    for (int i = 0; i < 10; i++) {
      var response = queryRaptor(
        accessStopsFilter,
        egressStopsFilter,
        edt, lat, searchWindow, date,
        timetableRepository
      );
      assertFalse(response.paths().isEmpty());
//    }

  }

  @Test
  void routeHeidelbergKarlsruhe() {

    Predicate<String> accessStopsFilter = stopName -> {
      return stopName.contains("Heidelberg, Alois-Link-Platz")
//        || stopName.contains("Heidelberg, Bergfriedhof")
//        || stopName.contains("Heidelberg, Weststadt/Südstadt")
//        || stopName.contains("Heidelberg, Kaiserstraße")
        ;
    };
    Predicate<String> egressStopsFilter = stopName -> {
      return stopName.contains("Karlsruhe Hauptfriedhof")
//        || stopName.contains("KA Durlacher Tor")
//        || stopName.contains("Karlsruhe Kronenplatz")
        ;
    };

    LocalDate date = LocalDate.of(2024, 11, 12);
    int edt = hm2time(10, 0);
    int lat = hm2time(14, 0);

    var searchWindow = Duration.ofHours(1);

    for (int i = 0; i < 5; i++) {
      var response = queryRaptor(
        accessStopsFilter,
        egressStopsFilter,
        edt, lat, searchWindow, date,
        timetableRepository
      );
      assertFalse(response.paths().isEmpty());
    }

  }

}
