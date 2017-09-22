package org.icgc.dcc.song.importer.dao.dcc.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.icgc.dcc.song.importer.config.DccMetadataConfig;
import org.icgc.dcc.song.importer.convert.DccMetadataConverter;
import org.icgc.dcc.song.importer.dao.dcc.DccMetadataDao;
import org.icgc.dcc.song.importer.dao.dcc.DccMetadataQueryBuilder;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@Slf4j
@Component
public class DccMetadataDbDao implements Closeable, DccMetadataDao {

  private DccMetadataQueryBuilder dccMetadataQueryBuilder;

  private final MongoClient mongoClient;
  private final MongoCollection<Document> mongoCollection;

  public DccMetadataDbDao(@NonNull DccMetadataConfig dccMetadataConfig,
      @NonNull DccMetadataQueryBuilder dccMetadataQueryBuilder) {
    val mongoClientURI = dccMetadataConfig.getMongoClientURI();
    this.mongoClient = new MongoClient(mongoClientURI);
    val db = mongoClient.getDatabase(dccMetadataConfig.getName());
    this.mongoCollection = db.getCollection(dccMetadataConfig.getCollection());
    this.dccMetadataQueryBuilder = dccMetadataQueryBuilder;
  }

  @Override
  public Optional<DccMetadata> findByObjectId(@NonNull String objectId){
    val query = dccMetadataQueryBuilder.buildIdQuery(objectId);
    return stream(mongoCollection.find(query).iterator())
        .map(DccMetadataConverter::convertToDccMetadata)
        .findFirst();
  }

  @Override
  public Set<DccMetadata> findByMultiObjectIds(@NonNull Set<String> objectIds){
    val query = dccMetadataQueryBuilder.buildMultiIdsQuery(objectIds);
    return stream(mongoCollection.find(query).iterator())
        .map(DccMetadataConverter::convertToDccMetadata)
        .collect(toImmutableSet());
  }

  @Override
  public void close() throws IOException {
    if (mongoClient != null){
      this.mongoClient.close();
    }
  }

}
