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
	"fmt"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"io/ioutil"
)

func init() {
	RootCmd.AddCommand(manifestCmd)
}

func manifest(analysisID string, filePath string) {
	client := createClient()
	studyID := viper.GetString("study")
	responseBody := client.Manifest(studyID, analysisID)

	// read the file
	err := ioutil.WriteFile(filePath, []byte(responseBody), 0644)
	if err != nil {
		fmt.Print(err)
	}
}

var manifestCmd = &cobra.Command{
	Use:   "manifest <analysisID> <filename>",
	Short: "Upload Analysis Metadata",
	Long:  `Uploads Metadata JSON describing an analysis and files for validation`,
	Run: func(cmd *cobra.Command, args []string) {
		manifest(args[0], args[1])
	},
}
