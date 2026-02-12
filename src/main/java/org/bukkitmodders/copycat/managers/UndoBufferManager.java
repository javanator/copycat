package org.bukkitmodders.copycat.managers;

import com.google.common.collect.EvictingQueue;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.model.UndoHistoryComponent;

import java.util.Map;
import java.util.Queue;
import java.util.Stack;

public class UndoBufferManager {

    private final Map<String, Queue<UndoHistoryComponent>> undoBuffers = new java.util.HashMap<>();
    private final Application application;

    public UndoBufferManager(Application application) {
        this.application = application;
    }

    public Queue<UndoHistoryComponent> getUndoBuffer(final String playerName) {
        return undoBuffers.computeIfAbsent(playerName, k -> EvictingQueue.create(20));
    }

    public void undo(Player player) {
        player.sendMessage("Undo for " + player.getName());
        Queue<UndoHistoryComponent> buffer = getUndoBuffer(player.getName());

        if (!buffer.isEmpty()) {
            UndoHistoryComponent lastUndo = null;
            if (buffer instanceof java.util.List) {
                lastUndo = ((java.util.List<UndoHistoryComponent>) buffer).remove(buffer.size() - 1);
            } else {
                // For EvictingQueue, we need to find the last element and remove it.
                // Since it doesn't support easy tail removal, we might have to reconstruct it
                // or use a different approach. But EvictingQueue is what was requested.
                // A simple but inefficient way to "pop" from a Queue:
                java.util.List<UndoHistoryComponent> list = new java.util.ArrayList<>(buffer);
                if (!list.isEmpty()) {
                    lastUndo = list.remove(list.size() - 1);
                    buffer.clear();
                    buffer.addAll(list);
                }
            }

            if (lastUndo != null) {
                lastUndo.revert();
            }
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
