package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;


import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;


public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        String blobname= blob.name;
        File targetFile = getCoverFile(blobname);

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(IOUtils.toByteArray(blob.inputStream));
        }

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        // Get the blob from the store given the name
        // name = album Id

        InputStream inputStream;


        File coverFile = getCoverFile(name);
        Path coverFilePath = null;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {

                try {
                    coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

        }

        inputStream = new FileInputStream(coverFile);
        Blob blob = new Blob(name,inputStream, new Tika().detect(coverFilePath));

        return Optional.of(blob);

    }


    private File getCoverFile(String albumId) {
        String coverFileName = format("covers/%s", albumId);
        return new File(coverFileName);
    }



    @Override
    public void deleteAll() {
        // Delete all blobs from the store
    }
}