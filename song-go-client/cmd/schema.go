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
)

var listMode bool

var schemaId string

func init() {
	RootCmd.AddCommand(schemaCmd)
	schemaCmd.Flags().BoolVarP(&listMode, "list", "l", false, "List all registered schemas")
	schemaCmd.Flags().StringVarP(&schemaId, "schemaId", "s", "", "Get a schema")
}

func schema() {
	// init song client
	client := createClient()

	// -a analysis-id, -d donor-id, -f file-id, -sa -sample-id,
	// -sp specimen-id
	// -t search-terms ([]), -i info(false)
	var responseBody string

	if listMode {
		responseBody = client.ListSchemas()
	} else {
		responseBody = client.GetSchema(schemaId)
	}

	var formattedJson bytes.Buffer
	if err := json.Indent(&formattedJson, []byte(responseBody), "", "  "); err != nil {
		panic("Response from server is not a valid json string")
	}

	fmt.Println(formattedJson.String())
}

var schemaCmd = &cobra.Command{
	Use:   "schema ( -l | -s <schemaId> )",
	Short: "Retrieve schema information",
	Long: "Retrieve schema information",
	Run: func(cmd *cobra.Command, args []string) {
		schema()
	},
}
