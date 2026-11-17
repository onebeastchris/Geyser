/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */


package org.geysermc.geyser.registry.populator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.paletter.vanilla.BedrockBlockState;
import org.geysermc.paletter.vanilla.BedrockBlocks;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The reader of blocks.json: the format (FORMAT.md in the mappings repository) on inline tables, and the bundled file
 * on a few well known blocks.
 */
class JavaBlockMappingTableTest {

    private static JsonObject json(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static JsonObject resource(String name) throws IOException {
        try (InputStream stream = JavaBlockMappingTableTest.class.getClassLoader().getResourceAsStream("mappings/" + name)) {
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static final JavaBlockMappingTable TABLE = JavaBlockMappingTable.of(json("""
        {"format": 1,
         "tables": {"$1": {"by": ["half"], "values": {"top": true, "bottom": false}}},
         "blocks": {
           "cobweb": {"name": "web"},
           "furnace": {"name": {"by": ["lit"], "values": {"false": "furnace", "true": "lit_furnace"}},
                       "states": {"minecraft:cardinal_direction": {"by": ["facing"], "values": {"north": "north", "south": "south", "west": "west", "east": "east"}}}},
           "oak_stairs": {"states": {"upside_down_bit": "$1", "constant": 7}}
         }}
        """));

    private static JavaBlockMappingTable.RawState state(String name, Map<String, ?> states) {
        return new JavaBlockMappingTable.RawState(name, new HashMap<String, Object>(states));
    }

    @Test
    void absentBlocksKeepTheirNameWithoutStates() {
        assertEquals(state("minecraft:stone", Map.of()), TABLE.rawState(Blocks.STONE.defaultBlockState()));
    }

    @Test
    void constantName() {
        assertEquals(state("minecraft:web", Map.of()), TABLE.rawState(Blocks.COBWEB.defaultBlockState()));
    }

    @Test
    void namesAndStatesByProperty() {
        var litSouth = Blocks.FURNACE.defaultBlockState().withValue(Properties.LIT, true).withValue(Properties.HORIZONTAL_FACING, Direction.SOUTH);
        assertEquals(state("minecraft:lit_furnace", Map.of("minecraft:cardinal_direction", "south")), TABLE.rawState(litSouth));
        var unlit = litSouth.withValue(Properties.LIT, false);
        assertEquals(state("minecraft:furnace", Map.of("minecraft:cardinal_direction", "south")), TABLE.rawState(unlit));
    }

    @Test
    void constantStatesReferencesAndTypedValues() {
        var top = Blocks.OAK_STAIRS.defaultBlockState().withValue(Properties.HALF, "top");
        assertEquals(state("minecraft:oak_stairs", Map.of("upside_down_bit", (byte) 1, "constant", 7)), TABLE.rawState(top));
    }

    @Test
    void realMappingsGiveKeys() throws IOException {
        JavaBlockMappingTable real = JavaBlockMappingTable.of(resource("blocks.json"));
        assertEquals(BedrockBlocks.STONE.defaultState(), real.bedrockState(Blocks.STONE.defaultBlockState()));

        var litSouth = Blocks.FURNACE.defaultBlockState().withValue(Properties.LIT, true).withValue(Properties.HORIZONTAL_FACING, Direction.SOUTH);
        BedrockBlockState furnace = real.bedrockState(litSouth);
        assertEquals("minecraft:lit_furnace", furnace.type().identifier());
        assertEquals("south", furnace.toNbt().getCompound("states").getString("minecraft:cardinal_direction"));

        assertEquals("minecraft:web", real.bedrockState(Blocks.COBWEB.defaultBlockState()).type().identifier());
    }

    @Test
    void otherFormatsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> JavaBlockMappingTable.of(json("{\"format\": 2, \"blocks\": {}}")));
        assertThrows(IllegalArgumentException.class, () -> JavaBlockMappingTable.of(json("{\"blocks\": {}}")));
    }
}
