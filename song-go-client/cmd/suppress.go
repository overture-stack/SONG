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
	"net/url"

	"github.com/overture-stack/SONG/song-go-client/song"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

func init() {
	RootCmd.AddCommand(suppressCmd)
}

func suppress(analysisID string) {
	// init song client
	studyID, accessToken := viper.GetString("study"), viper.GetString("accessToken")
	songURL, err := url.Parse(viper.GetString("songURL"))
	if err != nil {
		panic(err)
	}
	client := song.CreateClient(accessToken, songURL)

	// use song client
	responseBody := client.Suppress(studyID, analysisID)
	fmt.Println(string(responseBody))
}

var suppressCmd = &cobra.Command{
	Use:   "suppress <analysisID>",
	Short: "Suppress Analysis",
	Long:  `Suppresses an analysis`,
	Args:  cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		suppress(args[0])
	},
}
