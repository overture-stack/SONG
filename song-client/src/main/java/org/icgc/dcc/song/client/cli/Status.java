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
package org.icgc.dcc.song.client.cli;

import lombok.Data;
import lombok.NonNull;
import org.fusesource.jansi.AnsiConsole;

import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * This class holds status results for commands that have run.
 */
@Data
public class Status {

  private String errors;
  private String outputs;

  public Status() {
    this.errors = "";
    this.outputs = "";
  }

  void err(String s) {
    errors += s;
  }

  void output(String s) {
    outputs += s;
  }

  public void save(@NonNull Status s) {
    errors += s.errors;
    outputs += s.outputs;
  }

  public boolean isOk() {
    return errors.equals("");
  }

  public boolean hasOutputs() {
    return !outputs.equals("");
  }

  public boolean hasErrors() {
    return !isOk();
  }

  public void err(String format, Object... args) {
    err(String.format(format, args));
  }

  public void output(String format, Object... args) {
    if (args.length == 0) {
      outputs += format;
    } else {
      outputs += String.format(format, args);
    }
  }

  public void reportErrors() {
    if (! "".equals(errors)){
      AnsiConsole.err().println(
          ansi()
              .eraseLine()
              .fg(RED)
              .a(errors)
              .reset());
    }
  }

  public void reportOutput() {
    if (! "".equals(outputs)){
      AnsiConsole.out().println(
          ansi()
              .eraseLine()
              .fg(GREEN)
              .a(outputs)
              .reset());
    }
  }

  public void report() {
    reportOutput();
    reportErrors();
  }

}
