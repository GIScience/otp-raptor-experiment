package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.opentripplanner.framework.time.TimeUtils.hm2time;
import static org.opentripplanner.raptor.api.request.RaptorProfile.STANDARD;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentripplanner.raptor.RaptorService;
import org.opentripplanner.raptor._data.RaptorTestConstants;
import org.opentripplanner.raptor._data.api.PathUtils;
import org.opentripplanner.raptor._data.transit.TestAccessEgress;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.api.path.RaptorPath;
import org.opentripplanner.raptor.api.request.RaptorRequestBuilder;
import org.opentripplanner.raptor.api.response.RaptorResponse;
import org.opentripplanner.raptor.configure.RaptorConfig;

public class RoutingWithSynthDataTest implements RaptorTestConstants {


  @Test
  void horizontalRouteWithoutTransferButAccessEgressWalks() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.walk(20, 60)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.walk(29, 60)
    );

    var response = findTransitRoutes(access, egress, hm2time(0, 50), hm2time(1, 30), 3, new SynthGridTransitDataProvider());

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
      3, new SynthGridTransitDataProvider()
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

    SynthGridTransitDataProvider data = new SynthGridTransitDataProvider();
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
  void transferRequiredForwardOnly() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.free(22)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.free(88)
    );

    SynthGridTransitDataProvider data = new SynthGridTransitDataProvider();
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
  void transferRequiredBackwardsOnly() {

    List<TestAccessEgress> access = List.of(
      TestAccessEgress.free(77)
    );

    List<TestAccessEgress> egress = List.of(
      TestAccessEgress.free(1)
    );

    SynthGridTransitDataProvider data = new SynthGridTransitDataProvider();
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
    SynthGridTransitDataProvider data
  ) {
    RaptorRequestBuilder<TestTripSchedule> requestBuilder = new RaptorRequestBuilder<>();

    RaptorService<TestTripSchedule> raptorService = new RaptorService<>(
      RaptorConfig.defaultConfigForTest()
    );

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
      .searchOneIterationOnly();

    var request = requestBuilder.build();
    var response = raptorService.route(request, data);
    return response;
  }
}
