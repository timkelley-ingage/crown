package org.crown.repository.azure;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.crown.repository.BlobStorageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AzureBlobStorageRepository implements BlobStorageRepository {
    @Value("${azure.endpoint}")
    private String endpoint;
    @Value("${azure.accountName}")
    private String accountName;
    @Value("${azure.accountKey}")
    private String accountKey;
    @Value("${azure.containerNamePrefix}")
    private String containerNamePrefix;

    public void createBlob(String entity, String entityId, String name, InputStream data, long length) {
        BlobServiceClient blobServiceClient = createBlobServiceClient();

        /* Create a new container client */
        try {
            String containerName = String.format("%s-%s-container", containerNamePrefix, entity);
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            /* Upload the file to the container */
            String blobName = String.format("%s-%s", entityId, name);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("entityId", entityId);
            blobClient.setMetadata(metadata);
            blobClient.upload(data, length);
        } catch (BlobStorageException ex) {
            throw ex;
        }
    }

    @Override
    public void listBlobs(String entity, String entityId) {
        BlobServiceClient blobServiceClient = createBlobServiceClient();

        String containerName = String.format("%s-%s-container", containerNamePrefix, entity);
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        PagedIterable<BlobItem> items = containerClient.listBlobs();
        List entityBlobs = new ArrayList<>();
        items.forEach(blobItem -> {
            if (blobItem.getMetadata().get("entityId") == entityId) {
                entityBlobs.add(blobItem);
            }
        });
    }

    private BlobServiceClient createBlobServiceClient() {
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        /* Create a new BlobServiceClient with a SAS Token */
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();
        return blobServiceClient;
    }
}
