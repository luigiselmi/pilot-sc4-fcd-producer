#!/bin/bash
bak= $pwd
cd data/db
rm -fr dumps
rm -fr virtuoso*
rm -fr *.log
rm -f clean-logs.sh
cd "$bak"
