package net.driller.init;

import net.driller.data.TrainConnectionsSavedData;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

public interface GameRuleInit {
    GameRules.Key<GameRules.IntegerValue> MAX_TRAIN_LENGTH = GameRuleRegistry.register(
            "max_train_length",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(5, 1, 10,
                    (server, integerValue) ->
                            TrainConnectionsSavedData.get(server).getConnections()
                                    .forEach(connection -> connection.onMaxLengthChanged(integerValue.get()))
            )
    );

    static void init() {
        // static initialisation
    }
}
