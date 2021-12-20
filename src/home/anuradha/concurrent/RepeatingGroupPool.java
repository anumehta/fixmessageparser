package home.anuradha.concurrent;

import home.anuradha.RepeatingGroup;

public class RepeatingGroupPool extends ObjectPool<RepeatingGroup> {
    public RepeatingGroupPool(int minObjects) {
        super(minObjects);
    }

    @Override
    protected RepeatingGroup createNewObject() {
        return new RepeatingGroup();
    }
}
