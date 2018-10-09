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
	"os"
	"path/filepath"

	"github.com/fatih/color"
	homedir "github.com/mitchellh/go-homedir"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var cfgFile string
var accessTokenFlag string
var songURLFlag string
var studyFlag string

var configErrorMsg = `Fatal error, could not find appropriate configuration values.
Please use the configure command, --config option, or --accessToken and --songURL`

// RootCmd is the Base Command for CLI Application
var RootCmd = &cobra.Command{
	Short: `CLI Utility for uploading metadata to a SONG repository`,
	Long:  `CLI Utility for uploading metadata to a SONG repository`,
	Run: func(cmd *cobra.Command, args []string) {
		// Do Stuff Here
	},
}

// Execute adds all child commands to the root command and sets flags appropriately.
// This is called by main.main(). It only needs to happen once to the rootCmd.
func Execute() {
	if err := RootCmd.Execute(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}

// VerifyConfig verifies if the required configuration values have been set.
func VerifyConfig() {
	accessToken := viper.Get("accessToken")
	songURL := viper.Get("songURL")
	study := viper.Get("study")

	if accessToken == nil || accessToken == "" || songURL == nil || songURL == "" || study == nil || study == "" {
		color.Red("Fatal Error. Missing configuration values.")
		color.Red("See --help for additional information.")
	}
}

func init() {
	cobra.OnInitialize(initConfig)

	// Here you will define your flags and configuration settings.
	// Cobra supports persistent flags, which, if defined here,
	// will be global for your application.
	RootCmd.PersistentFlags().StringVar(&cfgFile, "config", "", "use a config file (default is $HOME/.song.yaml)")
	RootCmd.PersistentFlags().StringVar(&accessTokenFlag, "accessToken", "", "Provide an access token for authorizing operations to SONG")
	RootCmd.PersistentFlags().StringVar(&songURLFlag, "songURL", "", "url of SONG server")
	RootCmd.PersistentFlags().StringVar(&studyFlag, "study", "", "study to operate on")
}

// initConfig reads in config file and ENV variables if set.
func initConfig() {

	if cfgFile != "" {
		// Use config file from the flag.
		viper.SetConfigFile(cfgFile)
		viper.Set("config", cfgFile)
	} else {
		// Find home directory.
		home, err := homedir.Dir()
		if err != nil {
			fmt.Println(err)
			os.Exit(1)
		}

		// Search config in home directory with name ".song" (without extension).
		viper.AddConfigPath(home)
		viper.SetConfigName(".song")
		viper.Set("config", filepath.Join(home, ".song.yaml"))
	}

	viper.AutomaticEnv() // Read in environment variables that match
	viper.ReadInConfig() // If a config file is found, read it in.

	// CLI Overrides
	if accessTokenFlag != "" {
		viper.Set("accessToken", accessTokenFlag)
	}
	if songURLFlag != "" {
		viper.Set("songURL", songURLFlag)
	}
	if songURLFlag != "" {
		viper.Set("study", studyFlag)
	}

}
