#!/usr/bin/env bash

mvn -f pom.xml clean package -U

cp /target/scala-basic-blockchain.jar basic-blockchain.jar
