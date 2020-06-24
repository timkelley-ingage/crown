package org.crown.repository;

import java.io.InputStream;

public interface BlobStorageRepository {
    void createBlob(String entity, String entityId, String name, InputStream data, long length);
    void listBlobs(String entity, String entityId);
}
