package org.apache.cloudstack.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class VinesUtil {

    private static String vnfpRepository = "/var/cloudstack-vnfm/vnfp_repository/";
    private static String vnfgdRepository = "/var/cloudstack-nfvo/vnffgd_repository/";

    private static List<String> fileList;

    /////////////////////////////////////////////////////
    //////////////// VNF Packages Utils /////////////////
    /////////////////////////////////////////////////////

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                    return false;
            }
        }
        return dir.delete();
    }

    public static boolean extractVnfpZip(String vnfpUuid) throws IOException {
        // Open the file
        String fileName = vnfpRepository + vnfpUuid + ".zip";
        try (ZipFile file = new ZipFile(fileName)) {
            FileSystem fileSystem = FileSystems.getDefault();
            Enumeration<? extends ZipEntry> entries = file.entries();
            // Unzip files
            String uncompressedDirectory = vnfpRepository + vnfpUuid + "/";
            Files.createDirectory(fileSystem.getPath(uncompressedDirectory));
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    Files.createDirectories(fileSystem.getPath(uncompressedDirectory + entry.getName()));
                } else {
                    InputStream is2 = file.getInputStream(entry);
                    BufferedInputStream bis = new BufferedInputStream(is2);
                    String uncompressedFileName = uncompressedDirectory + entry.getName();
                    Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                    Files.createFile(uncompressedFilePath);
                    FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
                    while (bis.available() > 0) {
                        fileOutput.write(bis.read());
                    }
                    fileOutput.close();
                }
            }
        }
        // Delete Zip file
        File zip_file = new File(fileName);
        zip_file.delete();
        return true;
    }

    public static boolean downloadVnfp(String vnfpUuid, String vnfpName, String vnfpUrl) {
        // Format detection (Git or Zip)
        String format = vnfpUrl.substring(vnfpUrl.lastIndexOf(".") + 1);
        URL url;
        try {
            url = new URL(URLDecoder.decode(vnfpUrl, "UTF-8"));
        } catch (Exception e) {
            return false;
        }

        if (format.equals("zip")) {
            String destination = vnfpRepository + vnfpUuid + ".zip";
            try {
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(60000);
                connection.connect();

                InputStream is = new BufferedInputStream(url.openStream());
                OutputStream os = new FileOutputStream(destination);

                byte data[] = new byte[1024];
                int count;

                while ((count = is.read(data)) != -1) {
                    os.write(data, 0, count);
                }
                os.flush();
                os.close();
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(
                        "ERRO BUSCANDO ARQUIVO DE " + vnfpUrl + " PARA " + destination + " – " + e.getMessage());
                return false;
            }
        } else {
            if (format.equals("git")) {
                String cloneDirectoryPath = vnfpRepository + vnfpUuid + "/";
                try {
                    System.out.println("Cloning " + url.toString());
                    Git.cloneRepository().setURI(url.toString()).setDirectory(Paths.get(cloneDirectoryPath).toFile())
                            .call();
                    System.out.println("Completed Cloning");

                    // Remove the .git directory into repository
                    File f = new File(cloneDirectoryPath + ".git/");
                    boolean response = deleteDir(f);
                    if (!response)
                        return false; // delete error

                } catch (GitAPIException e) {
                    System.out.println("Exception occurred while cloning repo");
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false; // invalid vnfp format
            }
        }
        return true;
    }

    /**
     * Compress a folder (Zip format)
     *
     * @param source the source path
     * @param output the output path
     * @throws IOException
     */
    public static void zipFolder(String source, String output) {
        fileList = new ArrayList<String>();
        generateFileList(source, new File(source));
        byte[] buffer = new byte[1024];
        try {
            FileOutputStream fos = new FileOutputStream(output);
            ZipOutputStream zos = new ZipOutputStream(fos);
            System.out.println("Output to Zip : " + output);
            for (String file : fileList) {
                System.out.println("File Added : " + file);
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);
                FileInputStream in = new FileInputStream(source + File.separator + file);
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
            }
            zos.closeEntry();
            zos.close();
            System.out.println("Done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        fileList = null;
    }

    // Traverse a directory and get all files, and add the file into fileList
    private static void generateFileList(String source, File node) {
        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(source, node.getAbsoluteFile().toString()));
        }
        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(source, new File(node, filename));
            }
        }
    }

    // Format the file path for zip
    private static String generateZipEntry(String source, String file) {
        return file.substring(source.length() + 1, file.length());
    }

    /////////////////////////////////////////////////////
    //////////////////// VNFFGD Utils ///////////////////
    /////////////////////////////////////////////////////
    public static boolean downloadVnffgd(String vnffgdUuid, String vnffgdUrl) {
        // Format detection (JSON, YAML or Zip)
        String format = vnffgdUrl.substring(vnffgdUrl.lastIndexOf(".") + 1);
        URL url;
        try {
            url = new URL(URLDecoder.decode(vnffgdUrl, "UTF-8"));
        } catch (Exception e) {
            System.out.println("ERRO - Invalid URL");
            return false;
        }
        if (format.equals("yaml") || format.equals("YAML") || format.equals("yml") || format.equals("YML")) {
            System.out.println("ERRO - Feature not implemented");
            return false;
        } else if (format.equals("json") || format.equals("JSON") || format.equals("zip")) {
            String destination = vnfgdRepository + vnffgdUuid + "." + format;
            try {
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(60000);
                connection.connect();
                InputStream is = new BufferedInputStream(url.openStream());
                OutputStream os = new FileOutputStream(destination);
                byte data[] = new byte[1024];
                int count;
                while ((count = is.read(data)) != -1) {
                    os.write(data, 0, count);
                }
                os.flush();
                os.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(
                        "ERRO BUSCANDO ARQUIVO DE " + vnffgdUrl + " PARA " + destination + " – " + e.getMessage());
                return false;
            }
        } else {
            System.out.println("ERRO - Invalid VNFFGD format");
            return false;
        }
        return true;
    }
}