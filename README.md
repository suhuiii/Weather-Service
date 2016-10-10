# ZIP to Weather Report Service
This is a Java command line implementation of a weather reporting service based on data from OpenWeatherMap

The program reads a user defined text file (or uses the default zipcodes.txt) for a list of (US) ZIP codes and provides the following information for each ZIP code:
- Name of City
- Two letter State Name
- Maximum Temperature in Fahrenheit
- Minimum Temperature in Fahrenheit
- Current Weather Conditions

It also lists the hottest city(s) as well as the coolest city(s) from the ZIP codes provided.

The ZIP to city name and state service is provided by the Google Maps API.
The weather data for each ZIP code is provided by the OpenWeatherMap API.

## Motivation

This was developed for a software design class to demonstrate the use of mocks.

## Executing the Build
You can execute the build using one of the following commands from the root of the project:

- ./gradlew <task> (on Unix-like platforms such as Linux and Mac OS X)
- gradlew <task> (on Windows using the gradlew.bat batch file)

# Format of input file
The file used to read the ZIP codes should be a ".txt" file, with each ZIP code deliminated by a new line.
While there is technically no limit to the number of ZIP codes that can be put in the file, the maximum number of ZIPs that can be looked up is constrained by the 60 calls/min cap for the free API usage on OpenWeatherMap. 



