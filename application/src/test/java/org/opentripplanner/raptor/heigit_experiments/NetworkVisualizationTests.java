package org.opentripplanner.raptor.heigit_experiments;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ArrayUtils.toObject;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.opentripplanner.raptor._data.transit.TestTripPattern;
import org.opentripplanner.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.raptor.spi.RaptorRoute;

class NetworkVisualizationTests {

  SynthGridTransitDataProvider dataProvider = new SynthGridTransitDataProvider();


  @Test
  void printAllRoutes() {
    this.dataProvider = new SynthGridTransitDataProvider(10, Duration.ofMinutes(1));

    System.out.println("digraph G {");

    IntStream
      .range(0, 19)
      .mapToObj( routeIndex -> {

        RaptorRoute<TestTripSchedule> route = this.dataProvider.getRouteForIndex(routeIndex);

        TestTripPattern pattern = (TestTripPattern) route.pattern();
        List<Integer> stops = asList(toObject(pattern.stopIndexes));

        String result = stops.stream()
          .map(s -> "" + s)
          .collect(Collectors.joining(" -> "));

        return result + ";";
      }

    ).forEach(System.out::println);

    System.out.println("}");


  }


}
