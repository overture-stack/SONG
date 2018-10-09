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
)

var pFlag bool

func init() {
	RootCmd.AddCommand(statusCmd)
	statusCmd.Flags().BoolVarP(&pFlag, "ping", "p", false, "Just check if server is alive")
}

func getStatus(uploadID string) {
	var responseBody string
	studyID := viper.GetString("study")
	client := createClient()
	if pFlag {
		responseBody = client.GetServerStatus()
	} else {
		responseBody = client.GetStatus(studyID, uploadID)
	}
	fmt.Println(responseBody)
}

var statusCmd = &cobra.Command{
	Use:   "status -p OR status <uploadID>",
	Short: "Get status of uploaded analysis",
	Long:  `Get status of uploaded analysis`,
	Run: func(cmd *cobra.Command, args []string) {
		var uploadID string
		if len(args) > 0 {
			uploadID = args[0]
		} else {
			uploadID = ""
		}
		getStatus(uploadID)
	},
}
