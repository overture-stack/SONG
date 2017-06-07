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
 *
 */
package org.icgc.dcc.song.server.service;

import static org.icgc.dcc.song.server.model.enums.IdPrefix.File;

import java.util.List;

import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;
import lombok.val;

@Service
@NoArgsConstructor
public class FileService {

  @Autowired
  FileRepository repository;
  @Autowired
  IdService idService;

  public String create(String parentId, File f) {
    val id = idService.generate(File);
    f.setObjectId(id);
    f.setSampleId(parentId);

    int status = repository.create(f);

    if (status != 1) {
      return "error: Can't create" + f.toString();
    }

    return "ok:" + id;
  }

  public File read(String id) {
    return repository.read(id);
  }

  public List<File> readByParentId(String parentId) {
    return repository.readByParentId(parentId);
  }

  public String update(File f) {
    repository.update(f);
    return "ok";
  }

  public String delete(String id) {
    repository.delete(id);
    return "ok";
  }

  public String save(String studyId, File f) {
    String fileId = repository.findByBusinessKey(studyId, f.getFileName());
    if (fileId == null) {
      fileId = idService.generate(IdPrefix.File);
      f.setObjectId(fileId);
      repository.create(f);
    } else {
      repository.update(f);
    }
    return fileId;
  }

}
