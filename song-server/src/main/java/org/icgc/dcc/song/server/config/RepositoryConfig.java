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
package org.icgc.dcc.song.server.config;

import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.repository.InfoRepository;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.icgc.dcc.song.server.repository.UploadRepository;
import org.icgc.dcc.song.server.repository.mapper.InfoSearchResponseMapper;
import org.icgc.dcc.song.server.repository.search.SearchRepository;
import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.sql.DataSource;

@Lazy
@Configuration
public class RepositoryConfig {

  @Autowired
  private DataSource dataSource;

  @Bean
  public DBI dbi() {
    return new DBI(dataSource);
  }

  @Bean
  public StudyRepository studyRepository(DBI dbi) {
    return dbi.open(StudyRepository.class);
  }

  @Bean
  public DonorRepository donorRepository(DBI dbi) {
    return dbi.open(DonorRepository.class);
  }

  @Bean
  public SpecimenRepository SpecimenRepository(DBI dbi) {
    return dbi.open(SpecimenRepository.class);
  }

  @Bean
  public SampleRepository SampleRepository(DBI dbi) {
    return dbi.open(SampleRepository.class);
  }

  @Bean
  public FileRepository FileRepository(DBI dbi) {
    return dbi.open(FileRepository.class);
  }

  @Bean
  public UploadRepository statusRepository(DBI dbi) {
    return dbi.open(UploadRepository.class);
  }

  @Bean
  public AnalysisRepository AnalysisRepository(DBI dbi) {
    return dbi.open(AnalysisRepository.class);
  }

  @Bean
  public InfoRepository InfoRepository(DBI dbi) { return dbi.open(InfoRepository.class);}

  @Bean
  public SearchRepository searchRepository(DBI dbi){
    return new SearchRepository(dbi.open());
  }

}
