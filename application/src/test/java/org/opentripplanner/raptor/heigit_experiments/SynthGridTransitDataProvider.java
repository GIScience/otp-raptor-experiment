package org.opentripplanner.raptor.heigit_experiments;

import static java.util.stream.Collectors.joining;
import static org.opentripplanner.raptor.heigit_experiments.CollectionBasedIntIterator.toSet;

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

  int numberOfRows = 10;
  int numberOfColumns = 10;

  int[] stops;

  @Override
  public void setup() {

    this.stops = new int[this.numberOfStops()];

    for (int index = 0; index < numberOfRows * numberOfColumns; index++) {
      this.stops[index] = index;
    }

  }

  @Override
  public int numberOfStops() {
    return numberOfRows * numberOfColumns;
  }

  @Override
  public Iterator<TestTransfer> getTransfersFromStop(int fromStop) {
    return calculateTransfersToNeighbouringStops(fromStop);
  }

  private Iterator<TestTransfer> calculateTransfersToNeighbouringStops(int fromStop) {
    List<TestTransfer> stops = new ArrayList<>();
    int row = row(fromStop);
    int column = column(fromStop);
    addTransferStop(column - 1, row - 1, stops);
    addTransferStop(column, row - 1, stops);
    addTransferStop(column + 1, row - 1, stops);

    addTransferStop(column - 1, row, stops);
    addTransferStop(column + 1, row, stops);

    addTransferStop(column - 1, row + 1, stops);
    addTransferStop(column, row + 1, stops);
    addTransferStop(column + 1, row + 1, stops);

    return stops.iterator();
  }

  private void addTransferStop(int stopColumn, int stopRow, List<TestTransfer> stops) {
    if (stopColumn < 0 || stopRow < 0) return;
    if (stopColumn >= numberOfColumns || stopRow >= numberOfRows) return;

    int transferStop = toStopIndex(stopColumn, stopRow);
    stops.add(new TestTransfer(transferStop, 60, 6000));
  }

  private int toStopIndex(int column, int row) {
    return column + 10 * row;
  }

  @Override
  public Iterator<TestTransfer> getTransfersToStop(int toStop) {
    return calculateTransfersToNeighbouringStops(toStop);
  }

  @Override
  public IntIterator routeIndexIterator(IntIterator stops) {

    Set<Integer> stopIndices = toSet(stops);

    Set<Integer> routesTouchingStops = stopIndices
      .stream()
      .flatMap(index -> Stream.of(column(index), 10 + row(index)))
      .collect(Collectors.toSet());

    return new CollectionBasedIntIterator(routesTouchingStops);
  }

  private static int row(int stopIndex) {
    return stopIndex / 10;
  }

  private static int column(int stopIndex) {
    return stopIndex % 10;
  }


  @Override
  public RaptorRoute<TestTripSchedule> getRouteForIndex(int routeIndex) {

    int[] stops = getStopsForRoute(routeIndex);

    TestTripPattern pattern = TestTripPattern.pattern("Route_" + routeIndex, stops);

    String schedule = IntStream.range(0, stops.length)
      .mapToObj(i -> "00:0" + i)
      .collect(joining(" "));

    List<TestTripSchedule.Builder> timetable = IntStream.rangeClosed(0, 23)
      .mapToObj(this::to2Digits)
      .map(hourPrefix -> IntStream.range(0, stops.length)
        .mapToObj(i -> hourPrefix + to2Digits(i))
        .collect(joining(" ")))
      .map(TestTripSchedule::schedule)
      .toList();

    return TestRoute.route(pattern).withTimetable(timetable.toArray(new TestTripSchedule.Builder[0]));
  }

  private String to2Digits(int h) {
    return String.format("%02d:", h);
  }


  //TODO: ugly - needs cleanup
  int[] getStopsForRoute(int routeIndex) {
    int[] stops = new int[numberOfRows];

    if (routeIndex < this.numberOfColumns)
      fillStopsVertical(routeIndex, stops);
    else
      fillStopsHorizontal(routeIndex, stops);

    return stops;
  }

  //TODO: ugly - needs cleanup
  private void fillStopsVertical(int routeIndex, int[] stops) {
    for (int row = 0; row < this.numberOfRows; row++) {
      stops[row] = (10 * row) + routeIndex;
    }
  }

  //TODO: ugly - needs cleanup
  private void fillStopsHorizontal(int routeIndex, int[] stops) {
    for (int row = 0; row < this.numberOfRows; row++) {
      stops[row] = (10 * (routeIndex - 10)) + row;
    }
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
    return 24 * 60 * 60;
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
