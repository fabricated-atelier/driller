package net.driller.init;

import net.driller.event.DrillerEntityEvents;
import net.driller.event.DrillerServerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class EventInit {
    private static final DrillerEntityEvents entityEvents = new DrillerEntityEvents();
    private static final DrillerServerEvents serverEvents = new DrillerServerEvents();

    public static void init() {
        UseEntityCallback.EVENT.register(entityEvents);
        ServerWorldEvents.LOAD.register(serverEvents);
    }
}
