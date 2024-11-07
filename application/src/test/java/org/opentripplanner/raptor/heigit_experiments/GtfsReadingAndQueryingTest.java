package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.opentripplanner.framework.time.TimeUtils.hm2time;
import static org.opentripplanner.raptor.api.request.RaptorProfile.STANDARD;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.BitSet;
import org.junit.jupiter.api.Test;
import org.opentripplanner.GtfsTest;
import org.opentripplanner.raptor.RaptorService;
import org.opentripplanner.raptor._data.transit.TestAccessEgress;
import org.opentripplanner.raptor.api.path.RaptorPath;
import org.opentripplanner.raptor.api.request.RaptorEnvironment;
import org.opentripplanner.raptor.api.request.RaptorRequest;
import org.opentripplanner.raptor.api.request.RaptorRequestBuilder;
import org.opentripplanner.raptor.api.request.RaptorTuningParameters;
import org.opentripplanner.raptor.configure.RaptorConfig;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TransitLayer;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TransitTuningParameters;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TripPatternForDate;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TripSchedule;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.mappers.TransitLayerMapper;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.request.BoardAlight;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.request.RaptorRoutingRequestTransitData;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.request.TransitDataProviderFilter;
import org.opentripplanner.routing.api.request.RouteRequest;
import org.opentripplanner.standalone.config.RouterConfig;
import org.opentripplanner.transit.model.network.RoutingTripPattern;
import org.opentripplanner.transit.model.network.grouppriority.TransitGroupPriorityService;
import org.opentripplanner.transit.model.timetable.TripTimes;

class GtfsReadingAndQueryingTest extends GtfsTest {


  @Override
  public String getFeedName() {
    return "gtfs/heigit_simple";
  }

  @Test
  void fillTimetableRepositoryFromGtfsData() {

    // This is filling a timetableRepository which uses the OTP network model types to represent the timetable
    // Under the hood the OneBusAway GTFS modules (are used: )https://github.com/OneBusAway/onebusaway-gtfs-modules)
    // are used to read the gtfs data

    System.out.println();
    System.out.println("###################");
    System.out.println("timetableRepository.getFeedIds() = " + timetableRepository.getFeedIds());
    System.out.println("timetableRepository.getAllTripPatterns() = " + timetableRepository.getAllTripPatterns());
  }

  @Test
  void executeRoutingRequestFromGtfsData() {

    var raptorService = new RaptorService<>(new RaptorConfig<TripSchedule>(
      new RaptorTuningParameters() {
      }, new RaptorEnvironment() {
    }
    ));

    TransitTuningParameters tuningParameters = RouterConfig.DEFAULT.transitTuningConfig();
    var transitLayer = TransitLayerMapper.map(tuningParameters, timetableRepository);

    System.out.println("# of stops = " + transitLayer.getStopCount());
    int startingStop = 1;
    int endStop = 3;
    System.out.println("Access stop = " + transitLayer.getStopByIndex(startingStop));
    System.out.println("Egress stop = " + transitLayer.getStopByIndex(endStop));

    RaptorRequestBuilder<TripSchedule> requestBuilder = new RaptorRequestBuilder<>();
    int edt = hm2time(0, 1);
    int lat = hm2time(0, 30);
    RaptorRequest<TripSchedule> raptorRequest = requestBuilder
      .profile(STANDARD)
      .searchParams()
      .earliestDepartureTime(edt)
      .latestArrivalTime(lat)
      .searchOneIterationOnly()
//      .addAccessPaths(TestAccessEgress.free(startingStop))
//      .addEgressPaths(TestAccessEgress.free(endStop))
      .addAccessPaths(TestAccessEgress.walk(startingStop, 30))
      .addEgressPaths(TestAccessEgress.walk(endStop, 20))
      .maxNumberOfTransfers(3)
      .timetable(true)
      .build();

    RouteRequest routeRequest = new RouteRequest().withPreferences(
      builder -> {
        builder.withTransfer(b -> b.withSlack(Duration.ofSeconds(60)));
        builder.withTransit(b -> {
          b.withDefaultBoardSlackSec(0);
          b.withDefaultAlightSlackSec(0);
        });
      }
    );
    //routeRequest.setFrom(GenericLocation.fromStopId("B", "gtfs/heigit_simple", "B"));
    //routeRequest.setTo(GenericLocation.fromStopId("D", "gtfs/heigit_simple", "D"));

    RaptorRoutingRequestTransitData raptorDataProvider = createRequestTransitDataProvider(transitLayer, routeRequest);

    System.out.println("transfer slack = " + raptorDataProvider.slackProvider().transferSlack());

    //System.out.println("Stop START = " + raptorDataProvider.stopNameResolver().apply(startingStop));
    //System.out.println("Stop END   = " + raptorDataProvider.stopNameResolver().apply(endStop));

    //RaptorRoute<TripSchedule> route0 = raptorDataProvider.getRouteForIndex(0);
    //System.out.println("Route 0 = " + route0);
    //RaptorRoute<TripSchedule> route1 = raptorDataProvider.getRouteForIndex(1);
    //System.out.println("Route 1 = " + route1);

    //System.out.println(route0.timetable());

    var response = raptorService.route(raptorRequest, raptorDataProvider);

    for (RaptorPath<TripSchedule> path : response.paths()) {
      System.out.println("path = " + path);
    }
    
    assertEquals(2, response.paths().size());
  }

  private RaptorRoutingRequestTransitData createRequestTransitDataProvider(TransitLayer transitLayer, RouteRequest routeRequest) {
    return new RaptorRoutingRequestTransitData(
      transitLayer,
      TransitGroupPriorityService.empty(),
      ZonedDateTime.of(2024, 10, 7, 0, 0, 0, 0, ZoneId.of("UTC")),
      0,
      0,
      new TestTransitDataProviderFilter(),
      routeRequest
    );
  }

  private static class TestTransitDataProviderFilter implements TransitDataProviderFilter {

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
}
