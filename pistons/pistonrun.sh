#!/bin/bash
S=/tmp/claude-0/-home-user-Geyser/6a05a88c-43de-54cc-81a7-986e404eb4a7/scratchpad/bds-batching
J=/home/user/Geyser/bootstrap/standalone/build/libs/Geyser-Standalone.jar
export JAVA_TOOL_OPTIONS=
cd $S
out=pistons/${1:-run}; rm -rf $out
before=$(grep -a -c "Player Spawned" /home/user/bds-run/bds.log)
timeout 480 java -Xmx1g -Dfull=true -cp classes:$J BdsProbe $out join 385 12 true > $out.console 2>&1 &
pid=$!
for i in $(seq 1 120); do [ "$(grep -a -c 'Player Spawned' /home/user/bds-run/bds.log)" -gt "$before" ] && break; sleep 0.5; done
c() { echo "$1" > bds.fifo; sleep ${2:-0.25}; }
mark() { echo "say MARK $1" > bds.fifo; sleep 0.15; }
c "gamemode creative @a"; c "tp @a 8 120 8" 3
c "fill 0 108 0 40 125 40 air" 1
# learn runtime ids of the blocks involved
for b in stone air 'piston ["facing_direction"=1]' 'sticky_piston ["facing_direction"=1]' redstone_block moving_block 'piston_arm_collision ["facing_direction"=1]' 'sticky_piston_arm_collision ["facing_direction"=1]' moss_block melon_block; do
  mark "id ${b%% *}"; c "setblock 5 110 5 $b" 0.6
done
c "setblock 5 110 5 air" 0.5
cycles() { # name powercmd unpowercmd count [per-cycle setup]
  for n in $(seq 1 $4); do
    if [ -n "$5" ]; then mark "$1 reset $n"; c "$5" 0.3; [ -n "$6" ] && c "$6" 0.3; sleep 0.3; fi
    mark "$1 power $n"; c "$2" 1.0
    mark "$1 unpower $n"; c "$3" 1.0
  done
}
# (a) normal piston, one stone
c 'setblock 10 110 10 piston ["facing_direction"=1]'; c "setblock 10 111 10 stone" 1
cycles a "setblock 11 110 10 redstone_block" "setblock 11 110 10 air" 20 "setblock 10 112 10 air" "setblock 10 111 10 stone"
# (b) sticky piston, one stone
c 'setblock 14 110 10 sticky_piston ["facing_direction"=1]'; c "setblock 14 111 10 stone" 1
cycles b "setblock 15 110 10 redstone_block" "setblock 15 110 10 air" 20
# (c) normal piston, column of 12 stone
c 'setblock 18 110 10 piston ["facing_direction"=1]'; c "fill 18 111 10 18 122 10 stone" 1
cycles c "setblock 19 110 10 redstone_block" "setblock 19 110 10 air" 20 "fill 18 111 10 18 124 10 air" "fill 18 111 10 18 122 10 stone"
# (c2) sticky piston, column of 12 stone (comes back down)
c 'setblock 30 110 10 sticky_piston ["facing_direction"=1]'; c "fill 30 111 10 30 122 10 stone" 1
cycles c2 "setblock 31 110 10 redstone_block" "setblock 31 110 10 air" 10
# (d) 50 pistons (10 x 5), one stone each, powered together from below
c 'fill 10 110 20 19 110 24 piston ["facing_direction"=1]'; c "fill 10 111 20 19 111 24 stone" 1
cycles d "fill 10 109 20 19 109 24 redstone_block" "fill 10 109 20 19 109 24 air" 20 "fill 10 112 20 19 112 24 air" "fill 10 111 20 19 111 24 stone"
# (e1) moss block, (e2) melon, re-placed before every cycle
c 'setblock 22 110 10 piston ["facing_direction"=1]' 0.5
cycles e1 "setblock 23 110 10 redstone_block" "setblock 23 110 10 air" 10 "setblock 22 111 10 moss_block"
c 'setblock 26 110 10 piston ["facing_direction"=1]' 0.5
cycles e2 "setblock 27 110 10 redstone_block" "setblock 27 110 10 air" 10 "setblock 26 111 10 melon_block"
mark "end"
wait $pid 2>/dev/null
echo "PISTONRUN FINISHED"
