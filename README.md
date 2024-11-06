## About the experiment

This is a fork of [OTP](https://github.com/opentripplanner/OpenTripPlanner). 

We want to experiment with using OTP's raptor implementation as a standalone library.

## Installation in IntelliJ

- Requires Maven >= 3.6.6
- Import the project as a Maven project or from version control (GitHub)
- Run `mvn install`
- Execute `Build Project` in IntelliJ
- Reload all Maven projects (in Maven tool window)

Now everything should compile file and tests can be run in the IDE.


### Step 1: Use RaptorService (API) with simple example

__Goal:__  Execute a few queries on the OTP Raptor API. Try to load timetable data via GTFS import.

__Steps:__

- [x] Fork the OTP GH project.
- [x] Start with a simple test case (or a few) using the existing `RaptorModuleTestCase` mechanism but our own scenario.
- [x] Possible approach: Get rid of the test support code like `RaptorModuleTestCase` so that in the end we use `RaptorService` directly. Alternative: Build `RaptorService` use up from scratch.
- [ ] Create timetable for our scenario as GTFS files.
- [ ] Import GTFS files for running the test. Use OTP’s approach or an external library.


## Original README

See the [original README](README.original.md) for more information about the OpenTripPlanner project.
