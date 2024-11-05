package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.opentripplanner.framework.time.TimeUtils.hm2time;
import static org.opentripplanner.raptor._data.transit.TestRoute.route;
import static org.opentripplanner.raptor._data.transit.TestTripPattern.pattern;
import static org.opentripplanner.raptor._data.transit.TestTripSchedule.schedule;
import static org.opentripplanner.raptor.api.request.RaptorProfile.STANDARD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentripplanner.raptor.RaptorService;
import org.opentripplanner.raptor._data.RaptorTestConstants;
import org.opentripplanner.raptor._data.api.PathUtils;
import org.opentripplanner.raptor._data.transit.TestAccessEgress;
import org.opentripplanner.raptor._data.transit.TestTransitData;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.request.RaptorRequestBuilder;
import org.opentripplanner.raptor.configure.RaptorConfig;

/**
 * FEATURE UNDER TEST
 * <p>
 * Raptor should return a path if it exists for the most basic case with one route with one trip, an
 * access and an egress path.
 */
public class RoutingAPITest implements RaptorTestConstants {

  private final TestTransitData data = new TestTransitData();
  private final RaptorRequestBuilder<TestTripSchedule> requestBuilder = new RaptorRequestBuilder<>();
  private final RaptorService<TestTripSchedule> raptorService = new RaptorService<>(
    RaptorConfig.defaultConfigForTest()
  );

  @BeforeEach
  void setup() {
    requestBuilder
      .searchParams()
      .addAccessPaths(TestAccessEgress.walk(STOP_B, D30s))
      .addEgressPaths(TestAccessEgress.walk(STOP_D, D20s))
      .maxNumberOfTransfers(3)
      .earliestDepartureTime(T00_01)
      .latestArrivalTime(T00_30)
      .timetable(true);
    // ModuleTestDebugLogging.setupDebugLogging(data, requestBuilder);
  }

  @Test
  void findTwoParetoOptimalSolutionsWithStandardProfile() {
    data.withRoute(
      route(pattern("R1", STOP_B, STOP_C, STOP_D)).withTimetable(
        schedule("00:01, 00:05, 00:09"),
        schedule("00:03, 00:06, 00:10")
      )
    ).withRoute(
      route(pattern("R2", STOP_A, STOP_C, STOP_D)).withTimetable(
        schedule("00:01, 00:07, 00:08")
      )
    );

    requestBuilder.profile(STANDARD);
    requestBuilder.searchParams().searchOneIterationOnly();

    var request = requestBuilder.build();
    var response = raptorService.route(request, data);

    assertFalse(response.noConnectionFound());
    assertEquals(2, response.paths().size());

    System.out.println(PathUtils.pathsToString(response));
  }

  @Test
  void noTransferUnderOneMinute() {
    data.withRoute(
      route(pattern("R1", STOP_B, STOP_C, STOP_D)).withTimetable(
        schedule("00:01, 00:05, 00:09"),
        schedule("00:03, 00:06, 00:10")
      )
    ).withRoute(
      route(pattern("R2", STOP_A, STOP_C, STOP_D)).withTimetable(
        schedule("00:01, 00:06:59, 00:08")
      )
    );

    requestBuilder.profile(STANDARD);
    requestBuilder.searchParams().searchOneIterationOnly();

    var request = requestBuilder.build();
    var response = raptorService.route(request, data);

    assertFalse(response.noConnectionFound());
    assertEquals(1, response.paths().size());

    System.out.println(PathUtils.pathsToString(response));
  }

  @Test
  void searchRangeRequestWithWindow() {
    // We currently cannot really explain the meaning of a search window

    data.withRoute(
      route(pattern("R1", STOP_B, STOP_C, STOP_D)).withTimetable(
        schedule("00:01, 00:05, 00:09"),
        schedule("00:04, 00:07, 00:11"),
        schedule("00:15, 00:20, 00:23"),
        schedule("00:20, 00:25, 00:29")
      )
    );

    requestBuilder.profile(STANDARD);
    requestBuilder.searchParams()
      .earliestDepartureTime(hm2time(0, 1))
      .latestArrivalTime(hm2time(0, 30))
      .searchWindowInSeconds(14*60+31);

    var request = requestBuilder.build();
    var response = raptorService.route(request, data);

    assertFalse(response.noConnectionFound());
    // assertEquals(1, response.paths().size());

    System.out.println(PathUtils.pathsToString(response));
  }
}
