package client;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class GameObject {
    private Map<String, GameObject> children;
    private Map<String, GameObject> toRemove;
    private String id;
    protected GameObject parent;

    public GameObject(String id, GameObject parent) {
        this.id = id;
        children = new HashMap<String, GameObject>(0);
        toRemove = new HashMap<String, GameObject>(0);
        this.parent = parent;
    }

    public void update() {
        for (String s : toRemove.keySet()) {
            children.remove(s);
        }
        toRemove.clear();
        for (GameObject child : children.values()) {
            child.update();
        }
    }

    public void addChild(GameObject gameObject) {
        children.put(gameObject.getId(), gameObject);
    }

    public String getId() {
        return id;
    }

    public GameObject getChild(String playerID) {
        return children.get(playerID);
    }

    public Map<String, GameObject> getChildren() {
        return children;
    }

    public void remove(GameObject gameObject) {
        toRemove.put(gameObject.getId(),gameObject);
    }

    public GameObject getParent() {
        return parent;
    }
}
