package gov.energy.nbc.car.businessObject.dto;

public class FileAsRawBytes {

    public String fileName;
    public byte[] bytes;

    public FileAsRawBytes(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
    }
}