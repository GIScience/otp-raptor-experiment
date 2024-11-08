package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.opentripplanner.framework.time.TimeUtils.hm2time;
import static org.opentripplanner.raptor.heigit_experiments.QueryRaptorWithTimetable.queryRaptor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.opentripplanner.GtfsTest;

class DeFernverkehrTest extends GtfsTest {


  @Override
  public String getFeedName() {
    return "gtfs/de_fernverkehr";
  }

  @Test
  void routeHeidelbergToBerlin() {

    Predicate<String> accessStopsFilter = stopName -> stopName.contains("Heidelberg Hbf");
    Predicate<String> egressStopsFilter = stopName -> stopName.contains("Berlin Hbf");

    LocalDate date = LocalDate.of(2024, 11, 7);
    int edt = hm2time(8, 0);
    int lat = hm2time(16, 0);

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
  void routeFreiburgToHamburgAltona() {

    Predicate<String> accessStopsFilter = stopName -> stopName.contains("Freiburg(Breisgau) Hbf");
    Predicate<String> egressStopsFilter = stopName -> stopName.contains("Hamburg-Altona");

    LocalDate date = LocalDate.of(2024, 11, 7);
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

}
