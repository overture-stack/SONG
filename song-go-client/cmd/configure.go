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
	"os"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var editConfig bool

func init() {
	RootCmd.AddCommand(configureCmd)
	configureCmd.Flags().BoolVarP(&editConfig, "edit", "e", false, "Edit configuration")
}

func doConfigure() {
	file := viper.GetString("config")

	if editConfig {
		editConfiguration(file)
		return
	}

	b, err := ioutil.ReadFile(file)
	if err != nil {
		fmt.Print(err)
	}

	fmt.Println("Showing configuration for config file '" + file + "'")

	if len(b) < 1 {
		fmt.Println("Configuration file does not have any content!")
	} else {
		fmt.Println(string(b))
	}
}

func check(e error) {
	if e != nil {
		panic(e)
	}
}

func editConfiguration(config string) {
	fmt.Println("Setting configuration for config file '" + config + "'")
	file, err := os.Create(config)
	check(err)
	defer file.Close()

	var accessToken string
	fmt.Println("Please enter your access token: ")
	fmt.Scanln(&accessToken)

	var songURL string
	fmt.Println("Please enter URL of SONG server: ")
	fmt.Scanln(&songURL)

	var study string
	fmt.Println("Please enter study ID: ")
	fmt.Scanln(&study)

	accessTokenConfig := "accessToken: " + accessToken + "\n"
	songURLConfig := "songURL: " + songURL + "\n"
	studyConfig := "study: " + study + "\n"

	_, err = file.WriteString(accessTokenConfig)
	check(err)
	_, err = file.WriteString(songURLConfig)
	check(err)
	_, err = file.WriteString(studyConfig)
	check(err)
	file.Sync()
}

var configureCmd = &cobra.Command{
	Use:   "configure",
	Short: "Show/Edit configuration",
	Long:  `Shows configuration values in config file. Use -e to edit.`,
	Run: func(cmd *cobra.Command, args []string) {
		doConfigure()
	},
}
