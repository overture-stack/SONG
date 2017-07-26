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
package org.icgc.dcc.song.importer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;

import static org.icgc.dcc.common.core.util.Joiners.NEWLINE;
import static org.springframework.boot.Banner.Mode.CONSOLE;

/**
 * Application entry point.
 */
@Slf4j
@SpringBootApplication
@Configuration
@RequiredArgsConstructor
public class ClientMain implements CommandLineRunner {

  @Autowired private Importer importer;

  @Override public void run(String... strings) throws Exception {
    try{
      log.info("Importer Started");
      importer.run();
      log.info("Importer finished");
    } catch (Throwable e){
      log.error("Failed to run Importer with exception [{}] : [Message] -- {}:\n{} ",
          e.getClass().getSimpleName(), e.getMessage(), NEWLINE.join(e.getStackTrace()));
    }
  }

  public static void main(String... args) {
    val app = new SpringApplicationBuilder(ClientMain.class)
        .bannerMode(CONSOLE)
        .web(false)
        .logStartupInfo(false)
        .registerShutdownHook(true)
        .addCommandLineProperties(true)
        .build();
    app.run(args);


  }

}
