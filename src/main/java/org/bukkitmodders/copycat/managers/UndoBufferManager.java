package org.bukkitmodders.copycat.managers;

import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.model.UndoHistoryComponent;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

public class UndoBufferManager {

    private final Map<String, Deque<UndoHistoryComponent>> undoBuffers = new java.util.HashMap<>();
    private final Application application;

    public UndoBufferManager(Application application) {
        this.application = application;
    }

    public Deque<UndoHistoryComponent> getUndoBuffer(final String playerName) {
        int maxUndoSize = application.getConfigurationManager().getMaxUndoSize();
        return undoBuffers.computeIfAbsent(playerName, k -> new LinkedList<UndoHistoryComponent>() {
            @Override
            public boolean add(UndoHistoryComponent e) {
                if (size() >= maxUndoSize) {
                    removeFirst();
                }
                return super.add(e);
            }

            @Override
            public void addLast(UndoHistoryComponent e) {
                if (size() >= maxUndoSize) {
                    removeFirst();
                }
                super.addLast(e);
            }
        });
    }

    public void undo(Player player) {
        player.sendMessage("Undo for " + player.getName());
        Deque<UndoHistoryComponent> buffer = getUndoBuffer(player.getName());

        UndoHistoryComponent lastUndo = buffer.pollLast();
        if (lastUndo != null) {
            lastUndo.revert();
        }
    }

    public void purgeAll() {
        undoBuffers.values().forEach(buffer -> {
            buffer.forEach(UndoHistoryComponent::revert);
            buffer.clear();
        });
        undoBuffers.clear();
    }
}
