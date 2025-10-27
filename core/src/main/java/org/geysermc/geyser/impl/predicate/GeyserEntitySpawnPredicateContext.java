/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.impl.predicate;

import org.geysermc.geyser.api.entity.JavaEntityType;
import org.geysermc.geyser.api.predicate.context.entity.EntitySpawnPredicateContext;
import org.geysermc.geyser.entity.GeyserEntityType;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.GenericObjectData;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAddEntityPacket;

import java.util.UUID;

public class GeyserEntitySpawnPredicateContext extends GeyserMinecraftPredicateContext implements EntitySpawnPredicateContext {
    private final JavaEntityType type;
    private final int entityId;
    private final UUID entityUuid;
    private final int data;

    public GeyserEntitySpawnPredicateContext(GeyserSession session, JavaEntityType entityType, ClientboundAddEntityPacket packet) {
        super(session);
        type = entityType;
        entityId = packet.getEntityId();
        entityUuid = packet.getUuid();
        // TODO data other than generic
        data = packet.getData() instanceof GenericObjectData generic ? generic.getValue() : 0;
    }

    @Override
    public JavaEntityType entityType() {
        return type;
    }

    @Override
    public int entityId() {
        return entityId;
    }

    @Override
    public UUID entityUuid() {
        return entityUuid;
    }

    @Override
    public int data() {
        return data;
    }
}
