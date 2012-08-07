package server;

import common.CollisionChecker;
import common.Configuration;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public class GameWorld {
    private static GameWorld ourInstance = new GameWorld();
    private Map<String, PlayerCharacter> players = new ConcurrentHashMap<String, PlayerCharacter>();
    private Map<String, NPC> nPCs = new ConcurrentHashMap<String, NPC>();
    private static AtomicInteger nextNPC = new AtomicInteger(0);

    public static GameWorld getInstance() {
        return ourInstance;
    }

    private GameWorld() {
//        CollisionChecker.instance().setShape(new Rectangle(0, 0, Configuration.getInstance().getWindowSize(), Configuration.getInstance().getWindowSize()));
    }

    public PlayerCharacter getGameCharacter(String id) {
        return players.get(id);
    }

    public void addPlayer(PlayerCharacter character) {
        players.put(character.getId(), character);
    }

    public Map<String, PlayerCharacter> getPlayerCharacters() {
        return players;
    }

    public void removePlayer(AbstractCharacter abstractCharacter) {
        players.remove(abstractCharacter.getId());
    }


    public int getNumNPCs() {
        return nPCs.size();
    }

    public void spawnNPC() {
        String id = "" + nextNPC.getAndIncrement();
        NPC npc = new NPC("" + id);
        nPCs.put(id, npc);
        ServerMain.getExecutor().schedule(npc, 0, TimeUnit.MILLISECONDS);
    }

    public void removeNPC() {
        if (!nPCs.isEmpty()) {
            NPC npc = nPCs.values().iterator().next();
            npc.die();
            nPCs.remove(npc.getId());
        }
    }

    public Map<String, NPC> getNPCs() {
        return nPCs;
    }

    public void killNPC(NPC npc) {
        npc.die();
        nPCs.remove(npc.getId());
    }
}
