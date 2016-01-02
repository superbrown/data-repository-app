package gov.energy.nrel.dataRepositoryApp.dao;

import gov.energy.nrel.dataRepositoryApp.settings.ISettings;
import gov.energy.nrel.dataRepositoryApp.utilities.FileAsRawBytes;
import gov.energy.nrel.dataRepositoryApp.dao.dto.StoredFile;
import gov.energy.nrel.dataRepositoryApp.dao.exception.CouldNotCreateDirectory;
import gov.energy.nrel.dataRepositoryApp.dao.exception.UnableToDeleteFile;
import gov.energy.nrel.dataRepositoryApp.utilities.Utilities;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileStorageStorageDAO implements IFileStorageDAO {

    protected ISettings settings;

    protected static final String NAME_OF_DIRECTORY_FOR_REMOVED_FILES = "removed";

    public FileStorageStorageDAO(ISettings settings) {

        this.settings = settings;
    }

    @Override
    public StoredFile saveFile(Date timestamp, String subdirectory, FileAsRawBytes file)
            throws CouldNotCreateDirectory, IOException {

        String fileName = file.fileName;

        String rootDirectoryForUploadedDataFiles = getRootDirectoryForUploadedDataFiles();

        // DESIGN NOTE: The location is hierarchical by year, month and time. The idea is to avoid having a single
        // directory containing hoards of files.
        String relativeFileLocation = constructRelativeFileLocation(timestamp);
        if (StringUtils.isNotBlank(subdirectory)) {
            relativeFileLocation += "/" + subdirectory;
        }

        String fullyQualifiedFileLocation = rootDirectoryForUploadedDataFiles + relativeFileLocation;

        if (seeToItThatTheDirectoryExists(fullyQualifiedFileLocation) == false) {
            throw new CouldNotCreateDirectory(fullyQualifiedFileLocation);
        }

        File savedFile = saveTo(file.bytes, fullyQualifiedFileLocation + "/" + fileName);

        StoredFile storedFile = new StoredFile(fileName, relativeFileLocation + "/" + fileName);

        return storedFile;
    }


    @Override
    public void deletFile(String file)
            throws UnableToDeleteFile {

        boolean success = (new File(getRootDirectoryForUploadedDataFiles() + file).delete());
        if (success == false) {
            throw new UnableToDeleteFile(file);
        }
    }

    @Override
    public void moveFilesToRemovedFilesLocation(String filePath)
            throws IOException {

        String rootDirectoryForUploadedDataFiles = getRootDirectoryForUploadedDataFiles();
        String rootDirectoryForRemovedFiles = getRootDirectoryForRemovedFiles();

        String relativePath = extractPathToContainingDirectory(filePath);

        String sourcePath = rootDirectoryForUploadedDataFiles + relativePath;
        String destinationPath = rootDirectoryForRemovedFiles + relativePath;

        seeToItThatTheDirectoryExists(extractPathToContainingDirectory(destinationPath));

        Utilities.moveFolder(sourcePath, destinationPath);
    }

    private String extractPathToContainingDirectory(String filePath) {

        return filePath.substring(0, filePath.lastIndexOf("/"));
    }

    @Override
    public File getFile(String storageLocation) {

        return new File(getRootDirectoryForUploadedDataFiles() + storageLocation);
    }

    protected String getRootDirectoryForUploadedDataFiles() {

        String dataFilesDirectoryPath = settings.getRootDirectoryForUploadedDataFiles();
        dataFilesDirectoryPath = seeToItThatItEndsWithAFileSeparator(dataFilesDirectoryPath);
        return dataFilesDirectoryPath;
    }

    protected String getRootDirectoryForRemovedFiles() {

        return getRootDirectoryForUploadedDataFiles() + NAME_OF_DIRECTORY_FOR_REMOVED_FILES + "/";
    }

    protected File saveTo(byte[] fileContent, String newFilePath) throws IOException {

        Files.write(Paths.get(newFilePath), fileContent);
        return new File(newFilePath);
    }

    protected String seeToItThatItEndsWithAFileSeparator(String path) {

        if (path.endsWith(File.separator) == false) {
            path = path + "/";
        }
        return path;
    }

    protected static String constructRelativeFileLocation(Date timestamp) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/yyyy-MM-dd_aaa-hh-mm-ss_SSS-Z");
        return formatter.format(timestamp);
    }

    protected static boolean seeToItThatTheDirectoryExists(String uploadPath) {

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            return uploadDir.mkdirs();
        }

        return true;
    }
}