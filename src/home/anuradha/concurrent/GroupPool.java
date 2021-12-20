package home.anuradha.concurrent;

import home.anuradha.Group;

public class GroupPool extends ObjectPool<Group>{
    public GroupPool(int minObjects) {
        super(minObjects);
    }

    @Override
    protected Group createNewObject() {
        return new Group();
    }
}
