package Exceptions;

public class EntityDoesNotExist extends IllegalArgumentException {

    public EntityDoesNotExist(String messages) {
        super(messages);
    }

}
