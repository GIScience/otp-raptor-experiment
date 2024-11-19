package org.opentripplanner.raptor.heigit_experiments;

import static org.opentripplanner.raptor.heigit_experiments.CollectionBasedIntIterator.toSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opentripplanner.raptor._data.transit.TestRoute;
import org.opentripplanner.raptor._data.transit.TestTransfer;
import org.opentripplanner.raptor._data.transit.TestTripPattern;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.model.RaptorStopNameResolver;
import org.opentripplanner.raptor.spi.IntIterator;
import org.opentripplanner.raptor.spi.RaptorConstrainedBoardingSearch;
import org.opentripplanner.raptor.spi.RaptorCostCalculator;
import org.opentripplanner.raptor.spi.RaptorPathConstrainedTransferSearch;
import org.opentripplanner.raptor.spi.RaptorRoute;
import org.opentripplanner.raptor.spi.RaptorSlackProvider;
import org.opentripplanner.raptor.spi.RaptorTransitDataProvider;


public class SynthGridTransitDataProvider implements RaptorTransitDataProvider<TestTripSchedule> {

  int rows = 10;
  int columns = 10;

  int[] stops;

  @Override
  public void setup() {

    this.stops = new int[this.numberOfStops()];

    for (int index = 0; index < rows * columns; index++) {
      this.stops[index] = index;
    }

  }

  @Override
  public int numberOfStops() {
    return rows * columns;
  }

  @Override
  public Iterator<TestTransfer> getTransfersFromStop(int fromStop) {
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

  private void addTransferStop(int column1, int row1, List<TestTransfer> stops) {
    int transferStop = toStopIndex(column1, row1);
    stops.add(new TestTransfer(transferStop, 60, 6000));
  }

  private int toStopIndex(int column, int row) {
    return column + 10 * row;
  }

  @Override
  public Iterator<TestTransfer> getTransfersToStop(int toStop) {
    return null;
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
    return TestRoute.route(pattern);
  }


  //TODO: ugly - needs cleanup
  int[] getStopsForRoute(int routeIndex) {
    int[] stops = new int[rows];

    if (routeIndex < this.columns)
      fillStopsVertical(routeIndex, stops);
    else
      fillStopsHorizontal(routeIndex, stops);

    return stops;
  }

  //TODO: ugly - needs cleanup
  private void fillStopsVertical(int routeIndex, int[] stops) {
    for (int row = 0; row < this.rows; row++) {
      stops[row] = (10 * row) + routeIndex;
    }
  }

  //TODO: ugly - needs cleanup
  private void fillStopsHorizontal(int routeIndex, int[] stops) {
    for (int row = 0; row < this.rows; row++) {
      stops[row] = (10 * (routeIndex - 10)) + row;
    }
  }


  @Override
  public RaptorCostCalculator<TestTripSchedule> multiCriteriaCostCalculator() {
    return null;
  }


  @Override
  public RaptorSlackProvider slackProvider() {
    return null;
  }


  @Override
  public RaptorPathConstrainedTransferSearch<TestTripSchedule> transferConstraintsSearch() {
    return null;
  }


  @Override
  public RaptorStopNameResolver stopNameResolver() {
    return null;
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
    return null;
  }


  @Override
  public RaptorConstrainedBoardingSearch<TestTripSchedule> transferConstraintsReverseSearch(int routeIndex) {
    return null;
  }


}
