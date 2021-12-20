package home.anuradha.concurrent;

import home.anuradha.FIXMessage;

public class FIXMessagePool extends ObjectPool<FIXMessage>{
    public FIXMessagePool(int minObjects) {
        super(minObjects);
    }

    @Override
    protected FIXMessage createNewObject() {
        return new FIXMessage();
    }
}
