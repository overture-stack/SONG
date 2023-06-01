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
	"net/url"
	"testing"

	"github.com/stretchr/testify/assert"
)

func createEndpoint(address string) Endpoint {
	a, err := url.Parse(address)
	if err != nil {
		panic(err)
	}
	return Endpoint{a}
}

func TestSubmit(t *testing.T) {
	e := createEndpoint("http://test.com")
	studyId := "ABC123"

	x := e.Submit(studyId)
	assert.Equal(t, x.String(), "http://test.com/submit/ABC123", "Upload()")
}

func TestIsAlive(t *testing.T) {
	e := createEndpoint("https://www.catfur.org")
	x := e.IsAlive()
	assert.Equal(t, x.String(), "https://www.catfur.org/isAlive", "IsAlive()")
}

func TestPublish(t *testing.T) {
	e := createEndpoint("http://example.org:12345")
	studyId, analysisId := "XQA-𝜆123", "A2345-999-7012"
	x := e.Publish(studyId, analysisId)
	assert.Equal(t, x.String(), "http://example.org:12345/studies/XQA-%F0%9D%9C%86123/analysis/publish/A2345-999-7012",
		"Publish()")
}

func TestUnpublish(t *testing.T) {
	e := createEndpoint("http://example.org:12345")
	studyId, analysisId := "XQA-𝜆123", "A2345-999-7012"
	x := e.Unpublish(studyId, analysisId)
	assert.Equal(t, x.String(), "http://example.org:12345/studies/XQA-%F0%9D%9C%86123/analysis/unpublish/A2345-999-7012",
		"Unpublish()")
}

func TestSuppress(t *testing.T) {
	e := createEndpoint("http://www.testing.com")
	studyId, analysisId := "ABC123", "AN-123579"
	x := e.Suppress(studyId, analysisId)
	assert.Equal(t, x.String(), "http://www.testing.com/studies/ABC123/analysis/suppress/AN-123579", "Suppress()")
}

func TestGetAnalysis(t *testing.T) {
	e := createEndpoint("http://abc.de")
	studyId, analysisId := "ABC123", "AN-123579"
	x := e.GetAnalysis(studyId, analysisId)
	assert.Equal(t, x.String(), "http://abc.de/studies/ABC123/analysis/AN-123579", "GetAnalysis()")
}

func TestGetAnalysisFiles(t *testing.T) {
	e := createEndpoint("https://localhost:8080")
	studyId, analysisId := "XYZ2345", "13"
	x := e.GetAnalysisFiles(studyId, analysisId)
	assert.Equal(t, x.String(), "https://localhost:8080/studies/XYZ2345/analysis/13/files", "GetAnalysisFiles()")
}

func TestIdSearch(t *testing.T) {
	e := createEndpoint("http://abc.de:123")
	studyId := "ABC123"
	x := e.IdSearch(studyId)
	assert.Equal(t, x.String(), "http://abc.de:123/studies/ABC123/analysis/search/id", "IdSearch()")
}

func TestInfoSearch(t *testing.T) {
	e := createEndpoint("http://xyz.ai:23")
	studyId := "XYZ2345"
	x := e.InfoSearch(studyId)
	assert.Equal(t, x.String(), "http://xyz.ai:23/studies/XYZ2345/analysis/search/info", "InfoSearch()")
}

func TestExportStudy(t *testing.T) {
	e := createEndpoint("http://xyz.ai:23")
	studyId := "XYZ2345"
	x := e.ExportStudy(studyId)
	assert.Equal(t, x.String(), "http://xyz.ai:23/export/studies/XYZ2345", "ExportStudy()")
}

func TestExportAnalyses(t *testing.T) {
	e := createEndpoint("http://xyz.ai:23")
	analysisIds := []string{"1", "2"}
	x := e.ExportAnalyses(analysisIds)
	assert.Equal(t, x.String(), "http://xyz.ai:23/export/analysis/1,2", "ExportAnalyses()")
}

func TestUpdateFile(t *testing.T) {
	e := createEndpoint("http://xyz.ai:23")
	studyID := "123"
	fileID := "456"
	x := e.UpdateFile(studyID, fileID)
	assert.Equal(t, x.String(), "http://xyz.ai:23/studies/123/files/456", "TestUpdateFile()")
}
