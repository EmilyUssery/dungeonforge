package dungeonforge.factory;

import java.util.List;

/**
 * WEEK 12 (US-9.1) -- ADAPTER: the TARGET interface.
 *
 * This is what the client (MonsterFactory, and whoever registers blueprints with it) already
 * speaks: a list of MonsterDefs. Nothing here mentions a file, a column or a format. If it did,
 * the adaptee's vocabulary would have leaked into the target, and every new source would have
 * to pretend to be a CSV.
 */
public interface MonsterSource {

    /** Every blueprint this source can supply, already in DungeonForge's terms. */
    List<MonsterDef> definitions();

    /** A short label for messages and translation notes, e.g. "legacy". */
    String sourceName();
}
