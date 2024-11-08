package org.opentripplanner.raptor.heigit_experiments;

import static org.opentripplanner.raptor.api.request.RaptorProfile.STANDARD;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.opentripplanner.raptor.RaptorService;
import org.opentripplanner.raptor._data.transit.TestAccessEgress;
import org.opentripplanner.raptor.api.model.RaptorAccessEgress;
import org.opentripplanner.raptor.api.path.RaptorPath;
import org.opentripplanner.raptor.api.request.RaptorRequest;
import org.opentripplanner.raptor.api.request.RaptorRequestBuilder;
import org.opentripplanner.raptor.api.request.RaptorTuningParameters;
import org.opentripplanner.raptor.api.response.RaptorResponse;
import org.opentripplanner.raptor.configure.RaptorConfig;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TransitLayer;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TransitTuningParameters;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TripSchedule;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.mappers.TransitLayerMapper;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.request.RaptorRoutingRequestTransitData;
import org.opentripplanner.routing.api.request.RouteRequest;
import org.opentripplanner.standalone.config.RouterConfig;
import org.opentripplanner.standalone.config.routerconfig.RaptorEnvironmentFactory;
import org.opentripplanner.transit.model.network.grouppriority.TransitGroupPriorityService;
import org.opentripplanner.transit.model.site.StopLocation;
import org.opentripplanner.transit.service.TimetableRepository;

class QueryRaptorWithTimetable {

  static RaptorResponse<TripSchedule> queryRaptor(Predicate<String> accessStopsFilter, Predicate<String> egressStopsFilter, int edt, int lat, Duration searchWindow, LocalDate date, TimetableRepository timetable) {
    var raptorService = new RaptorService<>(new RaptorConfig<TripSchedule>(
      new RaptorTuningParameters() {
      }, RaptorEnvironmentFactory.create(1))
    );

    TransitTuningParameters tuningParameters = RouterConfig.DEFAULT.transitTuningConfig();
    var transitLayer = TransitLayerMapper.map(tuningParameters, timetable);

    int stopCount = transitLayer.getStopCount();
    System.out.println("# of stops = " + stopCount);

    List<RaptorAccessEgress> allAccessPoints = new ArrayList<>();
    List<RaptorAccessEgress> allEgressPoints = new ArrayList<>();
    for (int i = 0; i < stopCount; i++) {
      StopLocation stopByIndex = transitLayer.getStopByIndex(i);
      if (accessStopsFilter.test(stopByIndex.getName().toString())) {
        allAccessPoints.add(TestAccessEgress.walk(i, 60));
      }
      if (egressStopsFilter.test(stopByIndex.getName().toString())) {
        allEgressPoints.add(TestAccessEgress.walk(i, 60));
      }
    }

    System.out.println("Access Points = " + collectStopsDescriptions(allAccessPoints, transitLayer));
    System.out.println("Egress Points = " + collectStopsDescriptions(allEgressPoints, transitLayer));

    RaptorRequestBuilder<TripSchedule> requestBuilder = new RaptorRequestBuilder<>();
    RaptorRequest<TripSchedule> raptorRequest = requestBuilder
      .profile(STANDARD)
      .searchParams()
      .earliestDepartureTime(edt)
      .latestArrivalTime(lat)
      .searchWindow(searchWindow)
      //.searchOneIterationOnly()
      .addAccessPaths(allAccessPoints)
      .addEgressPaths(allEgressPoints)
      .maxNumberOfTransfers(5)
      .timetable(true)
      .build();

    RaptorRoutingRequestTransitData raptorDataProvider = createRequestTransitDataProvider(transitLayer, date);

    var response = raptorService.route(raptorRequest, raptorDataProvider);

    System.out.println();
    for (RaptorPath<TripSchedule> path : response.paths()) {
      System.out.println("Found path = " + path.toString(raptorDataProvider.stopNameResolver()));
    }
    System.out.println();

    return response;
  }

  static String collectStopsDescriptions(List<RaptorAccessEgress> allAccessPoints, TransitLayer transitLayer) {
    return allAccessPoints
      .stream()
      .map(l -> transitLayer.getStopByIndex(l.stop()))
      .map(s -> "%s (%s)".formatted(s.getName().toString(), s.getId()))
      .collect(Collectors.joining(", "));
  }

  static RaptorRoutingRequestTransitData createRequestTransitDataProvider(TransitLayer transitLayer, LocalDate searchDate) {
    RouteRequest routeRequest = new RouteRequest().withPreferences(
      builder -> {
        builder.withTransfer(b -> b.withSlack(Duration.ofSeconds(60)));
        builder.withTransit(b -> {
          b.withDefaultBoardSlackSec(0);
          b.withDefaultAlightSlackSec(0);
        });
      }
    );

    return new RaptorRoutingRequestTransitData(
      transitLayer,
      TransitGroupPriorityService.empty(),
      ZonedDateTime.of(searchDate, LocalTime.MIDNIGHT, ZoneId.of("UTC")),
      // ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS),
      0,
      0,
      new AcceptionTransitDataProviderFilter(),
      routeRequest
    );
  }


}
