#!/bin/bash
jobs=()
for loss in 0.01 0.03; do for rtt in 70 155; do for p in "game:3000" "heavy:15000"; do for v in "stock:harness-stock:400" "stock-rto1000:harness-stock:1000" "patched:harness-patched:400"; do
  IFS=: read pn tick <<< "$p"; IFS=: read vn bin rto <<< "$v"
  jobs+=("$vn $pn $loss $bin $rtt $rto $tick")
done; done; done; done
printf '%s\n' "${jobs[@]}" | xargs -P 6 -L 1 bash -c 'echo "$0 $1 loss=$2 | $(LOSS=$2 timeout -s KILL 60 ./$3 $4 $5 0 $6 0 25)"' > $1
