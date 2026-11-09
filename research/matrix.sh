#!/bin/bash
# variant: name binary minrto ibit
out=$1
variants=("stock:harness-stock:400:0" "stock-rto1000:harness-stock:1000:0" "stock-ibit:harness-stock:400:2" "patched:harness-patched:400:0" "patched-ibit:harness-patched:400:2")
profiles=("game:3000:0" "heavy:15000:0" "join:1500:4000000")
jobs=()
for rtt in 30 70 155 318; do for p in "${profiles[@]}"; do for v in "${variants[@]}"; do
  IFS=: read pn tick burst <<< "$p"; IFS=: read vn bin rto ib <<< "$v"
  jobs+=("$vn $pn $bin $rtt $rto $ib $tick $burst")
done; done; done
printf '%s\n' "${jobs[@]}" | xargs -P 6 -L 1 bash -c 'echo "$0 $1 | $(timeout -s KILL 60 ./$2 $3 $4 $5 $6 $7 25)"' > $out
