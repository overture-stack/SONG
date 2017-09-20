package org.icgc.dcc.song.importer.dao.dcc.impl;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@Slf4j
@Component
public class DccMetadataDbDao implements Closeable, DccMetadataDao {

  private final MongoClientURI mongoClientURI;
  private final MongoClient mongoClient;
  private final MongoCollection<Document> mongoCollection;
  private final DccMetadataQueryBuilder dccMetadataQueryBuilder;

  @Autowired
  public DccMetadataDbDao(@NonNull DccMetadataConfig dccMetadataConfig,
      @NonNull DccMetadataQueryBuilder dccMetadataQueryBuilder) {
    this.mongoClientURI = dccMetadataConfig.getMongoClientURI();
    this.mongoClient = new MongoClient(this.mongoClientURI);
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
  public List<DccMetadata> findByMultiObjectIds(@NonNull Collection<String> objectIds){
    val query = dccMetadataQueryBuilder.buildMultiIdsQuery(objectIds);
    return stream(mongoCollection.find(query).iterator())
        .map(DccMetadataConverter::convertToDccMetadata)
        .collect(toImmutableList());
  }

  @Override
  public void close() throws IOException {
    if (mongoClient != null){
      this.mongoClient.close();
    }
  }

}
