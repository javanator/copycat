package org.bukkitmodders.copycat.commands;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.Builder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Application;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.Duration;
import java.util.List;

@Builder
public class PlayerAuthenticator extends Authenticator {

    private static final long POLLING_PERIOD_TICKS = 20L; // 1 second

    private final Application application;
    private final Player player;

    private String username;
    private String password;

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        final Thread t = Thread.currentThread();

        //The dialog is non-blocking.
        DialogAction.CustomClickAction submitCredentialsAction = DialogAction.customClick(
                (view, audience) -> {
                    String enteredUsername = view.getText("username");
                    String enteredPassword = view.getText("password");

                    if (!(audience instanceof Player clickPlayer)) {
                        return;
                    }

                    // Cache for authenticator use on subsequent challenges
                    this.username = enteredUsername;
                    this.password = enteredPassword;
                    t.interrupt();
                },
                ClickCallback.Options.builder()
                        .uses(3)
                        .lifetime(Duration.ofSeconds(60))
                        .build()
        );

        Dialog dialog = buildAuthDialog(submitCredentialsAction);
        player.showDialog(dialog);

        return waitForCredentials();
    }

    /**
     * Waits for user input to supply credentials in a blocking dialog.
     *
     * @return {@code PasswordAuthentication} if credentials are supplied, otherwise null.
     */
    private PasswordAuthentication waitForCredentials() {
        try {
            application.getLogger().info("Sleeping for credentials on http request");
            Thread.sleep(Duration.ofSeconds(61));
            return null;
        } catch (InterruptedException e) {
            //On supply of input, interrupt is expected.
            application.getLogger().info("Credentials supplied. Thread.sleep() interrupted");
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }


    private Dialog buildAuthDialog(DialogAction.CustomClickAction submitAction) {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("URL needs Authentication"))
                        .inputs(List.of(
                                DialogInput.text("username", Component.text("Username")).build(),
                                DialogInput.text("password", Component.text("Password")).build()
                        ))
                        .build())
                .type(DialogType.notice(
                        ActionButton.create(
                                Component.text("Submit"),
                                Component.text("Submit your username and password"),
                                200,
                                submitAction
                        )
                )));
    }
}
