package org.icgc.dcc.song.importer.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import lombok.val;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class DccMetadataQueryBuilder {

  private static final String _ID = "_id";
  private static final String IN = "$in";

  public BasicDBObject buildMultiIdsQuery(List<String> ids){
    return idObject(inQuery(ids));
  }

  public BasicDBObject buildMultiIdsQuery(String ... ids){
    return buildMultiIdsQuery(newArrayList(ids));
  }

  public BasicDBObject buildIdQuery(String id){
    return idObject(id);
  }

  public static BasicDBObject object(String key, Object obj ){
    val q = new BasicDBObject();
    q.put(key, obj);
    return q;
  }

  public static <T> BasicDBList array(Collection<T> objects){
    val q = new BasicDBList();
    q.addAll(objects);
    return q;
  }

  public static BasicDBList array(Object... objects ){
    return array(newArrayList(objects));
  }

  private static BasicDBObject idObject(Object obj){
    return object(_ID, obj);
  }

  private static <T> BasicDBObject inQuery(Iterable<T> objects ){
    val list = new BasicDBList();
    objects.forEach(list::add);
    val query = new BasicDBObject();
    query.put(IN, list);
    return query;
  }

}
