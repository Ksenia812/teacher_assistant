package com.grsu.teacherassistant.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static com.grsu.teacherassistant.utils.PropertyUtils.AUTO_BACKUP_PROPERTY_NAME;
import static com.grsu.teacherassistant.utils.PropertyUtils.getProperty;

public class FileUtils {
    public static final String separator = File.separator;
    public static final String STUDENTS_PHOTO_EXTENSION = ".jpg";
    private static final String TOMCAT_PATH = System.getProperty("catalina.home");
    private static final String APP_FILES_DIR_NAME = "app_files";
    public static final String APP_FILES_PATH = buildPath(TOMCAT_PATH, APP_FILES_DIR_NAME);
    private static final String WEBAPPS_DIR_NAME = "webapps";
    public static final String WEBAPPS_FILES_PATH = buildPath(TOMCAT_PATH, WEBAPPS_DIR_NAME);
    private static final ClassLoader classLoader = FileUtils.class.getClassLoader();
    public static final String CONFIG_FILE_PATH = buildPath(APP_FILES_PATH, "config", "config.properties");
    public static final String CSV_FOLDER_PATH = buildPath(APP_FILES_PATH, "csv");
    public static final String CSV_EXTENSION = ".csv";
    public static final String STUDENTS_PHOTO_FOLDER_PATH = buildPath(WEBAPPS_FILES_PATH, "photo", "students");
    public static final String DATABASE_PATH = buildPath(APP_FILES_PATH, "database", getProperty("db.name"));//classLoader.getResource("/database/db.s3db").getPath();
    public static final String DEFAULT_BACKUP_PATH = "backups/";
    /**
     * Builds system-dependent path with specific separators
     *
     * @param separatorPrefix
     * @param separatorPostfix
     * @param args
     */
    public static String buildPath(boolean separatorPrefix, boolean separatorPostfix, String... args) {
        StringBuilder sb = new StringBuilder();
        if (separatorPrefix) {
            sb.append(separator);
        }
        for (String arg : args) {
            sb.append(arg);
            sb.append(separator);
        }
        if (!separatorPostfix) {
            sb.setLength(sb.length() - separator.length());
        }
        return sb.toString();
    }

    /**
     * Builds system-dependent path without trailing separators
     *
     * @param args
     * @return
     */
    public static String buildPath(String... args) {
        return buildPath(false, false, args);
    }

    /**
     * Builds system-dependent absolute path to resource file
     *
     * @param name
     * @param directories
     */
    @Deprecated
    public static String getAbsolutePathToResourceFile(String name, String... directories) {
        StringBuilder fullPath = new StringBuilder(separator);
        for (String dir : directories) {
            fullPath.append(dir);
            fullPath.append(separator);
        }
        fullPath.append(name);
        return classLoader.getResource(fullPath.toString()).getPath();
    }

    public static List<File> getCSVFilesFromAppFilesFolder() {
        return getFilesFromFolder(CSV_FOLDER_PATH, CSV_EXTENSION);
    }

    public static List<File> getFilesFromFolder(String path, String extension) {
        List<File> files = new ArrayList<>();
        collectFilesFromFolder(new File(path), extension, files);
        return files;
    }

    public static File getFile(String path, String fileName, String extension) {
        String filePath = buildPath(path, fileName);
        return new File(filePath + extension);
    }

    private static void collectFilesFromFolder(final File folder, String extension, List<File> files) {
        if (folder.listFiles() != null)
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    collectFilesFromFolder(fileEntry, extension, files);
                } else {
                    if (extension != null && fileEntry.getName().endsWith(extension)) {
                        files.add(fileEntry);
                    } else if (extension == null) {
                        files.add(fileEntry);
                    }
                }
            }
    }

    private static String generateBackupName() {
        GregorianCalendar calendar = new GregorianCalendar();
        StringBuilder name = new StringBuilder();
        name.append(calendar.get(Calendar.DAY_OF_MONTH)).append("-")
            .append(calendar.get(Calendar.MONTH)).append("-")
            .append(calendar.get(Calendar.YEAR)).append("_")
            .append(calendar.get(Calendar.HOUR_OF_DAY)).append("-")
            .append(calendar.get(Calendar.MINUTE)).append("-")
            .append(calendar.get(Calendar.SECOND)).append(".")
            .append(calendar.get(Calendar.MILLISECOND)).append("_");
        return name.toString();
    }

    public static void backupDatabase(String pathTo, String name) {
        try {
            if (!Boolean.valueOf(getProperty(AUTO_BACKUP_PROPERTY_NAME)) || !Files.exists(Paths.get(DATABASE_PATH))) {
                return;
            }
            if (!Files.exists(Paths.get(DEFAULT_BACKUP_PATH))) {
                System.out.println(Files.createDirectories(Paths.get(DEFAULT_BACKUP_PATH)));
            }
            Files.copy(Paths.get(DATABASE_PATH), Paths.get(pathTo + name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void backupDatabase(String reason) {
        try {
            if (!Boolean.valueOf(getProperty(AUTO_BACKUP_PROPERTY_NAME)) || !Files.exists(Paths.get(DATABASE_PATH))) {
                return;
            }
            if (!Files.exists(Paths.get(DEFAULT_BACKUP_PATH))) {
                System.out.println(Files.createDirectories(Paths.get(DEFAULT_BACKUP_PATH)));
            }
            System.out.println(Files.copy(
                Paths.get(DATABASE_PATH),
                Paths.get(DEFAULT_BACKUP_PATH + generateBackupName() + reason + ".s3db"),
                StandardCopyOption.REPLACE_EXISTING
            ).toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
