#!/bin/sh
# Install dependencies that are not publicly available into local Maven repository.

mvn install:install-file -Dfile=comm.jar -DpomFile=comm.pom
