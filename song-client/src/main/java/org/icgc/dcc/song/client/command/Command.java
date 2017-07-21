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
package org.icgc.dcc.song.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;

import org.icgc.dcc.song.client.cli.Status;

import lombok.Data;

import java.io.IOException;

/**
 * Abstract parent class for Command objects.
 */
@Data
public abstract class Command {

  private Status status = new Status();

  /***
   * Convenience method for children to save error message
   * 
   * @param format See String.format
   * @param args
   * 
   * Formats a string and adds it to the output for the command
   */
  public Status err(String format, Object... args) {
    status.err(format, args);
    return status;
  }

  /***
   * Convenience method for child classes to save output message
   * 
   * @param format See String.format
   * @param args
   * 
   * Formats a string and adds it to the error message for the command
   */
  public void output(String format, Object... args) {
    status.output(format, args);
  }

  public void save(Status status) {
    this.status.save(status);
  }

  public void report() {
    status.report();
  }

  /***
   * Require all of our children to define a "run" method.
   */
  public abstract void run();

  @SneakyThrows
  public JsonNode getJson() {
    val mapper = new ObjectMapper();

    val json = mapper.readTree(System.in);

    return json;
  }


}
