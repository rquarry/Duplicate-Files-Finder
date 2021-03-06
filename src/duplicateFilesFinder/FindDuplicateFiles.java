package duplicateFilesFinder;

import java.nio.file.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import static java.nio.file.FileVisitResult.*;

public class FindDuplicateFiles extends SimpleFileVisitor<Path> {

    // This is the max file size read with "readAllBytes" before input is broken
    // into byte array chunks
    static final int MAX_FILE_SIZE = 52428800;
    // This is amount of chunked data read when over 50MB
    static final int READ_SIZE = 8192;
    public int count = 0;
    public ArrayList<String> userFiles;

    public FindDuplicateFiles() {

        userFiles = new ArrayList();
    }
    /*
     * This function is called for each
     */

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {

        String tmpString;
        File myFile = file.toFile();

        tmpString = md5sum(myFile) + " " + file.toString();

        userFiles.add(tmpString);

        return CONTINUE;
    }

    public String md5sum(File file) {

        String hash = null;
        byte[] data = new byte[READ_SIZE];
        int length;

        if (file == null) {
            return hash;
        } /*
         * This makes sure a file over 50MB isn't converted to bytes in one shot
         * which crashes the stack. If it's less than 50MB the "else" below
         * handles it nicely
         */ else if (file.length() > MAX_FILE_SIZE) {
            try {
                System.out.println("big file found.....!\n");
                InputStream input = new FileInputStream(file);
                MessageDigest dg = MessageDigest.getInstance("MD5");

                while ((length = input.read(data)) != -1) {

                    dg.update(data, 0, length);

                }
                input.close();
                hash = new BigInteger(1, dg.digest()).toString(16);

            } catch (IOException f) {
            } catch (NoSuchAlgorithmException e) {
            }
        } else {
            try {
                data = Files.readAllBytes(file.toPath());
                MessageDigest dg = MessageDigest.getInstance("MD5");
                dg.update(data);
                hash = new BigInteger(1, dg.digest()).toString(16);
            } catch (IOException i) {
            } catch (NoSuchAlgorithmException e) {
            }
        }
        return hash;
    }

    /*
     * This function kicks off the file tree walk visitFile function and
     * controls actions on each file visited
     */
    public void loadList(File userFile) {

        Path startdir = userFile.toPath();

        try {

            Files.walkFileTree(startdir, this);

        } catch (IOException e) {
        }

    }

    public ArrayList<String> findDuplicates() {

        ListIterator<String> itr = userFiles.listIterator();
        ArrayList<String> matches = new ArrayList();
        String tmp = null, tmp2 = null;
        filePrinterComparator myComp = new filePrinterComparator();

        count = 0;

        while (itr.hasNext()) {

            if (count > 0) {

                tmp = tmp2;
                tmp2 = itr.next();
            } else {

                tmp = itr.next();
                // this protects against an odd number of elements crashing
                // the program
                if (itr.hasNext()) {
                    tmp2 = itr.next();
                } else {
                    break;
                }
            }

            //if (tmp.substring(0, 31).compareTo(tmp2.substring(0, 31)) == 0) {
            if (myComp.compare(tmp, tmp2) == 0) {
                matches.add(tmp);
                matches.add(tmp2);
                count++;
            }
        }
        return matches;
    }
}
