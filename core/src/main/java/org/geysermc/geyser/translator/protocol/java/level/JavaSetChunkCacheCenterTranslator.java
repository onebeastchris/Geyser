/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.translator.protocol.java.level;

import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket;
import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.util.ChunkUtils;

@Translator(packet = ClientboundSetChunkCacheCenterPacket.class)
public class JavaSetChunkCacheCenterTranslator extends PacketTranslator<ClientboundSetChunkCacheCenterPacket> {

    @Override
    public void translate(GeyserSession session, ClientboundSetChunkCacheCenterPacket packet) {
        // The Java server sends chunks around this centre, which can differ from the player's position
        // (vehicles, spectating an entity, server-side movement). Keep the Bedrock chunk publisher on it, or the client
        // drops chunks that fall outside the publisher area. See ChunkUtils#updateChunkPosition
        session.setJavaChunkCenter(Vector2i.from(packet.getChunkX(), packet.getChunkZ()));
        ChunkUtils.updateChunkPosition(session);
    }
}
