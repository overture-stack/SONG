/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.sodalite.server.repository.alternative;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.icgc.dcc.sodalite.server.utils.JsonUtils;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Update;
import org.skife.jdbi.v2.util.StringMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.SneakyThrows;
import lombok.val;

class SqlEntityRepository extends EntityRepository {

  @Autowired
  Handle h;
  boolean parentsExist;

  Update create;
  Update update;
  Update delete;

  List<String> fields;
  private String tableName;
  private String parentIdField;

  SqlEntityRepository(Handle h, String tableName, String parentIdField, List<String> fields) {
    this.h = h;
    this.tableName = tableName;
    this.parentIdField = parentIdField;
    this.fields = fields;
    val insert = sqlCmdCreate(tableName, parentIdField, fields);
    create = h.createStatement(insert);
  }

  private boolean parentsExist() {
    return parentIdField == null;
  }

  String sqlCmdCreate(String tableName, String parentIdField, List<String> fields) {
    String insert = "INSERT INTO " + tableName + " (id, studyId, ";
    if (parentsExist()) {
      insert += parentIdField;
    }
    insert += fieldList() + ") VALUES (?, ?";
    if (parentsExist) {
      insert += " ,?";
    }
    insert += bindings() + ")";
    return insert;
  }

  @Override
  public void create(String id, String parentId, String studyId, JsonNode data) {
    int i = 0;
    this.create.bind(i++, id);
    this.create.bind(i++, studyId);

    if (parentsExist) {
      this.create.bind(i++, parentId);
    }

    for (val f : fields) {
      this.create.bind(i++, data.get(f).asText());
    }

    create.execute();

  }

  private String bindings() {
    String s = "";
    for (int i = 1; i <= fields.size(); i++) {
      s += " ,?";
    }
    return s;
  }

  private String fieldList() {
    return fields.stream().collect(Collectors.joining(","));
  }

  @SneakyThrows
  @Override
  public JsonNode read(String id) {
    val rs = h.select("select * from " + tableName + " WHERE ID = " + id);
    val row = rs.get(0);

    String json = "{";
    if (parentsExist) {
      json += JsonUtils.jsonResponse(parentIdField, row.get(parentIdField).toString());
    }
    for (val f : fields) {
      json += JsonUtils.jsonResponse(f, row.get(f).toString());
    }
    json += "}";
    return JsonUtils.getTree(json);
  }

  @Override
  public void update(String id, JsonNode data) {
    String sql = "UPDATE " + tableName + "SET ";
    boolean first = true;
    for (val f : fields) {
      sql += f;
      sql += "=";
      sql += data.get(f).asText();
      if (!first) {
        sql += ",";
      }
      first = false;
    }
    sql += " WHERE id=?";
    h.execute(sql, id);
  }

  @Override
  public void delete(String id) {
    h.execute("update " + tableName + "SET deleted=? WHERE ID=?", true, id);
  }

  @Override
  public String findByBusinessKey(String studyId, String key) {
    String sql = "SELECT id FROM " + tableName + "WHERE study_id=? AND submitter_id=?";
    val query = h.createQuery(sql);
    query.bind(0, studyId);
    query.bind(1, key);
    val strings = query.map(StringMapper.FIRST).list();
    if (strings.isEmpty()) {
      return null;
    }
    return strings.get(0);
  }

  @Override
  public Collection<String> findByParent(String parentId) {
    String sql = "SELECT id FROM " + tableName + "WHERE " + parentIdField + "=?";
    val query = h.createQuery(sql);
    query.bind(0, parentId);
    val strings = query.map(StringMapper.FIRST).list();
    return strings;
  }

}