package org.bukkitmodders.copycat.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;

import java.util.Optional;
import java.util.Stack;

/**
 * UndoHistory represents a single undo-able action, containing the state of the blocks
 * and optionally a media player if the action was a video stream.
 */
@Builder(setterPrefix = "with")
@Getter
@Setter
public class UndoHistoryComponent {

    private Stack<RevertibleBlock> blocks;
    private DirectMediaPlayer mediaPlayer;
}
