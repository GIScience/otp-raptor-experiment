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
 * Various examples illustrating Raptor base API usage
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

    requestBuilder
      .searchParams()
      .earliestDepartureTime(T00_01)
      .latestArrivalTime(T00_30)
      .searchOneIterationOnly();

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

  @Test
  void searchRangeRequestWithWindow() {
    // We currently cannot really explain the meaning of a search window in all cases

    data.withRoute(
      route(pattern("R1", STOP_B, STOP_C, STOP_D)).withTimetable(
        schedule("10:59, 11:03, 11:07"), // Too early departure
        schedule("11:01, 11:05, 11:09"), // In result set: Earliest trip after EDT
        schedule("11:04, 11:07, 11:11"), // Slower than trip in R2 that departs at same time
        schedule("11:21, 11:25, 11:27"), // In result set: Latest trip still within LAT
        schedule("11:25, 11:27, 11:35") // Too late arrival
      )
    ).withRoute(route(pattern("R2", STOP_B, STOP_C, STOP_D)).withTimetable(
      schedule("11:04, 11:05, 11:10") // In result set: Shortest trip of both R1 and R2
    ));

    requestBuilder.profile(STANDARD);
    requestBuilder.searchParams()
      .earliestDepartureTime(hm2time(11, 0))
      .latestArrivalTime(hm2time(11, 30))
      .searchWindowInSeconds(5 * 60);

    var request = requestBuilder.build();
    var response = raptorService.route(request, data);

    assertFalse(response.noConnectionFound());
    assertEquals(3, response.paths().size());

    System.out.println(PathUtils.pathsToString(response));
  }
}
