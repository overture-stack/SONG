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
package org.icgc.dcc.sodalite.client.command;

import java.io.File;
import java.io.IOException;

import org.icgc.dcc.sodalite.client.config.SodaliteConfig;
import org.icgc.dcc.sodalite.client.register.Registry;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import lombok.NonNull;
import lombok.val;

public class RegisterCommand extends Command {
  @Override
  public void run(SodaliteConfig config) {
    if (getArgs().length < 3) {
      err("Usage: sodalite-client register <uploadID> <file>");
      return;
    }
    @NonNull val file = new File(getArgs()[2]);
    String json;
    try {
      json = Files.toString(file, Charsets.UTF_8);
    } catch (IOException e) {
      err("Error: Can't open file '%s'", file);
      return;
    }

    val registry = new Registry(config);
    String uploadId = getArgs()[1];

    String result = registry.registerAnalysis(uploadId, json);
    output(result);

  }

}
