package org.opentripplanner.raptor.heigit_experiments;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opentripplanner.raptor._data.transit.TestTransfer;
import org.opentripplanner.raptor.spi.IntIterator;

public class PrecomputedGridTransitDataProvider extends SynthGridTransitDataProvider {

  public PrecomputedGridTransitDataProvider(int size) {
    super(size);
    precompute();
  }

  public PrecomputedGridTransitDataProvider() {
    this(10);
  }

  private final int numberOfStops = numberOfColumns * numberOfRows;

  // map routeIndex to set of transfer objects
  private final Map<Integer, int[]> stopsPerRoute = new HashMap<>();

  // map stopIndex to set of route indexes
  private final Map<Integer, List<Integer>> routesPerStop = new HashMap<>();

  // map stopIndex to transfers
  private final Map<Integer, List<TestTransfer>> transfersPerStop = new HashMap<>();

  // Do not use setUp() since set up is called for each query!
  private void precompute() {
    int numberOfRoutes = numberOfRows + numberOfColumns;
    for (var routeIndex = 0; routeIndex < numberOfRoutes; routeIndex++) {
      stopsPerRoute.put(routeIndex, super.getStopsForRoute(routeIndex));
    }
//    System.out.println(stopsPerRoute);

    for (var stopIndex = 0; stopIndex < numberOfStops(); stopIndex++) {
      routesPerStop.put(stopIndex, routesPerStop(stopIndex).toList());
      transfersPerStop.put(stopIndex, calculateTransfersToNeighbouringStops(stopIndex));
    }
//    System.out.println(routesPerStop);
//    System.out.println(transfersPerStop);

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
  Iterator<TestTransfer> transfersToNeighbouringStops(int startStop) {
    var stops = transfersPerStop.get(startStop);
    return stops.iterator();
  }

}
