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
	"strconv"
)

type FileMetaData struct {
	FileAccess string      `json:"fileAccess,omitempty"`
	Md5        string      `json:"fileMd5sum,omitempty"`
	Size       *int        `json:"fileSize,omitempty"`
	Info       interface{} `json:"info,omitempty"`
}

type SizeFlag struct{}

func (s SizeFlag) String() string { return "" }

func (s SizeFlag) Type() string { return "int" }

func (s *SizeFlag) Set(str string) error {
	if i, err := strconv.Atoi(str); err != nil {
		return err
	} else {
		fileMetaData.Size = &i
	}
	return nil
}

type InfoFlag struct{}

func (i InfoFlag) String() string { return "" }

func (i InfoFlag) Type() string { return "json" }

func (i *InfoFlag) Set(str string) error {
	var info map[string]interface{}

	if err := json.Unmarshal([]byte(str), &info); err != nil {
		return err
	} else {
		fileMetaData.Info = info
	}
	return nil
}

var fileMetaData FileMetaData
var sizeFlag SizeFlag
var infoFlag InfoFlag

func init() {
	RootCmd.AddCommand(updateFileCmd)

	updateFileCmd.Flags().StringVarP(&fileMetaData.FileAccess, "file-access", "a", "", "File access")
	updateFileCmd.Flags().VarP(&infoFlag, "info", "i", "File info in json string")
	updateFileCmd.Flags().StringVarP(&fileMetaData.Md5, "md5", "m", "", "File md5")
	updateFileCmd.Flags().VarP(&sizeFlag, "size", "s", "File size")
}

func updateFile(fileID string) {
	studyID := viper.GetString("study")

	b, err := json.Marshal(fileMetaData)
	if err != nil {
		panic(err)
	}

	client := createClient()
	responseBody := client.UpdateFile(studyID, fileID, b)

	var formattedJson bytes.Buffer
	if err := json.Indent(&formattedJson, []byte(responseBody), "", "  "); err != nil {
		panic("Response from server is not a valid json string")
	}

	fmt.Println(formattedJson.String())
}

var updateFileCmd = &cobra.Command{
	Use:   "update-file objectID [flags]",
	Short: "Update a file's metadata",
	Long:  `Update a files's metdata through its object id`,
	Args:  cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		updateFile(args[0])
	},
}
