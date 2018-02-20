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
package org.icgc.dcc.song.importer;

import lombok.Getter;
import org.icgc.dcc.song.importer.download.DownloadIterator;
import org.icgc.dcc.song.importer.filters.impl.SpecimenFileFilter;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.storage.SimpleDccStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Lazy
public class Config {

  @Autowired
  private SimpleDccStorageClient simpleDccStorageClient;

  @Autowired
  private DownloadIterator<DccMetadata> dccMetadataDownloadIterator;

  @Autowired
  private DownloadIterator<PortalFileMetadata> portalFileMetadataDownloadIterator;

  @Autowired
  private SpecimenFileFilter specimenFileFilter;

  @Getter
  @Value("${importer.updateMatchedNormalSubmitterSamples}")
  private Boolean updateMatchedNormalSubmitterSamples;

  @Getter
  @Value("${importer.disableSSL}")
  private boolean disableSSL;

  public static final String PORTAL_API = "https://dcc.icgc.org";
  public static final Path PERSISTED_DIR_PATH = Paths.get("persisted");
  public static final String DATA_CONTAINER_PERSISTENCE_FN = "dataContainer.dat";


  @Bean
  public Factory factory(){
    return new Factory(simpleDccStorageClient, dccMetadataDownloadIterator, portalFileMetadataDownloadIterator, specimenFileFilter);
  }

}
