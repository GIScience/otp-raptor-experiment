package org.opentripplanner.raptor.heigit_experiments;

import java.util.BitSet;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TripPatternForDate;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.request.BoardAlight;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.request.TransitDataProviderFilter;
import org.opentripplanner.transit.model.network.RoutingTripPattern;
import org.opentripplanner.transit.model.timetable.TripTimes;

class AcceptionTransitDataProviderFilter implements TransitDataProviderFilter {

  @Override
  public boolean tripPatternPredicate(TripPatternForDate tripPatternForDate) {
    return true;
  }

  @Override
  public boolean tripTimesPredicate(TripTimes tripTimes, boolean withFilters) {
    return true;
  }

  @Override
  public boolean hasSubModeFilters() {
    return true;
  }

  @Override
  public BitSet filterAvailableStops(
    RoutingTripPattern tripPattern,
    BitSet boardingPossible,
    BoardAlight boardAlight
  ) {
    return boardingPossible;
  }
}
