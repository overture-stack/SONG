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

func init() {
	RootCmd.AddCommand(publishCmd)
}

func publish(analysisID string) {
	client := createClient()
	studyID := viper.GetString("study")
	responseBody := client.Publish(studyID, analysisID)
	fmt.Println(string(responseBody))
}

var publishCmd = &cobra.Command{
	Use:   "publish <analysisID>",
	Short: "Publish a saved Analysis",
	Long:  `Publish a saved Analysis by specifying the AnalysisID`,
	Args:  cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		publish(args[0])
	},
}
