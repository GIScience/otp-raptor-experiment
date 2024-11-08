package org.opentripplanner.raptor.heigit_experiments;

import static org.opentripplanner.raptor.heigit_experiments.CollectionBasedIntIterator.toSet;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opentripplanner.raptor._data.transit.TestRoute;
import org.opentripplanner.raptor._data.transit.TestTripPattern;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.model.RaptorStopNameResolver;
import org.opentripplanner.raptor.api.model.RaptorTransfer;
import org.opentripplanner.raptor.spi.IntIterator;
import org.opentripplanner.raptor.spi.RaptorConstrainedBoardingSearch;
import org.opentripplanner.raptor.spi.RaptorCostCalculator;
import org.opentripplanner.raptor.spi.RaptorPathConstrainedTransferSearch;
import org.opentripplanner.raptor.spi.RaptorRoute;
import org.opentripplanner.raptor.spi.RaptorSlackProvider;
import org.opentripplanner.raptor.spi.RaptorTransitDataProvider;


public class SynthGridTransitDataProvider implements RaptorTransitDataProvider<TestTripSchedule>{

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
  public Iterator<? extends RaptorTransfer> getTransfersFromStop(int fromStop) {
    return null;
  }

  @Override
  public Iterator<? extends RaptorTransfer> getTransfersToStop(int toStop) {
    return null;
  }

  @Override
  public IntIterator routeIndexIterator(IntIterator stops) {

    Set<Integer> stopIndices = toSet(stops);

    Set<Integer> routesTouchingStops = stopIndices
      .stream()
      .flatMap(index -> Stream.of(10 + index % 10, index / 10))
      .collect(Collectors.toSet());

    return new CollectionBasedIntIterator(routesTouchingStops);
  }


  @Override
  public RaptorRoute<TestTripSchedule> getRouteForIndex(int routeIndex) {

    int[] stops = getStopsForRoute(3);

    //TODO: better name with test - maybe 'Route_<index>'
    TestTripPattern pattern = TestTripPattern.pattern("TTP_NAME", stops);
    return TestRoute.route(pattern);
  }


  int[] getStopsForRoute(int routeIndex) {
    int[] stops = new int[rows];

    if (routeIndex < this.columns)
      fillStopsVertical(routeIndex, stops);
    else
      fillStopsHorizontal(routeIndex, stops);

    return stops;
  }

  private void fillStopsVertical(int routeIndex, int[] stops) {
    for (int row = 0; row < this.rows; row++) {
      stops[row] = (10 * row) + routeIndex;
    }
  }

  private void fillStopsHorizontal(int routeIndex, int[] stops) {
    for (int row = 0; row < this.rows; row++) {
      stops[row] = (10 * (routeIndex-10)) + row;
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
    return 0;
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
