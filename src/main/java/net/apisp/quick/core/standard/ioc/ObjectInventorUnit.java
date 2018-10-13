package net.apisp.quick.core.standard.ioc;

public class ObjectInventorUnit {
    public interface ObjectInventor {
        Object create(Object... args);
    }

    private ObjectInventor inventor;
    private Object[] args;

    private ObjectInventorUnit(ObjectInventor inventor, Object... args) {
        this.inventor = inventor;
        this.args = args;
    }

    public static ObjectInventorUnit create(ObjectInventor inventor, Object... args) {
        return new ObjectInventorUnit(inventor, args);
    }

    public ObjectInventorUnit.ObjectInventor getObjectInventor() {
        return this.inventor;
    }

    public Object[] getArgs() {
        return this.args;
    }
}
