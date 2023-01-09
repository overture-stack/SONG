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

var iFlag bool

func init() {
	RootCmd.AddCommand(saveCmd)
	saveCmd.Flags().BoolVarP(&iFlag, "ignoreCollisions", "i", false, "ignore Collisions with IDs")
}

func save(uploadID string) {
	studyID := viper.GetString("study")
	client := createClient()
	responseBody := client.Save(studyID, uploadID, iFlag)
	fmt.Println(string(responseBody))
}

var saveCmd = &cobra.Command{
	Use:   "save <uploadID>",
	Short: "Save the uploaded Analysis",
	Long:  `Save the uploaded Analysis`,
	Args:  cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		save(args[0])
	},
}
