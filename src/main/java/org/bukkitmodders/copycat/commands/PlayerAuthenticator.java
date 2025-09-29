package org.bukkitmodders.copycat.commands;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.StringUtil;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.model.PlayerSettingsType;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Builder
public class PlayerAuthenticator extends Authenticator {

    private final Application application;
    private final Player player;
    private final HttpRequest request;
    private final PlayerSettingsType.Shortcut shortcut;

    private String username;
    private String password;
    private int attempts;

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if (attempts > 3) {
                attempts++;
                return null;
        }

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            return new PasswordAuthentication(username, password.toCharArray());
        }

        DialogAction.CustomClickAction submitAction = DialogAction.customClick(
                (view, audience) -> {
                    username = view.getText("username");
                    password = view.getText("password");

                    if (audience instanceof Player player) {
                        HttpClient httpClient = HttpClient.newBuilder()
                                .followRedirects(HttpClient.Redirect.NORMAL)
                                .connectTimeout(Duration.ofSeconds(30))
                                .authenticator(this)
                                .build();
                        CompletableFuture<HttpResponse<byte[]>> responseFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
                        BukkitScheduler scheduler = application.getServer().getScheduler();
//                        if (isPolling) {
//                            scheduler.runTaskAsynchronously(application, () -> handleHttpResponse(responseFuture, player, shortcut, scheduler));
//                        } else {
                            CommandBuilder.handleHttpResponse(responseFuture, player, shortcut, scheduler);
//                        }
                    }
                },
                ClickCallback.Options.builder()
                        .uses(1) // Set the number of uses for this callback. Defaults to 1
                        .lifetime(Duration.ofMinutes(5)) // Set the lifetime of the callback. Defaults to 12 hours
                        .build()
        );

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("URL needs Authentication"))
                        .inputs(List.of(
                                DialogInput.text("username", Component.text("Username")).build(),
                                DialogInput.text("password", Component.text("Password")).build()
                        ))
                        .build())
                .type(DialogType.notice(ActionButton.create(Component.text("Submit")
                                , Component.text("Submit your username and password")
                                , 100,
                                submitAction
                        ))
                ));

        player.showDialog(dialog);

        return null;
    }
}
