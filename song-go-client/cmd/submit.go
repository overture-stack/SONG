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
	"io/ioutil"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

func init() {
	RootCmd.AddCommand(submitCmd)
}

func submit(filePath string) {
	studyID := viper.GetString("study")

	// read the file
	b, err := ioutil.ReadFile(filePath)
	if err != nil {
		fmt.Print(err)
	}

	if len(b) < 1 {
		panic("File does not have any content!")
	}

	client := createClient()
	responseBody := client.Submit(studyID, b)
	fmt.Println(string(responseBody))
}

var submitCmd = &cobra.Command{
	Use:   "submit <filename>",
	Short: "Submit Analysis Metadata",
	Long:  `Submit Metadata JSON describing an analysis and files for validation`,
	Args:  cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		submit(args[0])
	},
}
