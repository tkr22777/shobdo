package importer.gsheet;

public class InsufficientDataException extends Exception {
    public InsufficientDataException(String message) {
        super(message);
    }
}
