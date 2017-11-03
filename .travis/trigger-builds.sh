#!/bin/bash
mkdir repos
cd repos
for i in $(echo ${CN1_TRIGGER_REPOS} | tr " " "\n")
do
  rm -rf repo
  # process
  git clone https://github.com/${i} repo
  cd repo
  travis restart
  cd ..
  rm -rf repo

done
