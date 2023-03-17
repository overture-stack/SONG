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

package cmd

import (
	"bytes"
	"encoding/json"
	"fmt"
	"strings"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var searchTerms []string
var includeInfo bool

var analysisId, donorId, fileId, sampleId, specimenId string

func init() {
	RootCmd.AddCommand(searchCmd)
	searchCmd.Flags().StringArrayVarP(&searchTerms, "search-terms", "t", []string{}, "List of seach terms")
	searchCmd.Flags().BoolVarP(&includeInfo, "info", "n", false, "Include info field")
	searchCmd.Flags().StringVarP(&analysisId, "analysis-id", "a", "", "AnalysisID to match")
	searchCmd.Flags().StringVarP(&donorId, "donor-id", "d", "", "DonorID to match")
	searchCmd.Flags().StringVarP(&fileId, "file-id", "f", "", "FileID to match")
	searchCmd.Flags().StringVarP(&sampleId, "sample-id", "m", "", "SampleID to match")
	searchCmd.Flags().StringVarP(&specimenId, "specimen-id", "p", "", "Specimen ID to match")
}

func getIds() map[string]string {
	var ids map[string]string = map[string]string{}

	ids["analysisId"] = analysisId
	ids["donorId"] = donorId
	ids["specimenId"] = specimenId
	ids["sampleId"] = sampleId
	ids["fileId"] = fileId

	return ids
}

func getTerms() map[string]string {
	ids := map[string]string{}
	for _, t := range searchTerms {
		x := strings.SplitN(t, "=", 2)
		if len(x) < 2 {
			fmt.Printf("Search term '%s' has no = sign; skipping it...\n", t)
		} else {
			ids[x[0]] = x[1]
		}
	}
	return ids
}

func search() {
	// init song client
	studyID := viper.GetString("study")
	client := createClient()

	// -a analysis-id, -d donor-id, -f file-id, -sa -sample-id,
	// -sp specimen-id
	// -t search-terms ([]), -i info(false)
	var responseBody string

	if len(analysisId) > 0 {
		responseBody = client.GetAnalysis(studyID, analysisId)
	} else if len(searchTerms) > 0 {
		responseBody = client.InfoSearch(studyID, includeInfo, getTerms())
	} else {
		responseBody = client.IdSearch(studyID, getIds())
	}

	var formattedJson bytes.Buffer
	if err := json.Indent(&formattedJson, []byte(responseBody), "", "  "); err != nil {
		panic("Response from server is not a valid json string")
	}

	fmt.Println(formattedJson.String())
}

var searchCmd = &cobra.Command{
	Use:   "search [options]",
	Short: "Search for an Analysis",
	Long:  `Search for an analysis`,
	Run: func(cmd *cobra.Command, args []string) {
		search()
	},
}
