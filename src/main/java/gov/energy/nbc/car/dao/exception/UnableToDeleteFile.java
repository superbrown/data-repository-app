package gov.energy.nbc.car.dao.exception;

public class UnableToDeleteFile extends Throwable {

    private final String file;

    public UnableToDeleteFile(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "UnableToDeleteFile{" +
                "file='" + file + '\'' +
                '}';
    }
}