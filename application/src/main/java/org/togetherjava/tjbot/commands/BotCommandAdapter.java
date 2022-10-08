package org.togetherjava.tjbot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.Contract;

import org.togetherjava.tjbot.commands.componentids.ComponentId;
import org.togetherjava.tjbot.commands.componentids.ComponentIdGenerator;
import org.togetherjava.tjbot.commands.componentids.Lifespan;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Adapter implementation of a {@link BotCommand}. The minimal setup only requires implementation of
 * their respective command method. A new command can then be registered by adding it to
 * {@link Features}.
 * <p>
 * Further, {@link #onButtonClick(ButtonInteractionEvent, List)} and
 * {@link #onSelectionMenu(SelectMenuInteractionEvent, List)} can be overridden if desired. The
 * default implementation is empty, the adapter will not react to such events.
 * <p>
 * <p>
 * The adapter manages some getters for you, you've to create the {@link CommandData} yourself. See
 * {@link #BotCommandAdapter(CommandData, CommandVisibility)}} for more info on that. Minimal
 * modifications can be done on the {@link CommandData} returned by {@link #getData()}.
 * <p>
 * <p>
 * If implementations want to add buttons or selection menus, it is highly advised to use component
 * IDs generated by {@link #generateComponentId(String...)}, which will automatically create IDs
 * that are valid per {@link SlashCommand#onSlashCommand(SlashCommandInteractionEvent)}.
 * <p>
 * <p>
 * Some example commands are available in {@link org.togetherjava.tjbot.commands.basic}.
 * Registration of commands can be done in {@link Features}.
 */
public abstract class BotCommandAdapter implements BotCommand {
    private final String name;
    private final Command.Type type;
    private final CommandVisibility visibility;
    private final CommandData data;
    private ComponentIdGenerator componentIdGenerator;

    /**
     * Creates a new adapter with the given data.
     *
     * @param data the data for this command
     * @param visibility the visibility of the command
     */
    protected BotCommandAdapter(CommandData data, CommandVisibility visibility) {
        this.data = data.setGuildOnly(visibility == CommandVisibility.GUILD);
        this.visibility = Objects.requireNonNull(visibility, "The visibility shouldn't be null");

        this.name = data.getName();
        this.type = data.getType();
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public Command.Type getType() {
        return type;
    }

    @Override
    public final CommandVisibility getVisibility() {
        return visibility;
    }

    @Override
    public CommandData getData() {
        return data;
    }

    @Override
    @Contract(mutates = "this")
    public final void acceptComponentIdGenerator(ComponentIdGenerator generator) {
        componentIdGenerator =
                Objects.requireNonNull(generator, "The given generator cannot be null");
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override
    public void onButtonClick(ButtonInteractionEvent event, List<String> args) {
        // Adapter does not react by default, subclasses may change this behavior
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override
    public void onSelectionMenu(SelectMenuInteractionEvent event, List<String> args) {
        // Adapter does not react by default, subclasses may change this behavior
    }

    /**
     * Helper method to generate component IDs that are considered valid per
     * {@link #acceptComponentIdGenerator(ComponentIdGenerator)}.
     * <p>
     * They can be used to create buttons or selection menus and transport additional data
     * throughout the event (e.g. the user id who created the button dialog).
     * <p>
     * IDs generated by this method have a regular lifespan, meaning that they might get evicted and
     * expire after not being used for a long time. Use
     * {@link #generateComponentId(Lifespan, String...)} to set other lifespans, if desired.
     *
     * @param args the extra arguments that should be part of the ID
     * @return the generated component ID
     */
    @SuppressWarnings("OverloadedVarargsMethod")
    protected final String generateComponentId(String... args) {
        return generateComponentId(Lifespan.REGULAR, args);
    }

    /**
     * Helper method to generate component IDs that are considered valid per
     * {@link #acceptComponentIdGenerator(ComponentIdGenerator)}.
     * <p>
     * They can be used to create buttons or selection menus and transport additional data
     * throughout the event (e.g. the user id who created the button dialog).
     *
     * @param lifespan the lifespan of the component id, controls when an id that was not used for a
     *        long time might be evicted and expire
     * @param args the extra arguments that should be part of the ID
     * @return the generated component ID
     */
    @SuppressWarnings({"OverloadedVarargsMethod", "WeakerAccess"})
    protected final String generateComponentId(Lifespan lifespan, String... args) {
        return componentIdGenerator
            .generate(new ComponentId(UserInteractorPrefix.getPrefixedNameFromInstance(this),
                    Arrays.asList(args)), lifespan);
    }
}
