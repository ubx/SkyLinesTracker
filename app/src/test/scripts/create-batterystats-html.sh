#!/usr/bin/env bash

QUEUE_FIXES=${1:-true}

alias python=/usr/bin/python2.7
python /home/andreas/work/src/github.com/google/battery-historian/scripts/historian.py batterystats-${QUEUE_FIXES}.txt > batterystats-${QUEUE_FIXES}.html


