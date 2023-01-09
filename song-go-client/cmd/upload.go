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

var asyncFlag bool

func init() {
	RootCmd.AddCommand(uploadCmd)
	uploadCmd.Flags().BoolVarP(&asyncFlag, "async", "a", false, "Upload asynchronously")
}

func upload(filePath string) {
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
	responseBody := client.Upload(studyID, b, asyncFlag)
	fmt.Println(string(responseBody))
}

var uploadCmd = &cobra.Command{
	Use:   "upload <filename>",
	Short: "Upload Analysis Metadata",
	Long:  `Uploads Metadata JSON describing an analysis and files for validation`,
	Args:  cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		upload(args[0])
	},
}
