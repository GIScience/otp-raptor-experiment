package org.opentripplanner.astar;

import static org.opentripplanner.OtpArchitectureModules.GOOGLE_COLLECTIONS;
import static org.opentripplanner.OtpArchitectureModules.OTP_ROOT;
import static org.opentripplanner.OtpArchitectureModules.UTILS;

import org.junit.jupiter.api.Test;
import org.opentripplanner._support.arch.Package;

public class AStarArchitectureTest {

  private static final Package ASTAR = OTP_ROOT.subPackage("astar");

  private static final Package ASTAR_MODEL = ASTAR.subPackage("model");

  private static final Package ASTAR_SPI = ASTAR.subPackage("spi");

  private static final Package ASTAR_STRATEGY = ASTAR.subPackage("strategy");

  @Test
  void enforcePackageDependencies() {
    ASTAR.dependsOn(UTILS, GOOGLE_COLLECTIONS, ASTAR_MODEL, ASTAR_SPI).verify();
  }

  @Test
  void enforcePackageDependenciesInModel() {
    ASTAR_MODEL.dependsOn().verify();
  }

  @Test
  void enforcePackageDependenciesInSPI() {
    ASTAR_SPI.dependsOn().verify();
  }

  @Test
  void enforcePackageDependenciesInStrategy() {
    ASTAR_STRATEGY.dependsOn(UTILS, ASTAR, ASTAR_SPI).verify();
  }
}
