package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.*;
import static org.opentripplanner.raptor.heigit_experiments.CollectionBasedIntIterator.toSet;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.model.RaptorTripPattern;
import org.opentripplanner.raptor.spi.IntIterator;
import org.opentripplanner.raptor.spi.RaptorRoute;
import org.opentripplanner.raptor.spi.RaptorTimeTable;

class SynthGridTransitDataProviderTest {

  SynthGridTransitDataProvider dataProvider = new SynthGridTransitDataProvider();


  @Test
  void getRouteForIndex() {

    RaptorRoute<TestTripSchedule> route = this.dataProvider.getRouteForIndex(3);

    RaptorTimeTable<TestTripSchedule> timetable = route.timetable();
    RaptorTripPattern pattern = route.pattern();

    System.out.println("timetable = " + timetable);
    System.out.println("pattern = " + pattern);

  }


  @Test
  void routeIndexIterator() {

    CollectionBasedIntIterator stops = new CollectionBasedIntIterator(List.of(0, 10, 46, 77, 95));
    IntIterator iterator = this.dataProvider.routeIndexIterator(stops);

    assertEquals(Set.of(0, 1, 4, 7, 9, 10, 15, 16, 17), toSet(iterator));

  }


  @Test
  void numberOfStops() {

    int stops = this.dataProvider.numberOfStops();
    assertEquals(100, stops);
  }


}