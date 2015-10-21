#!/bin/bash
cd ../zkconfter/
mvn -DaltDeploymentRepository=snapshot-repo::default::file:../mvn-repository clean deploy
