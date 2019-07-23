package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private BlobStore store;

    public AlbumsController (AlbumsBean albumsBean, BlobStore blobStore){
        this.albumsBean = albumsBean;
        this.store = blobStore;
    }

    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        Blob blob = new Blob(String.valueOf(albumId),uploadedFile.getInputStream(), uploadedFile.getContentType());
        store.put(blob);
        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Optional<Blob> blob = store.get(String.valueOf(albumId));

        byte[] imageBytes = null;
        String contentType = null;
        if(blob.isPresent()) {

            contentType = blob.get().contentType;
            imageBytes =IOUtils.toByteArray(blob.get().inputStream);
        }
        else {
            imageBytes = getExistingCoverContent();
        }
        HttpHeaders headers = createImageHttpHeaders(contentType,imageBytes);
        return new HttpEntity<>(imageBytes, headers);
    }


    private HttpHeaders createImageHttpHeaders(String contentType, byte[] imageBytes) throws IOException {
        if (contentType == null)
        {
            contentType = "image/jpeg";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;

    }



    private byte [] getExistingCoverContent() throws IOException {


            InputStream is = this.getClass().getClassLoader().getResourceAsStream("default-cover.jpg");

        return IOUtils.toByteArray(is);
    }
}
