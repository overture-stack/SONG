/*
 * Copyright (c) 2019 The Ontario Institute for Cancer Research. All rights reserved.
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
package bio.overture.song.core.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;

/** Joiners and Splitters for commonly used separators. */
public enum Separators {
  TAB("\t"),
  NEWLINE("\n"),
  DOT("."),
  COMMA(","),
  DASH("-"),
  UNDERSCORE("_"),
  SLASH("/"),
  WHITESPACE(" "),
  COLON(":"),
  SEMICOLON(";"),
  HASHTAG("#"),
  DOLLAR("$"),
  PIPE("|"),
  AMPERSAND("&"),
  PATH(System.lineSeparator());

  @Getter private String separator;
  private Splitter splitter;
  private Joiner joiner;

  Separators(@NonNull String separator) {
    this.separator = separator;
    splitter = Splitter.on(separator);
    joiner = Joiner.on(separator);
  }

  public Iterable<String> split(CharSequence sequence) {
    return splitter.split(sequence);
  }

  public List<String> splitToList(CharSequence sequence) {
    return splitter.splitToList(sequence);
  }

  public String join(Iterable<?> s) {
    return joiner.join(s);
  }

  public String join(Iterator<?> s) {
    return joiner.join(s);
  }

  public String join(Object... strings) {
    return joiner.join(strings);
  }
};
