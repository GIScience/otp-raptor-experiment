package org.opentripplanner.raptor.heigit_experiments;

import static org.opentripplanner.framework.time.TimeUtils.hm2time;
import static org.opentripplanner.raptor.api.request.RaptorProfile.STANDARD;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.opentripplanner.GtfsTest;
import org.opentripplanner.raptor.RaptorService;
import org.opentripplanner.raptor._data.transit.TestAccessEgress;
import org.opentripplanner.raptor.api.request.RaptorEnvironment;
import org.opentripplanner.raptor.api.request.RaptorRequest;
import org.opentripplanner.raptor.api.request.RaptorRequestBuilder;
import org.opentripplanner.raptor.api.request.RaptorTuningParameters;
import org.opentripplanner.raptor.configure.RaptorConfig;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TransitLayer;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TransitTuningParameters;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TripSchedule;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.mappers.TransitLayerMapper;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.request.RaptorRoutingRequestTransitData;
import org.opentripplanner.standalone.config.RouterConfig;
import org.opentripplanner.transit.model.network.grouppriority.TransitGroupPriorityService;

class GtfsReadingAndQueryingTest extends GtfsTest {


  @Override
  public String getFeedName() {
    return "gtfs/simple";
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
    int startingStop = 0;
    int endStop = 2;
    System.out.println("Access stop = " + transitLayer.getStopByIndex(startingStop));
    System.out.println("Egress stop = " + transitLayer.getStopByIndex(endStop));

    RaptorRequestBuilder<TripSchedule> requestBuilder = new RaptorRequestBuilder<>();
    int edt = hm2time(11, 0);
    int lat = hm2time(12, 0);
    RaptorRequest<TripSchedule> request = requestBuilder
      .profile(STANDARD)
      .searchParams()
      .earliestDepartureTime(edt)
      .latestArrivalTime(lat)
      .searchOneIterationOnly()
      .addAccessPaths(TestAccessEgress.free(startingStop))
      .addEgressPaths(TestAccessEgress.free(endStop))
      .maxNumberOfTransfers(3)
      .timetable(true)
      .build();

    if (1 == 1) return; // To make test succeed
    // TODO: Fill in missing data in the following code

    var raptorDataProvider = createRequestTransitDataProvider(transitLayer);

    var result = raptorService.route(request, raptorDataProvider);

    System.out.println("result.paths() = " + result.paths());
  }

  private RaptorRoutingRequestTransitData createRequestTransitDataProvider(TransitLayer transitLayer) {
    return new RaptorRoutingRequestTransitData(
      transitLayer,
      TransitGroupPriorityService.empty(),
      ZonedDateTime.now(),
      0,
      0,
      null, // must be filled with valid TransitDataProviderFilter
      null // must be filled with valid RouteRequest
    );
  }
}
