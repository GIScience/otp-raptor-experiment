package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.opentripplanner.raptor._data.transit.TestRoute.route;
import static org.opentripplanner.raptor._data.transit.TestTripPattern.pattern;
import static org.opentripplanner.raptor._data.transit.TestTripSchedule.schedule;
import static org.opentripplanner.raptor.api.request.RaptorProfile.MIN_TRAVEL_DURATION;
import static org.opentripplanner.raptor.api.request.RaptorProfile.STANDARD;
import static org.opentripplanner.raptor.moduletests.support.RaptorModuleTestConfig.multiCriteria;
import static org.opentripplanner.raptor.moduletests.support.RaptorModuleTestConfig.standard;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentripplanner.raptor.RaptorService;
import org.opentripplanner.raptor._data.RaptorTestConstants;
import org.opentripplanner.raptor._data.api.PathUtils;
import org.opentripplanner.raptor._data.transit.TestAccessEgress;
import org.opentripplanner.raptor._data.transit.TestTransitData;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.request.RaptorRequestBuilder;
import org.opentripplanner.raptor.configure.RaptorConfig;
import org.opentripplanner.raptor.moduletests.support.RaptorModuleTestCase;

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

  /**
   * Stops: 0..3
   * <p>
   * Stop on route (stop indexes): R1:  1 - 2 - 3
   * <p>
   * Schedule: R1: 00:01 - 00:03 - 00:05
   * <p>
   * Access (toStop & duration): 1  30s
   * <p>
   * Egress (fromStop & duration): 3  20s
   */
  @BeforeEach
  void setup() {
    data.withRoute(
      route(pattern("R1", STOP_B, STOP_C, STOP_D)).withTimetable(
        schedule("00:01, 00:03, 00:05"),
        schedule("00:02, 00:05, 00:06")
      )
    );
    requestBuilder
      .searchParams()
      .addAccessPaths(TestAccessEgress.walk(STOP_B, D30s))
      .addEgressPaths(TestAccessEgress.walk(STOP_D, D20s))
      .earliestDepartureTime(T00_01)
      .latestArrivalTime(T00_10)
      .timetable(true);

    // ModuleTestDebugLogging.setupDebugLogging(data, requestBuilder);
  }

  @Test
  @DisplayName("min_travel_duration")
  void minTravelDuration() {
    requestBuilder.profile(STANDARD);
//    requestBuilder.profile(MIN_TRAVEL_DURATION);
    requestBuilder.searchParams().searchOneIterationOnly();

    var request = requestBuilder.build();
    var response = raptorService.route(request, data);

    assertFalse(response.noConnectionFound());
    System.out.println(PathUtils.pathsToString(response));
  }

//  static List<RaptorModuleTestCase> testCases() {
//    var path = "Walk 30s ~ B ~ BUS R1 0:01 0:05 ~ D ~ Walk 20s [0:00:30 0:05:20 4m50s Tₓ0 C₁940]";
//    return RaptorModuleTestCase
//      .of()
//      .addMinDuration("4m50s", TX_0, T00_00, T00_10)
//      .add(standard(), PathUtils.withoutCost(path))
//      .add(multiCriteria(), path)
//      .build();
//  }
//  @ParameterizedTest
//  @MethodSource("testCases")
//  void testRaptor(RaptorModuleTestCase testCase) {
//    assertEquals(testCase.expected(), testCase.run(raptorService, data, requestBuilder));

//  }
}
