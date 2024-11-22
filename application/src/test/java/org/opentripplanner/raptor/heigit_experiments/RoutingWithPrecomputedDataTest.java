package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.opentripplanner.framework.time.TimeUtils.hm2time;
import static org.opentripplanner.raptor.api.request.RaptorProfile.STANDARD;

import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentripplanner.raptor.RaptorService;
import org.opentripplanner.raptor._data.RaptorTestConstants;
import org.opentripplanner.raptor._data.api.PathUtils;
import org.opentripplanner.raptor._data.transit.TestAccessEgress;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.path.RaptorPath;
import org.opentripplanner.raptor.api.request.RaptorRequestBuilder;
import org.opentripplanner.raptor.api.response.RaptorResponse;
import org.opentripplanner.raptor.configure.RaptorConfig;

public class RoutingWithPrecomputedDataTest implements RaptorTestConstants {


  @Test
  void horizontalRouteWithoutTransferButAccessEgressWalks() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.walk(20, 60)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.walk(29, 60)
    );

    var response = findTransitRoutes(
      access, egress,
      hm2time(0, 50), hm2time(1, 30),
      3,
      new PrecomputedGridTransitDataProvider()
    );

    assertFalse(response.noConnectionFound());
    assertEquals(1, response.paths().size());

    System.out.println(PathUtils.pathsToString(response));
  }

  @Test
  void verticalRouteWithoutTransfer() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.free(12)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.free(72)
    );

    var response = findTransitRoutes(
      access, egress,
      hm2time(10, 20), hm2time(11, 0),
      3, new PrecomputedGridTransitDataProvider()
    );

    assertFalse(response.noConnectionFound());
    assertEquals(1, response.paths().size());

    System.out.println(PathUtils.pathsToString(response));
  }

  @Test
  void transferRequiredAndBackwardsMove() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.free(12)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.free(23)
    );

    PrecomputedGridTransitDataProvider data = new PrecomputedGridTransitDataProvider();
    var response = findTransitRoutes(
      access, egress,
      hm2time(12, 0), hm2time(14, 0),
      3, data
    );

    assertFalse(response.noConnectionFound());
//    assertEquals(1, response.paths().size());

    for (RaptorPath<TestTripSchedule> path : response.paths()) {
      path.legStream().forEach(System.err::println);
      System.out.println(path.toString(data.stopNameResolver()));
    }

//    System.out.println(PathUtils.pathsToString(response));
  }

  @Test
  void twoTransfersRequired() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.free(102),
      TestAccessEgress.free(103)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.free(998),
      TestAccessEgress.free(999)
    );

    PrecomputedGridTransitDataProvider data = new PrecomputedGridTransitDataProvider(100);
    var response = findTransitRoutes(
      access, egress,
      hm2time(10, 0), hm2time(14, 0),
      5, data
    );

    assertFalse(response.noConnectionFound());

    for (RaptorPath<TestTripSchedule> path : response.paths()) {
      path.legStream().forEach(System.err::println);
      System.out.println(path.toString(data.stopNameResolver()));
    }

  }

  @Test
  @Disabled
  void twoTransfersRequired1000() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.free(1002),
      TestAccessEgress.free(1003),
      TestAccessEgress.free(1004)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.free(999997),
      TestAccessEgress.free(999998),
      TestAccessEgress.free(999999)
    );

    PrecomputedGridTransitDataProvider data = new PrecomputedGridTransitDataProvider(1000);
    var response = findTransitRoutes(
      access, egress,
      hm2time(0, 0), hm2time(48, 0),
      5, data
    );

    assertFalse(response.noConnectionFound());

    for (RaptorPath<TestTripSchedule> path : response.paths()) {
      path.legStream().forEach(System.err::println);
      System.out.println(path.toString(data.stopNameResolver()));
    }

  }

  @Test
  @Disabled
  void manyAccessAndEgressStops() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.free(500),
      TestAccessEgress.free(501),
      TestAccessEgress.free(502),
      TestAccessEgress.free(503),
      TestAccessEgress.free(504),
      TestAccessEgress.free(505),
      TestAccessEgress.free(506),
      TestAccessEgress.free(507),
      TestAccessEgress.free(508),
      TestAccessEgress.free(509),
      TestAccessEgress.free(510)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.free(155750),
      TestAccessEgress.free(155751),
      TestAccessEgress.free(155752),
      TestAccessEgress.free(155753),
      TestAccessEgress.free(155754),
      TestAccessEgress.free(155755),
      TestAccessEgress.free(155756),
      TestAccessEgress.free(155757),
      TestAccessEgress.free(155758),
      TestAccessEgress.free(155759)
    );

    PrecomputedGridTransitDataProvider data = new PrecomputedGridTransitDataProvider(500);
    var response = findTransitRoutes(
      access, egress,
      hm2time(0, 0), hm2time(24, 0),
      5, data
    );

    assertFalse(response.noConnectionFound());

    for (RaptorPath<TestTripSchedule> path : response.paths()) {
      path.legStream().forEach(System.err::println);
      System.out.println(path.toString(data.stopNameResolver()));
    }

  }

  @Test
  void transferRequiredForwardOnly() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.free(22)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.free(88)
    );

    PrecomputedGridTransitDataProvider data = new PrecomputedGridTransitDataProvider();
    var response = findTransitRoutes(
      access, egress,
      hm2time(12, 0), hm2time(14, 0),
      3, data
    );

    assertFalse(response.noConnectionFound());
//    assertEquals(1, response.paths().size());

    for (RaptorPath<TestTripSchedule> path : response.paths()) {
      path.legStream().forEach(System.err::println);
      System.out.println(path.toString(data.stopNameResolver()));
    }

//    System.out.println(PathUtils.pathsToString(response));
  }

  @ParameterizedTest
  @CsvSource({"10", "20", "100", "200", "500", "650", "800", "1000"})
  void transferRequiredForwardOnlyScaled(int size, TestReporter reporter) {

    var start = 2 * size + 2;
    var end = size * (size - 2) + size - 2;
    int travelTimeMax = size * 2 / 60 + 2;

    reporter.publishEntry("start", start + "");
    reporter.publishEntry("end", end + "");
    reporter.publishEntry("travel time max", travelTimeMax + "");

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.free(start)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.free(end)
    );

    var data = new PrecomputedGridTransitDataProvider(size);
    var response = findTransitRoutes(
      access, egress,
      hm2time(0, 0), hm2time(travelTimeMax, 0),
      3, data
    );

    assertFalse(response.noConnectionFound());
//    assertEquals(1, response.paths().size());

    for (RaptorPath<TestTripSchedule> path : response.paths()) {
      System.out.println("#####");
      path.legStream().forEach(System.err::println);
      System.out.println(path.toString(data.stopNameResolver()));
    }

//    System.out.println(PathUtils.pathsToString(response));
  }

  @Test
  void transferRequiredBackwardsOnly() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.free(77)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.free(1)
    );

    PrecomputedGridTransitDataProvider data = new PrecomputedGridTransitDataProvider();
    var response = findTransitRoutes(
      access, egress,
      hm2time(12, 0), hm2time(14, 0),
      3, data
    );

    assertFalse(response.noConnectionFound());
//    assertEquals(1, response.paths().size());

    for (RaptorPath<TestTripSchedule> path : response.paths()) {
      path.legStream().forEach(System.err::println);
      System.out.println(path.toString(data.stopNameResolver()));
    }

//    System.out.println(PathUtils.pathsToString(response));
  }

  private static RaptorResponse<TestTripSchedule> findTransitRoutes(
    List<TestAccessEgress> accesses,
    List<TestAccessEgress> egresses,
    int edt, int lat, int maxNumberOfTransfers,
    PrecomputedGridTransitDataProvider data
  ) {
    RaptorRequestBuilder<TestTripSchedule> requestBuilder = new RaptorRequestBuilder<>();

    RaptorService<TestTripSchedule> raptorService = new RaptorService<>(
      RaptorConfig.defaultConfigForTest()
    );

    long before = System.currentTimeMillis();
    requestBuilder
      .searchParams()
      .addAccessPaths(accesses.toArray(new TestAccessEgress[0]))
      .addEgressPaths(egresses.toArray(new TestAccessEgress[0]))
      .maxNumberOfTransfers(maxNumberOfTransfers)
      .timetable(true);

    requestBuilder.profile(STANDARD);
    requestBuilder
      .searchParams()
      .earliestDepartureTime(edt)
      .latestArrivalTime(lat)
//      .searchWindow(Duration.ofMinutes(60));
      .searchOneIterationOnly();

    var request = requestBuilder.build();
    var response = raptorService.route(request, data);
    long after = System.currentTimeMillis();
    System.out.println("Query time: " + (after - before) + " msecs");

    return response;
  }
}
