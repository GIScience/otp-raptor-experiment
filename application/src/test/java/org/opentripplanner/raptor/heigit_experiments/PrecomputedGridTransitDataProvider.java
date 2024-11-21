package org.opentripplanner.raptor.heigit_experiments;

import static org.opentripplanner.raptor.heigit_experiments.CollectionBasedIntIterator.toSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentripplanner.raptor._data.transit.TestTransfer;
import org.opentripplanner.raptor.spi.IntIterator;

public class PrecomputedGridTransitDataProvider extends SynthGridTransitDataProvider {

  public PrecomputedGridTransitDataProvider(int size) {
    super(size);
  }

  public PrecomputedGridTransitDataProvider() {
    super(10);
  }

  private final int numberOfStops = numberOfColumns * numberOfRows;

  // map routeIndex to set of transfer objects
  private final Map<Integer, int[]> stopsPerRoute = new HashMap<>();

  // map stopIndex to set of route indexes
  private final Map<Integer, List<Integer>> routesPerStop = new HashMap<>();

  @Override
  public void setup() {
    int numberOfRoutes = numberOfRows + numberOfColumns;
    for (var routeIndex = 0; routeIndex < numberOfRoutes; routeIndex++) {
      stopsPerRoute.put(routeIndex, super.getStopsForRoute(routeIndex));
    }
//    System.out.println(stopsPerRoute);

    for (var stopIndex = 0; stopIndex < numberOfStops(); stopIndex++) {
      routesPerStop.put(stopIndex, routesPerStop(stopIndex).toList());
    }
    System.out.println(routesPerStop);

  }

  @Override
  public int numberOfStops() {
    return numberOfStops;
  }

  @Override
  int[] getStopsForRoute(int routeIndex) {
    return stopsPerRoute.get(routeIndex);
  }

  @Override
  public IntIterator routeIndexIterator(IntIterator stops) {
    // I think it's not feasible to precompute routes for all possible sets of stops.
    // But it could be cached.
    Set<Integer> routesTouchingStops = new HashSet<>();
    while (stops.hasNext()) {
      var stopIndex = stops.next();
      routesTouchingStops.addAll(routesPerStop.get(stopIndex));
    }
    return new CollectionBasedIntIterator(routesTouchingStops);
  }

  @Override
  public Iterator<TestTransfer> getTransfersFromStop(int fromStop) {
    return super.getTransfersFromStop(fromStop);
  }

  @Override
  public Iterator<TestTransfer> getTransfersToStop(int toStop) {
    return super.getTransfersToStop(toStop);
  }

}
