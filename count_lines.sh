#!/bin/bash

#Count the number of lines of java code
find ./ -name "*.java" -print0 | xargs -0 cat | wc -l