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
package org.icgc.dcc.sodalite.client.register;

import static java.lang.String.format;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.val;

/**
 * 
 */
public class Endpoint {

  private String serverUrl;

  @Autowired
  Endpoint(String url) {
    serverUrl = url;
  }

  String analysis(String studyId, String uploadId, String analysisType) {
    return format("%s/studies/%s/analyses/%s/%s", serverUrl, studyId, analysisType, uploadId);
  }

  String publishAll(String studyId) {
    return format("%s/studies/%s/func/publish", serverUrl, studyId);
  }

  public String publishById(String studyId, String uploadId) {
    return publishAll(studyId) + "/" + uploadId;
  }

  String status(String studyId, String uploadId) {
    val url = "%s/studies/%s/statuses/%s";
    return format(url, serverUrl, studyId, uploadId);
  }
}
