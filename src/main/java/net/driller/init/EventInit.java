package net.driller.init;

import net.driller.event.DrillerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class EventInit {
    private static final DrillerEntityEvents entityEvents = new DrillerEntityEvents();

    public static void init() {
        UseEntityCallback.EVENT.register(entityEvents);
    }
}
