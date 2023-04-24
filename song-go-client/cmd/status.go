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
)

var pFlag bool

func init() {
	RootCmd.AddCommand(statusCmd)
}

func getServerStatus() {
	var responseBody string
	client := createClient()
	responseBody = client.GetServerStatus()
	fmt.Println(responseBody)
}

var statusCmd = &cobra.Command{
	Use:   "status",
	Short: "Get Song Server Status",
	Long:  `Get Song Server Status`,
	Run: func(cmd *cobra.Command, args []string) {
		getServerStatus()
	},
}
