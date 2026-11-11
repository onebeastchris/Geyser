# BDS 1.26.63.1 piston packet measurements

These are the packets Mojang's Bedrock Dedicated Server sent to a NetherNet client for console-driven piston contraptions, with no network impairment.

- `full-run2.tsv`: every decoded packet with its contents, tick index and timestamps. The `MARK <test> power|unpower <n>` lines are chat markers.
- `summary-run2.txt`: counts per test and per cycle. These include the redstone block's own 2 UpdateBlocks per cycle, so subtract those.
- `sequence-*.txt`: verbatim packet sequences for one cycle of each test.

Tests:
- (a) piston pushing 1 stone
- (b) sticky piston, push and pull
- (c) 12 stone in a line
- (d) 50 pistons firing together
- (e1/e2) moss or melon, destroyed rather than pushed

Key results:
- PistonArm NBT is sent once per state change (State 1→2 on extend, 3→0 on retract, 2 ticks apart), with Progress only ever 0 or 1. It is never resent per tick.
- No BlockEventPacket was sent.
- No layer-1 UpdateBlock was sent. Layer 1 only appears as an `extraBlocks` air entry in the settling UpdateSubChunkBlocks.
- Block changes are batched per sub-chunk in UpdateSubChunkBlocks: flags 3 while moving, flags 19 when settling.
