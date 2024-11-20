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
    stops.add(new TestTransfer(transferStop, 180, 18000)); // Each transfer takes 3 minutes
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

    int[] stopsForRoute = getStopsForRoute(routeIndex);
    boolean isVerticalRoute = routeIndex < numberOfColumns;

    TestTripPattern pattern = TestTripPattern.pattern("Route_" + routeIndex, stopsForRoute);

    List<TestTripSchedule.Builder> timetable = IntStream.rangeClosed(0, 23)
      .mapToObj(this::to2Digits)
      .map(hourPrefix -> IntStream.range(0, stopsForRoute.length)
        .mapToObj(stopIndexInRoute -> timeForStop(hourPrefix, stopIndexInRoute, isVerticalRoute))
        .collect(joining(" ")))
      .map(TestTripSchedule::schedule)
      .toList();

    return TestRoute.route(pattern).withTimetable(timetable.toArray(new TestTripSchedule.Builder[0]));
  }

  private String timeForStop(String hourPrefix, int stopIndexInRoute, boolean isVerticalRoute) {
    // Vertical routes start at 30 minutes past
    return isVerticalRoute
      ? hourPrefix + to2Digits(stopIndexInRoute + 30)
      : hourPrefix + to2Digits(stopIndexInRoute);
  }

  private String to2Digits(int h) {
    return String.format("%02d:", h);
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
        stops[row] = (10 * row) + routeIndex;
      }
    } else {
      for (int row = this.numberOfRows - 1; row >= 0; row--) {
        int rowIndexOfStop = this.numberOfRows - row - 1;
        stops[row] = 10 * rowIndexOfStop + routeIndex;
      }
    }
  }

  //TODO: ugly - needs cleanup
  private void fillStopsHorizontal(int routeIndex, int[] stops, boolean forwards) {
    if (forwards) {
      for (int column = 0; column < this.numberOfColumns; column++) {
        stops[column] = (10 * (routeIndex - 10)) + column;
      }
    } else {
      for (int column = this.numberOfColumns - 1; column >= 0; column--) {
        int columnIndexOfStop = this.numberOfColumns - column - 1;
        stops[column] = (10 * (routeIndex - 10)) + columnIndexOfStop;
      }
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
