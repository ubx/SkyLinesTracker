#!/bin/bash

export GOPATH=$HOME/work
export GOBIN=$GOPATH/bin
export PATH=$PATH:$GOBIN
cd ~/work/src/github.com/google/battery-historian
go run cmd/battery-historian/battery-historian.go
