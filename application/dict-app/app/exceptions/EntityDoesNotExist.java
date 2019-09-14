package exceptions;

public class EntityDoesNotExist extends IllegalArgumentException {

    public EntityDoesNotExist(String messages) {
        super(messages);
    }
}
