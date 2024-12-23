package org.opentripplanner.raptor.heigit_experiments;

import static java.util.stream.Collectors.joining;
import static org.opentripplanner.raptor.heigit_experiments.CollectionBasedIntIterator.toSet;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.opentripplanner.raptor._data.transit.TestRoute;
import org.opentripplanner.raptor._data.transit.TestTransfer;
import org.opentripplanner.raptor._data.transit.TestTripPattern;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.model.RaptorStopNameResolver;
import org.opentripplanner.raptor.spi.DefaultSlackProvider;
import org.opentripplanner.raptor.spi.IntIterator;
import org.opentripplanner.raptor.spi.RaptorConstrainedBoardingSearch;
import org.opentripplanner.raptor.spi.RaptorCostCalculator;
import org.opentripplanner.raptor.spi.RaptorPathConstrainedTransferSearch;
import org.opentripplanner.raptor.spi.RaptorRoute;
import org.opentripplanner.raptor.spi.RaptorSlackProvider;
import org.opentripplanner.raptor.spi.RaptorTransitDataProvider;


public class SynthGridTransitDataProvider implements RaptorTransitDataProvider<TestTripSchedule> {

  final int numberOfRows;
  final int numberOfColumns;

  final Duration travelTimePerStop;

  public SynthGridTransitDataProvider() {
    this(10);
  }

  public SynthGridTransitDataProvider(int size, Duration travelTimePerStop) {
    numberOfRows = size;
    numberOfColumns = size;
    this.travelTimePerStop = travelTimePerStop;
  }

  public SynthGridTransitDataProvider(int size) {
    this(size, Duration.ofMinutes(1));
  }

  @Override
  public int numberOfStops() {
    return numberOfRows * numberOfColumns;
  }

  @Override
  public Iterator<TestTransfer> getTransfersFromStop(int fromStop) {
    return transfersToNeighbouringStops(fromStop);
  }

  @Override
  public Iterator<TestTransfer> getTransfersToStop(int toStop) {
    return transfersToNeighbouringStops(toStop);
  }

  Iterator<TestTransfer> transfersToNeighbouringStops(int startStop) {
    var stops = calculateTransfersToNeighbouringStops(startStop);
    return stops.iterator();
  }

  List<TestTransfer> calculateTransfersToNeighbouringStops(int startStop) {
    List<TestTransfer> stops = new ArrayList<>();
    int row = row(startStop);
    int column = column(startStop);
    addTransferStop(column - 1, row - 1, stops);
    addTransferStop(column, row - 1, stops);
    addTransferStop(column + 1, row - 1, stops);

    addTransferStop(column - 1, row, stops);
    addTransferStop(column + 1, row, stops);

    addTransferStop(column - 1, row + 1, stops);
    addTransferStop(column, row + 1, stops);
    addTransferStop(column + 1, row + 1, stops);
    return stops;
  }

  private void addTransferStop(int stopColumn, int stopRow, List<TestTransfer> stops) {
    if (stopColumn < 0 || stopRow < 0) return;
    if (stopColumn >= numberOfColumns || stopRow >= numberOfRows) return;

    int transferStop = toStopIndex(stopColumn, stopRow);
    stops.add(new TestTransfer(transferStop, 180, 18000)); // Each transfer takes 3 minutes
  }

  private int toStopIndex(int column, int row) {
    return column + this.numberOfColumns * row;
  }

  @Override
  public IntIterator routeIndexIterator(IntIterator stops) {

    Set<Integer> stopIndices = toSet(stops);

    Set<Integer> routesTouchingStops = stopIndices
      .stream()
      .flatMap(stopIndex -> routesPerStop(stopIndex))
      .collect(Collectors.toSet());

    return new CollectionBasedIntIterator(routesTouchingStops);
  }

  Stream<Integer> routesPerStop(int stopIndex) {
    return Stream.of(column(stopIndex), this.numberOfColumns + row(stopIndex));
  }

  int row(int stopIndex) {
    return stopIndex / this.numberOfColumns;
  }

  int column(int stopIndex) {
    return stopIndex % this.numberOfColumns;
  }


  @Override
  public RaptorRoute<TestTripSchedule> getRouteForIndex(int routeIndex) {

    int[] stopsForRoute = getStopsForRoute(routeIndex);
    boolean isVerticalRoute = routeIndex < numberOfColumns;

    TestTripPattern pattern = TestTripPattern.pattern("Route_" + routeIndex, stopsForRoute);

    LocalDate referenceDay = LocalDate.now();

    List<TestTripSchedule.Builder> timetable = IntStream.rangeClosed(0, 23)
      .mapToObj(h -> LocalTime.of(h, 0))
      .map(localTime -> LocalDateTime.of(referenceDay, localTime))
      .map(startTime -> IntStream.range(0, stopsForRoute.length)
        .mapToObj(stopIndexInRoute -> timeForStop(referenceDay, startTime, stopIndexInRoute, isVerticalRoute))
        .collect(joining(" ")))
      .map(TestTripSchedule::schedule)
      .toList();

    return TestRoute.route(pattern).withTimetable(timetable.toArray(new TestTripSchedule.Builder[0]));
  }

  private String timeForStop(LocalDate referenceDay, LocalDateTime tripStartingTime, int stopIndexInRoute, boolean isVerticalRoute) {

    if (isVerticalRoute) {
      tripStartingTime = tripStartingTime.plusMinutes(30);
    }

    var arrivalOffset = travelTimePerStop.multipliedBy(stopIndexInRoute);
    LocalDateTime stopArrivalTime = tripStartingTime.plus(arrivalOffset);
    return toTimeStringWithDayOffset(referenceDay, stopArrivalTime);
  }

  //TODO: ugly - needs cleanup
  int[] getStopsForRoute(int routeIndex) {
    boolean isEven = routeIndex % 2 == 0;
    int[] stops = new int[numberOfRows];

    if (routeIndex < this.numberOfColumns)
      fillStopsVertical(routeIndex, stops, isEven);
    else
      fillStopsHorizontal(routeIndex, stops, isEven);

    return stops;
  }

  //TODO: ugly - needs cleanup
  private void fillStopsVertical(int routeIndex, int[] stops, boolean forwards) {
    if (forwards) {
      for (int row = 0; row < this.numberOfRows; row++) {
        stops[row] = (this.numberOfColumns * row) + routeIndex;
      }
    } else {
      for (int row = this.numberOfRows - 1; row >= 0; row--) {
        int rowIndexOfStop = this.numberOfRows - row - 1;
        stops[row] = this.numberOfColumns * rowIndexOfStop + routeIndex;
      }
    }
  }

  //TODO: ugly - needs cleanup
  private void fillStopsHorizontal(int routeIndex, int[] stops, boolean forwards) {
    if (forwards) {
      for (int column = 0; column < this.numberOfColumns; column++) {
        stops[column] = (this.numberOfRows * (routeIndex - this.numberOfRows)) + column;
      }
    } else {
      for (int column = this.numberOfColumns - 1; column >= 0; column--) {
        int columnIndexOfStop = this.numberOfColumns - column - 1;
        stops[column] = (this.numberOfRows * (routeIndex - this.numberOfRows)) + columnIndexOfStop;
      }
    }
  }

  static String toTimeStringWithDayOffset(LocalDate referenceDay, LocalDateTime time) {
    var theTimeOnly = time.toLocalTime().toString();
    var daysOffset = time.toLocalDate().toEpochDay() - referenceDay.toEpochDay();

    return daysOffset == 0
      ? theTimeOnly
      : theTimeOnly + "+" + daysOffset + "d";
  }


  @Override
  public RaptorCostCalculator<TestTripSchedule> multiCriteriaCostCalculator() {
    return null;
  }


  @Override
  public RaptorSlackProvider slackProvider() {
    return new DefaultSlackProvider(60, 30, 30);
  }


  @Override
  public RaptorPathConstrainedTransferSearch<TestTripSchedule> transferConstraintsSearch() {
    return null;
  }


  @Override
  public RaptorStopNameResolver stopNameResolver() {
    return stopIndex -> "S#" + stopIndex;
  }

  @Override
  public int getValidTransitDataStartTime() {
    return 0;
  }


  @Override
  public int getValidTransitDataEndTime() {
    return 2 * 24 * 60 * 60; // two days in seconds
  }


  @Override
  public RaptorConstrainedBoardingSearch<TestTripSchedule> transferConstraintsForwardSearch(int routeIndex) {
    throw new UnsupportedOperationException("transferConstraintsForwardSearch");
  }


  @Override
  public RaptorConstrainedBoardingSearch<TestTripSchedule> transferConstraintsReverseSearch(int routeIndex) {
    throw new UnsupportedOperationException("transferConstraintsReverseSearch");
  }


}
