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

package org.geysermc.geyser.command;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.GeyserLogger;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserRegisterPermissionsEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.util.PlatformType;
import org.geysermc.geyser.api.util.TriState;
import org.geysermc.geyser.command.defaults.AdvancedTooltipsCommand;
import org.geysermc.geyser.command.defaults.AdvancementsCommand;
import org.geysermc.geyser.command.defaults.ConnectionTestCommand;
import org.geysermc.geyser.command.defaults.DumpCommand;
import org.geysermc.geyser.command.defaults.ExtensionsCommand;
import org.geysermc.geyser.command.defaults.HelpCommand;
import org.geysermc.geyser.command.defaults.ListCommand;
import org.geysermc.geyser.command.defaults.OffhandCommand;
import org.geysermc.geyser.command.defaults.ReloadCommand;
import org.geysermc.geyser.command.defaults.SettingsCommand;
import org.geysermc.geyser.command.defaults.StatisticsCommand;
import org.geysermc.geyser.command.defaults.StopCommand;
import org.geysermc.geyser.command.defaults.VersionCommand;
import org.geysermc.geyser.event.GeyserEventRegistrar;
import org.geysermc.geyser.event.type.GeyserDefineCommandsEventImpl;
import org.geysermc.geyser.extension.command.GeyserExtensionCommand;
import org.geysermc.geyser.text.ChatColor;
import org.geysermc.geyser.text.GeyserLocale;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.NoSuchCommandException;
import org.incendo.cloud.execution.ExecutionCoordinator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;

public class CommandRegistry {

    private final GeyserImpl geyser;
    private final CommandManager<GeyserCommandSource> cloud;

    /**
     * Map of Geyser subcommands to their Commands
     */
    private final Map<String, Command> commands = new Object2ObjectOpenHashMap<>(13);

    /**
     * Map of Extensions to Maps of their subcommands
     */
    private final Map<Extension, Map<String, Command>> extensionCommands = new Object2ObjectOpenHashMap<>(0);

    /**
     * Map of root commands (that are for extensions) to Extensions
     */
    private final Map<String, Extension> extensionRootCommands = new Object2ObjectOpenHashMap<>(0);

    /**
     * Map containing only permissions that have been registered with a default value
     */
    private final Map<String, TriState> permissionDefaults = new Object2ObjectOpenHashMap<>(13);

    /**
     * The order and behaviour of these exception handlers is designed to mirror the typical cloud implementations.
     * For example: https://github.com/Incendo/cloud/blob/a4cc749b91564af57bb7bba36dd8011b556c2b3a/cloud-minecraft/cloud-fabric/src/main/java/cloud/commandframework/fabric/FabricExecutor.java#L94-L173
     */
    private final List<ExceptionHandler<?>> exceptionHandlers = List.of(
        new ExceptionHandler<>(InvalidSyntaxException.class, (src, e) -> src.sendLocaleString("geyser.command.invalid_syntax", e.correctSyntax())),
        new ExceptionHandler<>(InvalidCommandSenderException.class, (src, e) -> src.sendLocaleString("geyser.command.invalid_sender", e.commandSender().getClass().getSimpleName(), e.requiredSender())),
        new ExceptionHandler<>(NoPermissionException.class, this::handleNoPermission),
        new ExceptionHandler<>(NoSuchCommandException.class, (src, e) -> src.sendLocaleString("geyser.command.not_found")),
        new ExceptionHandler<>(ArgumentParseException.class, (src, e) -> src.sendLocaleString("geyser.command.invalid_argument", e.getCause().getMessage())),
        new ExceptionHandler<>(CommandExecutionException.class, (src, e) -> handleUnexpectedThrowable(src, e.getCause()))
    );

    public CommandRegistry(GeyserImpl geyser, CommandManager<GeyserCommandSource> cloud) {
        this.geyser = geyser;
        this.cloud = cloud;

        // Override the default exception handlers that the typical cloud implementations provide so that we can perform localization.
        // This is kind of meaningless for our Geyser-Standalone implementation since these handlers are the default exception handlers in that case.
        for (ExceptionHandler<?> handler : exceptionHandlers) {
            handler.register(cloud);
        }

        // begin command registration
        registerBuiltInCommand(new HelpCommand(geyser, "help", "geyser.commands.help.desc", "geyser.command.help", "geyser", this.commands));
        registerBuiltInCommand(new ListCommand(geyser, "list", "geyser.commands.list.desc", "geyser.command.list"));
        registerBuiltInCommand(new ReloadCommand(geyser, "reload", "geyser.commands.reload.desc", "geyser.command.reload"));
        registerBuiltInCommand(new OffhandCommand(geyser, "offhand", "geyser.commands.offhand.desc", "geyser.command.offhand"));
        registerBuiltInCommand(new DumpCommand(geyser, "dump", "geyser.commands.dump.desc", "geyser.command.dump"));
        registerBuiltInCommand(new VersionCommand(geyser, "version", "geyser.commands.version.desc", "geyser.command.version"));
        registerBuiltInCommand(new SettingsCommand(geyser, "settings", "geyser.commands.settings.desc", "geyser.command.settings"));
        registerBuiltInCommand(new StatisticsCommand(geyser, "statistics", "geyser.commands.statistics.desc", "geyser.command.statistics"));
        registerBuiltInCommand(new AdvancementsCommand("advancements", "geyser.commands.advancements.desc", "geyser.command.advancements"));
        registerBuiltInCommand(new AdvancedTooltipsCommand("tooltips", "geyser.commands.advancedtooltips.desc", "geyser.command.tooltips"));
        registerBuiltInCommand(new ConnectionTestCommand(geyser, "connectiontest", "geyser.commands.connectiontest.desc", "geyser.command.connectiontest"));
        if (this.geyser.getPlatformType() == PlatformType.STANDALONE) {
            registerBuiltInCommand(new StopCommand(geyser, "stop", "geyser.commands.stop.desc", "geyser.command.stop"));
        }

        if (!this.geyser.extensionManager().extensions().isEmpty()) {
            registerBuiltInCommand(new ExtensionsCommand(this.geyser, "extensions", "geyser.commands.extensions.desc", "geyser.command.extensions"));
        }

        GeyserDefineCommandsEvent defineCommandsEvent = new GeyserDefineCommandsEventImpl(this.commands) {

            @Override
            public void register(@NonNull Command command) {
                if (!(command instanceof GeyserExtensionCommand extensionCommand)) {
                    throw new IllegalArgumentException("Expected GeyserExtensionCommand as part of command registration but got " + command + "! Did you use the Command builder properly?");
                }

                registerExtensionCommand(extensionCommand.extension(), extensionCommand);
            }
        };

        this.geyser.eventBus().fire(defineCommandsEvent);

        for (Map.Entry<Extension, Map<String, Command>> entry : this.extensionCommands.entrySet()) {
            Extension extension = entry.getKey();

            // Register this extension's root command
            extensionRootCommands.put(extension.rootCommand(), extension);

            // Register help commands for all extensions with commands
            String id = extension.description().id();
            registerExtensionCommand(extension, new HelpCommand(
                this.geyser,
                "help",
                "geyser.commands.exthelp.desc",
                "geyser.command.exthelp." + id,
                extension.rootCommand(),
                entry.getValue()));
        }

        // wait for the right moment (depends on the platform) to register permissions
        geyser.eventBus().subscribe(new GeyserEventRegistrar(this), GeyserRegisterPermissionsEvent.class, this::onRegisterPermissions);
    }

    @NonNull
    public CommandManager<GeyserCommandSource> cloud() {
        return cloud;
    }

    @NonNull
    public Map<String, Command> commands() {
        return Collections.unmodifiableMap(this.commands);
    }

    @NonNull
    public Map<Extension, Map<String, Command>> extensionCommands() {
        return Collections.unmodifiableMap(this.extensionCommands);
    }

    /**
     * For internal Geyser commands
     */
    public void registerBuiltInCommand(GeyserCommand command) {
        register(command, this.commands);
    }

    public void registerExtensionCommand(@NonNull Extension extension, @NonNull GeyserCommand command) {
        register(command, this.extensionCommands.computeIfAbsent(extension, e -> new HashMap<>()));
    }

    private void register(GeyserCommand command, Map<String, Command> commands) {
        command.register(cloud);

        commands.put(command.name(), command);
        geyser.getLogger().debug(GeyserLocale.getLocaleStringLog("geyser.commands.registered", command.name()));

        for (String alias : command.aliases()) {
            commands.put(alias, command);
        }

        if (!command.permission().isBlank() && command.permissionDefault() != null) {
            permissionDefaults.put(command.permission(), command.permissionDefault());
        }
    }

    private void onRegisterPermissions(GeyserRegisterPermissionsEvent event) {
        for (Map.Entry<String, TriState> permission : permissionDefaults.entrySet()) {
            event.register(permission.getKey(), permission.getValue());
        }
    }

    /**
     * Returns the description of the given command
     *
     * @param command the root command node
     * @param locale the ideal locale that the description should be in
     * @return a description if found, otherwise an empty string. The locale is not guaranteed.
     */
    @NonNull
    public String description(@NonNull String command, @NonNull String locale) {
        if (command.equals(GeyserCommand.DEFAULT_ROOT_COMMAND)) {
            return GeyserLocale.getPlayerLocaleString("geyser.command.root.geyser", locale);
        }

        Extension extension = extensionRootCommands.get(command);
        if (extension != null) {
            return GeyserLocale.getPlayerLocaleString("geyser.command.root.extension", locale, extension.name());
        }
        return "";
    }

    /**
     * Dispatches a command into cloud and handles any thrown exceptions.
     * This method may or may not be blocking, depending on the {@link ExecutionCoordinator} in use by cloud.
     */
    public void runCommand(@NonNull GeyserCommandSource source, @NonNull String command) {
        cloud.commandExecutor().executeCommand(source, command).whenComplete((result, throwable) -> {
            if (throwable == null) {
                return;
            }

            // mirrors typical cloud implementations
            if (throwable instanceof CompletionException) {
                throwable = throwable.getCause();
            }

            try {
                handleThrowable(source, result.commandContext(), throwable);
            } catch (Throwable secondary) {
                // otherwise it gets swallowed by whenComplete.
                // we assume this won't throw.
                handleUnexpectedThrowable(source, secondary);
            }
        });
    }

    private void handleThrowable(@NonNull GeyserCommandSource source, @NonNull CommandContext<GeyserCommandSource> commandContext, @NonNull Throwable throwable) {
        if (throwable instanceof Exception exception) {
            cloud.exceptionController().handleException(commandContext, exception);
            for (ExceptionHandler<?> handler : exceptionHandlers) {
                if (handler.handle(source, exception)) {
                    return;
                }
            }
        }
        handleUnexpectedThrowable(source, throwable);
    }

    private void handleNoPermission(GeyserCommandSource source, NoPermissionException exception) {
        // we basically recheck bedrock-only and player-only to see if they were the cause of this
        if (exception.missingPermission() instanceof GeyserPermission permission) {
            GeyserPermission.Result result = permission.check(source);
            if (result == GeyserPermission.Result.NOT_BEDROCK) {
                source.sendMessage(ChatColor.RED + GeyserLocale.getPlayerLocaleString("geyser.command.bedrock_only", source.locale()));
                return;
            }
            if (result == GeyserPermission.Result.NOT_PLAYER) {
                source.sendMessage(ChatColor.RED + GeyserLocale.getPlayerLocaleString("geyser.command.player_only", source.locale()));
                return;
            }
        } else {
            GeyserLogger logger = GeyserImpl.getInstance().getLogger();
            if (logger.isDebug()) {
                logger.debug("Expected a GeyserPermission for %s but instead got %s".formatted(exception.currentChain(), exception.missingPermission()));
            }
        }

        // Result.NO_PERMISSION, or we're unable to recheck
        source.sendLocaleString("geyser.command.permission_fail");
    }

    private void handleUnexpectedThrowable(GeyserCommandSource source, Throwable throwable) {
        source.sendLocaleString("command.failed"); // java edition translation key
        GeyserImpl.getInstance().getLogger().error("Exception while executing command handler", throwable);
    }

    @AllArgsConstructor
    private class ExceptionHandler<E extends Exception> {

        final Class<E> type;
        final BiConsumer<GeyserCommandSource, E> handler;

        @SuppressWarnings("unchecked")
        boolean handle(GeyserCommandSource source, Exception exception) {
            if (type.isInstance(exception)) {
                E e = (E) exception;
                // if cloud has a registered exception handler for this type, use it, otherwise use this handler.
                // we register all the exception handlers to cloud, so it will likely just be cloud invoking this same handler.
                cloud.exceptionController().handleException(source, type, e, handler);
                return true;
            }
            return false;
        }

        void register(CommandManager<GeyserCommandSource> manager) {
            manager.registerExceptionHandler(type, handler);
        }
    }
}
