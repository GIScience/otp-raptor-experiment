package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.opentripplanner.framework.time.TimeUtils.hm2time;
import static org.opentripplanner.raptor.api.request.RaptorProfile.STANDARD;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentripplanner.GtfsTest;
import org.opentripplanner.raptor.RaptorService;
import org.opentripplanner.raptor._data.transit.TestAccessEgress;
import org.opentripplanner.raptor.api.model.RaptorAccessEgress;
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
import org.opentripplanner.transit.model.site.StopLocation;
import org.opentripplanner.transit.model.timetable.TripTimes;

class DeFernverkehrTest extends GtfsTest {


  @Override
  public String getFeedName() {
    return "gtfs/de_fernverkehr";
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

    int stopCount = transitLayer.getStopCount();
    System.out.println("# of stops = " + stopCount);

    List<RaptorAccessEgress> allAccessPoints = new ArrayList<>();
    List<RaptorAccessEgress> allEgressPoints = new ArrayList<>();
    for (int i = 0; i < stopCount; i++) {
      StopLocation stopByIndex = transitLayer.getStopByIndex(i);
      if (stopByIndex.getName().toString().contains("Heidelberg Hbf")) {
        allAccessPoints.add(TestAccessEgress.walk(i, 60));
      }
      if (stopByIndex.getName().toString().contains("Berlin Hbf")) {
        allEgressPoints.add(TestAccessEgress.walk(i, 60));
      }
    }

    allAccessPoints.forEach(l -> System.out.println("Access: " + transitLayer.getStopByIndex(l.stop())));
    allEgressPoints.forEach(l -> System.out.println("Egress: " + transitLayer.getStopByIndex(l.stop())));

    RaptorRequestBuilder<TripSchedule> requestBuilder = new RaptorRequestBuilder<>();
    int edt = hm2time(8, 0);
    int lat = hm2time(16, 0);
    RaptorRequest<TripSchedule> raptorRequest = requestBuilder
      .profile(STANDARD)
      .searchParams()
      .earliestDepartureTime(edt)
      .latestArrivalTime(lat)
      .searchWindow(Duration.ofHours(2))
      //.searchOneIterationOnly()
      .addAccessPaths(allAccessPoints)
      .addEgressPaths(allEgressPoints)
      .maxNumberOfTransfers(5)
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
    RaptorRoutingRequestTransitData raptorDataProvider = createRequestTransitDataProvider(transitLayer, routeRequest);

    var response = raptorService.route(raptorRequest, raptorDataProvider);

    for (RaptorPath<TripSchedule> path : response.paths()) {
      System.out.println("path = " + path);
    }

    assertFalse(response.noConnectionFound());
  }

  private RaptorRoutingRequestTransitData createRequestTransitDataProvider(TransitLayer transitLayer, RouteRequest routeRequest) {
    return new RaptorRoutingRequestTransitData(
      transitLayer,
      TransitGroupPriorityService.empty(),
      ZonedDateTime.of(2024, 11, 7, 0, 0, 0, 0, ZoneId.of("UTC")),
      // ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS),
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
