package gov.energy.nrel.dataRepositoryApp.utilities.fileReader.exception;

public class UnsupportedFileExtension extends Exception {

    public final String fileName;

    public UnsupportedFileExtension(String fileName) {
        this.fileName = fileName;
    }
}
