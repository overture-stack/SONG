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

package org.icgc.dcc.song.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.icgc.dcc.common.core.util.Joiners;

import java.util.function.Supplier;

import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;

@Slf4j
public abstract class EntityController {
  // TODO: Have Entity Controllers (not Upload) extend from this abstract class

  protected static void checkRequest(boolean errorCondition, String formatTemplate, Object... args) {
    if (errorCondition) {
      // We don't want exception within an exception-handling routine.
      final Supplier<String> errorMessageProvider = () -> {
        try {
          return format(formatTemplate, args);
        } catch (Exception e) {
          final String errorDetails = "message: '" + formatTemplate +
              "', parameters: '" + COMMA.join(args) + "'";
          log.error("Error while formatting message - " + errorDetails, e);

          return "Invalid web request - " + errorDetails;
        }
      };

      // TODO: @Robert, use your new exception here.
      // throw new BadRequestException(errorMessageProvider.get());
    }
  }

}
