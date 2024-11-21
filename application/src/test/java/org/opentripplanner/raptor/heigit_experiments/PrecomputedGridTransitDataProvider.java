package org.opentripplanner.raptor.heigit_experiments;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.opentripplanner.raptor._data.transit.TestTransfer;
import org.opentripplanner.raptor.spi.IntIterator;

public class PrecomputedGridTransitDataProvider extends SynthGridTransitDataProvider {

  public PrecomputedGridTransitDataProvider(int size) {
    super(size);
  }

  public PrecomputedGridTransitDataProvider() {
    super(10);
  }

  // map routeIndex to set of transfer objects
  private Map<Integer, int[]> stopsPerRoute = new HashMap<>();

  @Override
  public void setup() {
    int maxRouteIndexExcluded = numberOfRows + numberOfColumns;
    for (var routeIndex = 0; routeIndex < maxRouteIndexExcluded; routeIndex++) {
      stopsPerRoute.put(routeIndex, super.getStopsForRoute(routeIndex));
    }
//    System.out.println(stopsPerRoute);
  }

  @Override
  int[] getStopsForRoute(int routeIndex) {
    return stopsPerRoute.get(routeIndex);
  }

  @Override
  public IntIterator routeIndexIterator(IntIterator stops) {
    return super.routeIndexIterator(stops);
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
