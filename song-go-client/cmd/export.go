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
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

func init() {
	RootCmd.AddCommand(exportCmd)
}

func export(analysisIds []string) {
	studyID := viper.GetString("study")

	client := createClient()
	var responseBody string
	if len(analysisIds) == 0 {
		responseBody = client.ExportStudy(studyID)
	} else {
		responseBody = client.ExportAnalyses(analysisIds)
	}

	var formattedJson bytes.Buffer
	if err := json.Indent(&formattedJson, []byte(responseBody), "", "  "); err != nil {
		panic("Response from server is not a valid json string")
	}

	fmt.Println(formattedJson.String())
}

var exportCmd = &cobra.Command{
	Use:   "export [analysisId]...",
	Short: "Export payloads",
	Long:  `Export analysis or study payloads in json format`,
	Args:  cobra.MinimumNArgs(0),
	Run: func(cmd *cobra.Command, args []string) {
		export(args)
	},
}
