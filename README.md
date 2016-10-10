# ZIPCode to Weather Service
This is a Java commandline implementation of a (US) zipcode to Weather Report look up service.

This program prompts for a text file that contains the zipcodes of interest, looks up the information and lists the name of the city, state, Minimum and Maximum Temperature in Fahrenheit as well as the current weather conditions.

The APIs used to lookup the zipcode and weather are Google Maps and Weather.gov respectively.

## Motivation

This was developed for a software design class to demonstrate the use of mocking.

## Executing the Build
You can execute the build using one of the following commands from the root of the project:

- ./gradlew <task> (on Unix-like platforms such as Linux and Mac OS X)
- gradlew <task> (on Windows using the gradlew.bat batch file)


## Format of input file
The file should be a ".txt" file, with each zipcode separated by a new line. 

For instance:
12345
67890
11122

You may also view zipCodes.txt which is the default input file provided in the repository.
