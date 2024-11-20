package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opentripplanner.raptor.heigit_experiments.CollectionBasedIntIterator.toSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentripplanner.raptor._data.transit.TestTransfer;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.model.RaptorTripPattern;
import org.opentripplanner.raptor.spi.IntIterator;
import org.opentripplanner.raptor.spi.RaptorRoute;
import org.opentripplanner.raptor.spi.RaptorSlackProvider;

class SynthGridTransitDataProviderTest {

  SynthGridTransitDataProvider dataProvider = new SynthGridTransitDataProvider();


  @Test
  void getRouteForEvenIndex() {

    RaptorRoute<TestTripSchedule> route = this.dataProvider.getRouteForIndex(4);
    RaptorTripPattern pattern = route.pattern();

    String expected = "TestTripPattern{name: 'Route_4', stops: [4, 14, 24, 34, 44, 54, 64, 74, 84, 94], " +
      "restrictions: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]}";

    assertEquals(expected, pattern.toString());
  }

  @Test
  void getRouteForOddIndex() {

    RaptorRoute<TestTripSchedule> route = this.dataProvider.getRouteForIndex(3);
    RaptorTripPattern pattern = route.pattern();

    String expected = "TestTripPattern{name: 'Route_3', stops: [93, 83, 73, 63, 53, 43, 33, 23, 13, 3], " +
      "restrictions: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]}";

    assertEquals(expected, pattern.toString());
  }


  @Test
  void getStopsForVerticalRoute10() {
    int[] computedStops = this.dataProvider.getStopsForRoute(3);
    int[] expectedStops = {93, 83, 73, 63, 53, 43, 33, 23, 13, 3};

    assertArrayEquals(expectedStops, computedStops);
  }

  @Test
  void getStopsForVerticalRoute20() {
    var dataProvider = new SynthGridTransitDataProvider(20);
    int[] computedStops = dataProvider.getStopsForRoute(3);
    int[] expectedStops = {383, 363, 343, 323, 303, 283, 263, 243, 223, 203, 183, 163, 143, 123, 103, 83, 63, 43, 23, 3};

    assertArrayEquals(expectedStops, computedStops);
  }


  // TODO: Duplicate for size 20
  @Test
  void getStopsForHorizontalRoute() {
    int[] computedStops = this.dataProvider.getStopsForRoute(17);
    int[] expectedStops = {79, 78, 77, 76, 75, 74, 73, 72, 71, 70};

    assertArrayEquals(expectedStops, computedStops);
  }


  @Test
  void routeIndexIterator() {

    CollectionBasedIntIterator stops = new CollectionBasedIntIterator(List.of(0, 10, 46, 77, 95));
    IntIterator iterator = this.dataProvider.routeIndexIterator(stops);

    assertEquals(Set.of(0, 11, 14, 7, 5, 10, 19, 6, 17), toSet(iterator));

  }


  @Test
  void numberOfStops() {

    int stops = this.dataProvider.numberOfStops();
    assertEquals(100, stops);
  }

  @Test
  void validTransitStartAndEndTimes() {
    assertEquals(0, this.dataProvider.getValidTransitDataStartTime());
    assertEquals(24 * 60 * 60, this.dataProvider.getValidTransitDataEndTime()); // full day
  }

  @Test
  void transfersToStop() {

    assertEquals(List.of(
      new TestTransfer(12, 180, 18000),
      new TestTransfer(13, 180, 18000),
      new TestTransfer(14, 180, 18000),
      new TestTransfer(22, 180, 18000),
      new TestTransfer(24, 180, 18000),
      new TestTransfer(32, 180, 18000),
      new TestTransfer(33, 180, 18000),
      new TestTransfer(34, 180, 18000)
    ), toList(this.dataProvider.getTransfersToStop(23)));

    assertEquals(List.of(
      new TestTransfer(1, 180, 18000),
      new TestTransfer(10, 180, 18000),
      new TestTransfer(11, 180, 18000)
    ), toList(this.dataProvider.getTransfersToStop(0)));

    assertEquals(List.of(
      new TestTransfer(88, 180, 18000),
      new TestTransfer(89, 180, 18000),
      new TestTransfer(98, 180, 18000)
    ), toList(this.dataProvider.getTransfersToStop(99)));
  }

  @Test
  void transfersFromStop() {

    assertEquals(List.of(
      new TestTransfer(12, 180, 18000),
      new TestTransfer(13, 180, 18000),
      new TestTransfer(14, 180, 18000),
      new TestTransfer(22, 180, 18000),
      new TestTransfer(24, 180, 18000),
      new TestTransfer(32, 180, 18000),
      new TestTransfer(33, 180, 18000),
      new TestTransfer(34, 180, 18000)
    ), toList(this.dataProvider.getTransfersFromStop(23)));

    assertEquals(List.of(
      new TestTransfer(1, 180, 18000),
      new TestTransfer(10, 180, 18000),
      new TestTransfer(11, 180, 18000)
    ), toList(this.dataProvider.getTransfersFromStop(0)));

    assertEquals(List.of(
      new TestTransfer(88, 180, 18000),
      new TestTransfer(89, 180, 18000),
      new TestTransfer(98, 180, 18000)
    ), toList(this.dataProvider.getTransfersFromStop(99)));
  }

  @Test
  void slackProvider() {
    RaptorSlackProvider raptorSlackProvider = this.dataProvider.slackProvider();

    assertEquals(30, raptorSlackProvider.alightSlack(42));
    assertEquals(30, raptorSlackProvider.boardSlack(42));
    assertEquals(60, raptorSlackProvider.transferSlack());
  }

  @Test
  @Disabled("yet to implement")
  void multiCriteriaCostCalculator() {

  }

  private static <T> List<T> toList(Iterator<T> iterator) {
    List<T> list = new ArrayList<>();
    while (iterator.hasNext()) {
      list.add(iterator.next());
    }
    return list;
  }

}
