/*
 *     Copyright (C) 2018  Ontario Institute for Cancer Research
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package song

import (
	"encoding/json"
	"net/http"
	"net/url"
	"time"
)

// Client struct allowing for making REST calls to a SONG server
type Client struct {
	accessToken string
	httpClient  *http.Client
	endpoint    *Endpoint
}

// CreateClient is a Factory Function for creating and returning a SONG client
func CreateClient(accessToken string, base *url.URL) *Client {
	tr := &http.Transport{
		MaxIdleConns:    10,
		IdleConnTimeout: 30 * time.Second,
	}
	httpClient := &http.Client{Transport: tr}
	songEndpoints := &Endpoint{base}

	client := &Client{
		accessToken: accessToken,
		endpoint:    songEndpoints,
		httpClient:  httpClient,
	}

	return client
}

// Upload uploads the file contents and returns the response
func (c *Client) Upload(studyID string, byteContent []byte, async bool) string {
	return c.post(c.endpoint.Upload(studyID, async), byteContent)
}

// GetStatus return the status JSON of an uploadID
func (c *Client) GetStatus(studyID string, uploadID string) string {
	return c.get(c.endpoint.GetStatus(studyID, uploadID))
}

// GetServerStatus return whether server is alive
func (c *Client) GetServerStatus() string {
	return c.get(c.endpoint.IsAlive())
}

// Save saves the specified uploadID as an analysis assuming it had passed validation
func (c *Client) Save(studyID string, uploadID string, ignoreCollisions bool) string {
	return c.post(c.endpoint.Save(studyID, uploadID, ignoreCollisions), nil)
}

// Publish publishes a specified saved analysisID
func (c *Client) Publish(studyID string, analysisID string) string {
	return c.put(c.endpoint.Publish(studyID, analysisID), nil)
}

// Suppress supress an analysis
func (c *Client) Suppress(studyID string, analysisID string) string {
	return c.put(c.endpoint.Suppress(studyID, analysisID), nil)
}

func (c *Client) getAnalysis(studyID string, analysisID string) string {
	return c.get(c.endpoint.GetAnalysis(studyID, analysisID))
}

func (c *Client) getAnalysisFiles(studyID string, analysisID string) string {
	return c.get(c.endpoint.GetAnalysisFiles(studyID, analysisID))
}

// IdSearch search id
func (c *Client) IdSearch(studyID string, ids map[string]string) string {
	searchTerms, err := json.Marshal(ids)
	if err != nil {
		panic(err)
	}
	return c.post(c.endpoint.IdSearch(studyID), searchTerms)
}

// InfoSearch search info
func (c *Client) InfoSearch(studyID string, includeInfo bool, terms map[string]string) string {
	searchRequest := createInfoSearchJSON(includeInfo, terms)
	return c.post(c.endpoint.InfoSearch(studyID), searchRequest)
}

// Manifest search info
func (c *Client) Manifest(studyID string, analysisID string) string {
	var data = c.getAnalysisFiles(studyID, analysisID)
	manifest := createManifest(analysisID, data)
	return manifest
}

// ExportStudy export study
func (c *Client) ExportStudy(studyID string) string {
	return c.get(c.endpoint.ExportStudy(studyID))
}

// ExportAnalyses export analyses
func (c *Client) ExportAnalyses(analysisIds []string) string {
	return c.get(c.endpoint.ExportAnalyses(analysisIds))
}

// UpdateFile update file metadata
func (c *Client) UpdateFile(studyID string, fileID string, data []byte) string {
	return c.put(c.endpoint.UpdateFile(studyID, fileID), data)
}
