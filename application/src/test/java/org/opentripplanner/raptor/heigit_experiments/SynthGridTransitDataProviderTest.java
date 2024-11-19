package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opentripplanner.raptor.heigit_experiments.CollectionBasedIntIterator.toSet;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.model.RaptorTripPattern;
import org.opentripplanner.raptor.spi.IntIterator;
import org.opentripplanner.raptor.spi.RaptorCostCalculator;
import org.opentripplanner.raptor.spi.RaptorRoute;
import org.opentripplanner.raptor.spi.RaptorSlackProvider;

class SynthGridTransitDataProviderTest {

  SynthGridTransitDataProvider dataProvider = new SynthGridTransitDataProvider();


  @Test
  void getRouteForIndex() {

    RaptorRoute<TestTripSchedule> route = this.dataProvider.getRouteForIndex(4);
    RaptorTripPattern pattern = route.pattern();

    String expected = "TestTripPattern{name: 'Route_4', stops: [4, 14, 24, 34, 44, 54, 64, 74, 84, 94], " +
      "restrictions: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]}";

    assertEquals(expected, pattern.toString());
  }


  @Test
  void getStopsForVerticalRoute() {
    int[] computedStops = this.dataProvider.getStopsForRoute(3);
    int[] expectedStops = {3, 13, 23, 33, 43, 53, 63, 73, 83, 93};

    assertArrayEquals(expectedStops, computedStops);
  }


  @Test
  void getStopsForHorizontalRoute() {
    int[] computedStops = this.dataProvider.getStopsForRoute(17);
    int[] expectedStops = {70, 71, 72, 73, 74, 75, 76, 77, 78, 79};

    assertArrayEquals(expectedStops, computedStops);
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

  @Test
  @Disabled("yet to implement")
  void validTransitStartAndEndTimes() {
    //getValidTransitDataStartTime()
    //getValidTransitDataEndTime()
  }

  @Test
  @Disabled("yet to implement")
  void transfers() {
    //getTransfersFromStop(int fromStop)
    //getTransfersToStop(int toStop)
  }

  @Test
  @Disabled("yet to implement")
  void multiCriteriaCostCalculator() {

  }

  @Test
  @Disabled("yet to implement")
  void slackProvider() {

  }


}