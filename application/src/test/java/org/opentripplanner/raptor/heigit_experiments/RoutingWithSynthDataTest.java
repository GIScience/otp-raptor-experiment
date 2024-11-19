package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.opentripplanner.raptor.api.request.RaptorProfile.STANDARD;

import org.junit.jupiter.api.Test;
import org.opentripplanner.raptor.RaptorService;
import org.opentripplanner.raptor._data.RaptorTestConstants;
import org.opentripplanner.raptor._data.api.PathUtils;
import org.opentripplanner.raptor._data.transit.TestAccessEgress;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.request.RaptorRequestBuilder;
import org.opentripplanner.raptor.configure.RaptorConfig;

public class RoutingWithSynthDataTest implements RaptorTestConstants {


  @Test
  void horizontalRouteWithoutTransfer() {
    SynthGridTransitDataProvider data = new SynthGridTransitDataProvider();

    RaptorRequestBuilder<TestTripSchedule> requestBuilder = new RaptorRequestBuilder<>();

    RaptorService<TestTripSchedule> raptorService = new RaptorService<>(
      RaptorConfig.defaultConfigForTest()
    );

    requestBuilder
      .searchParams()
      .addAccessPaths(TestAccessEgress.free(20))
      .addEgressPaths(TestAccessEgress.free(29))
      .maxNumberOfTransfers(3)
      .timetable(true);

    requestBuilder.profile(STANDARD);
    requestBuilder
      .searchParams()
      .earliestDepartureTime(T00_01)
      .latestArrivalTime(T00_30)
      .searchOneIterationOnly();

    var request = requestBuilder.build();
    var response = raptorService.route(request, data);

    assertFalse(response.noConnectionFound());
    assertEquals(1, response.paths().size());

    System.out.println(PathUtils.pathsToString(response));
  }
}
