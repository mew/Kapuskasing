package ca.noratastic.kapuskasing;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Main {
    static final HashMap<String, String> methods = new HashMap<>();
    static final HashMap<String, String> fields = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: kapuskaming.jar <jar> <methods> <fields>");
            return;
        }

        File inputJar = new File(args[0]);
        File methodsCsv = new File(args[1]);
        File fieldsCsv = new File(args[2]);

        readMappings(methodsCsv, methods);
        readMappings(fieldsCsv, fields);

        String out;
        if (args.length == 4) {
            out = args[3];
        } else {
            out = inputJar.getName().substring(0, inputJar.getName().lastIndexOf(".")) + "-out.jar";
        }
        File outputJar = new File(out);

        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(outputJar.toPath()))) {
            try (ZipFile zipFile = new ZipFile(inputJar)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    ZipEntry entry2;
                    byte[] entryBytes;

                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        if (entry.getName().endsWith(".class")) {
                            entry2 = zipFile.getEntry(entry.getName());
                            ClassNode clazz = new ClassNode();
                            ClassReader classReader = new ClassReader(stream);
                            classReader.accept(clazz, ClassReader.SKIP_CODE);

                            ClassNode clazz2 = new ClassNode();
                            Remapper remapper = new RemapperImplementation();
                            ClassRemapper classRemapper = new ClassRemapper(clazz2, remapper);
                            classReader.accept(classRemapper, ClassReader.EXPAND_FRAMES);

                            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                            clazz2.accept(classWriter);

                            entry2 = new ZipEntry(clazz2.name + ".class");
                            entryBytes = classWriter.toByteArray();

                        } else {
                            entry2 = entry;
                            entryBytes = readInputStream(stream);
                        }
                        outputStream.putNextEntry(entry2);
                        outputStream.write(entryBytes);
                        outputStream.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void readMappings(File csv, HashMap<String, String> map) throws IOException {
        for (String line : Files.readAllLines(csv.toPath())) {
            if (!line.contains(",") || line.contains("searge")) {
                continue;
            }

            String[] split = line.split(",");
            map.put(split[0], split[1]);
        }
    }

    private static byte[] readInputStream(InputStream stream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = stream.read(buffer)) != -1;) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }
}