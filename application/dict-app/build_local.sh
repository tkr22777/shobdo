#!/bin/bash

#Removing previous binaries
rm -rf target/universal/*

#Building the app binaries for distribution
activator dist

