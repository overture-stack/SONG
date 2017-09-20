package org.icgc.dcc.song.importer.config;

import com.mongodb.MongoClientURI;
import lombok.Getter;
import lombok.val;
import org.icgc.dcc.song.importer.dao.DccMetadataDbDao;
import org.icgc.dcc.song.importer.dao.DccMetadataQueryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy
@Getter
public class DccMetadataConfig {

  @Value("${dcc-metadata.db.host}")
  private String host;

  @Value("${dcc-metadata.db.name}")
  private String name;

  @Value("${dcc-metadata.db.collection}")
  private String collection;

  @Value("${dcc-metadata.db.username}")
  private String username;

  @Value("${dcc-metadata.db.password}")
  private String password;

  @Bean
  public DccMetadataDbDao dccMetadataDbDao(){
    val queryBuilder = new DccMetadataQueryBuilder();
    return new DccMetadataDbDao(this, queryBuilder);
  }


  public MongoClientURI getMongoClientURI(){
    return new MongoClientURI(String.format("mongodb://%s:%s@%s/%s", username,password,host, name));
  }

}
