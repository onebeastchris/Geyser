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

package org.geysermc.geyser.session.cache;

import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundCloseFormPacket;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.form.impl.FormDefinitions;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.PluginMessageUtils;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class FormCache {
    /**
     * The magnitude of this doesn't actually matter, but it must be negative so that
     * BedrockNetworkStackLatencyTranslator can detect the hack.
     */
    private static final long MAGIC_FORM_IMAGE_HACK_TIMESTAMP = -1234567890L;

    private final FormDefinitions formDefinitions = FormDefinitions.instance();
    private final GeyserSession session;
    private boolean closing;
    private Form currentForm;
    private Form queuedForm;

    public boolean hasFormOpen() {
        // If forms is empty it implies that there are no forms to show
        // so technically this returns "has forms to show" or "has open"
        // Forms are only queued in specific circumstances, such as waiting on
        // previous inventories to close
        return (currentForm != null && !closing) && queuedForm != null;
    }

    public void showForm(Form form) {
        if (session.getUpstream().isInitialized() && !session.isClosingInventory()) {
            this.currentForm = form;
            String jsonData = formDefinitions.codecFor(form).jsonData(form);

            ModalFormRequestPacket formRequestPacket = new ModalFormRequestPacket();
            formRequestPacket.setFormId(1);
            formRequestPacket.setFormData(jsonData);
            session.sendUpstreamPacket(formRequestPacket);
            PluginMessageUtils.sendFormOpenMessage(session, true);

            // Hack to fix the (url) image loading bug
            if (form instanceof SimpleForm) {
                NetworkStackLatencyPacket latencyPacket = new NetworkStackLatencyPacket();
                latencyPacket.setFromServer(true);
                latencyPacket.setTimestamp(MAGIC_FORM_IMAGE_HACK_TIMESTAMP);
                session.scheduleInEventLoop(
                    () -> session.sendUpstreamPacket(latencyPacket),
                    500, TimeUnit.MILLISECONDS
                );
            }
        } else {
            // Must queue!
            this.queuedForm = form;
        }
    }

    public int sendServerSettings(Form form) {
        // Just going to assume that there won't be any open / queued form!
        this.currentForm = form;
        return 1;
    }

    public void resendOrSendQueued() {
        if (currentForm != null) {
            showForm(currentForm);
        } else if (queuedForm != null) {
            showForm(queuedForm);
            queuedForm = null;
        }
    }

    public void handleResponse(ModalFormResponsePacket response) {
        PluginMessageUtils.sendFormOpenMessage(session, false);
        if (currentForm == null) {
            return;
        }

        this.closing = false;
        try {
            formDefinitions.definitionFor(currentForm)
                    .handleFormResponse(currentForm, response.getFormData());
        } catch (Exception e) {
            GeyserImpl.getInstance().getLogger().error("Error while processing form response!", e);
        }

        this.currentForm = null;
        if (queuedForm != null) {
            showForm(queuedForm);
            this.queuedForm = null;
        }
    }

    public void closeForms() {
        if (currentForm != null && !closing) {
            // We will need to wait for client response...?
            closing = true;
            session.sendUpstreamPacket(new ClientboundCloseFormPacket());
        }

        if (queuedForm != null) {
            try {
                formDefinitions.definitionFor(queuedForm)
                    .handleFormResponse(queuedForm, null);
            } catch (Exception e) {
                GeyserImpl.getInstance().getLogger().error("Error while closing form!", e);
            }
            queuedForm = null;
        }
    }
}
