package org.crown.web.rest;

import com.azure.core.annotation.Get;
import io.github.jhipster.web.util.HeaderUtil;
import org.crown.repository.BlobStorageRepository;
import org.crown.web.rest.errors.BadRequestAlertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api")
public class DocumentResource {
    private static final String ENTITY_NAME = "document";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private BlobStorageRepository blobStorageRepository;

    @Autowired
    public DocumentResource(BlobStorageRepository blobStorageRepository) {
        this.blobStorageRepository = blobStorageRepository;
    }

    @PostMapping("/document/upload")
    public ResponseEntity uploadDocument(@NotEmpty @RequestParam("entity") String entity,
                                         @NotEmpty @RequestParam("entityId") String entityId,
                                         @RequestParam("file") MultipartFile file) throws BadRequestAlertException, URISyntaxException {
        try {
            blobStorageRepository.createBlob(entity, entityId, file.getOriginalFilename(), file.getInputStream(), file.getSize());
        } catch (IOException io) {
            throw new BadRequestAlertException("Error uploading file", entity + "-" + ENTITY_NAME, "idnull");
        }

        return ResponseEntity.created(new URI("/api/documents")).
            headers(HeaderUtil.createEntityCreationAlert(applicationName, true, entity + "-" + ENTITY_NAME, "")).
            build();
    }

    @GetMapping("documents/{entity}/{entityId")
    public ResponseEntity getDocuments(@PathVariable String entity, @PathVariable String entityId) {
        blobStorageRepository.listBlobs(entity, entityId);

        return ResponseEntity.ok().build();
    }
}
