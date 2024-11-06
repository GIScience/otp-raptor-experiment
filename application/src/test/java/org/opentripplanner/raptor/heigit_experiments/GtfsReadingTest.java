package org.opentripplanner.raptor.heigit_experiments;

import org.junit.jupiter.api.Test;
import org.opentripplanner.GtfsTest;

class GtfsReadingTest extends GtfsTest {


  @Override
  public String getFeedName() {
    return "gtfs/simple";
  }

  @Test
  void fillTimetableRepositoryFromGtfsData() {

    // This is filling a timetableRepository which uses the OTP network model types to represent the timetable
    // Under the hood the OneBusAway GTFS modules (are used: )https://github.com/OneBusAway/onebusaway-gtfs-modules)
    // are used to read the gtfs data

    System.out.println();
    System.out.println("###################");
    System.out.println("timetableRepository.getFeedIds() = " + timetableRepository.getFeedIds());
    System.out.println("timetableRepository.getAllTripPatterns() = " + timetableRepository.getAllTripPatterns());
  }


}
